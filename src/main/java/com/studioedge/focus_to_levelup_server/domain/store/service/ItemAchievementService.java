package com.studioedge.focus_to_levelup_server.domain.store.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studioedge.focus_to_levelup_server.domain.focus.dao.DailyGoalRepository;
import com.studioedge.focus_to_levelup_server.domain.focus.dao.DailySubjectRepository;
import com.studioedge.focus_to_levelup_server.domain.focus.entity.DailyGoal;
import com.studioedge.focus_to_levelup_server.domain.focus.entity.DailySubject;
import com.studioedge.focus_to_levelup_server.domain.store.dao.MemberItemRepository;
import com.studioedge.focus_to_levelup_server.domain.store.entity.MemberItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

import static com.studioedge.focus_to_levelup_server.global.common.AppConstants.getServiceDate;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ItemAchievementService {

    private final MemberItemRepository memberItemRepository;
    private final DailyGoalRepository dailyGoalRepository;
    private final DailySubjectRepository dailySubjectRepository;
    private final ObjectMapper objectMapper;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final Map<DayOfWeek, String> DAY_OF_WEEK_KR = Map.of(
            DayOfWeek.MONDAY, "월요일",
            DayOfWeek.TUESDAY, "화요일",
            DayOfWeek.WEDNESDAY, "수요일",
            DayOfWeek.THURSDAY, "목요일",
            DayOfWeek.FRIDAY, "금요일",
            DayOfWeek.SATURDAY, "토요일",
            DayOfWeek.SUNDAY, "일요일"
    );

    // 약점 극복용 짧은 요일 표기
    private static final Map<DayOfWeek, String> DAY_OF_WEEK_SHORT = Map.of(
            DayOfWeek.MONDAY, "월",
            DayOfWeek.TUESDAY, "화",
            DayOfWeek.WEDNESDAY, "수",
            DayOfWeek.THURSDAY, "목",
            DayOfWeek.FRIDAY, "금",
            DayOfWeek.SATURDAY, "토",
            DayOfWeek.SUNDAY, "일"
    );

    /**
     * 집중 세션 종료 시 모든 달성 조건 체크
     *
     * @param memberId 회원 ID
     * @param request 요청온 유저의 집중 시간 정보
     * @param dailyGoal 오늘의 목표 정보 (최대 집중 시간 포함)
     */
    public void checkAchievements(Long memberId, Integer focusSeconds,
                                  LocalDateTime sessionStartTime, DailyGoal dailyGoal) {
        log.info("=== checkAchievements called: memberId={}, focusSeconds={}, startTime={}", memberId, focusSeconds, sessionStartTime);
        LocalDateTime sessionEndTime = LocalDateTime.now();
        LocalDate serviceDate = getServiceDate();

        // 모든 아이템 조회 (progressData 업데이트용)
        List<MemberItem> allMemberItems = memberItemRepository.findAllByMemberIdWithItem(memberId);
        log.info("Found {} total member items", allMemberItems.size());

        // 미완료 아이템 조회 (달성 체크용)
        List<MemberItem> incompleteMemberItems = allMemberItems.stream()
                .filter(mi -> !mi.getIsCompleted())
                .toList();
        log.info("Found {} incomplete member items", incompleteMemberItems.size());

        // 오늘 이미 달성된 아이템 ID 목록 조회
        Set<Long> achievedItemIdsToday = allMemberItems.stream()
                .filter(mi -> mi.getIsCompleted() && mi.getCompletedDate() != null && mi.getCompletedDate().equals(serviceDate))
                .map(mi -> mi.getItem().getId())
                .collect(Collectors.toSet());

        // itemId별로 MemberItem 그룹화 (2회 달성 아이템 처리용)
        Map<Long, List<MemberItem>> memberItemsByItemId = allMemberItems.stream()
                .collect(Collectors.groupingBy(mi -> mi.getItem().getId()));

        // 1단계: 모든 미완료 아이템의 progressData를 먼저 업데이트 (오늘의 값으로)
        for (MemberItem memberItem : incompleteMemberItems) {
            String itemName = memberItem.getItem().getName();
            try {
                switch (itemName) {
                    case "집중력 폭발" -> updateConsecutiveFocusProgress(memberItem, dailyGoal);
                    case "시작 시간 사수" -> updateMorningStartProgress(memberItem, dailyGoal, serviceDate);
                    case "마지막 생존자" -> updateLateNightEndProgress(memberItem, dailyGoal, serviceDate);
                    case "휴식은 사치" -> updateLimitedRestProgress(memberItem, memberId, serviceDate, dailyGoal);
                    case "약점 극복" -> updateWeakestDayProgress(memberItem, memberId, serviceDate);
                    case "저지 불가" -> updateSevenDaysStreakProgress(memberItem, memberId, serviceDate);
                    case "과거 나와 대결" -> updateBeatLastWeekProgress(memberItem, memberId, serviceDate);
                    case "누적 집중의 대가" -> updateWeeklyAccumulationProgress(memberItem, memberId, serviceDate);
                }
                log.debug("Updated progressData for incomplete item: memberItemId={}, itemName={}", memberItem.getId(), itemName);
            } catch (Exception e) {
                log.error("Error updating progressData for item: {}", itemName, e);
            }
        }

        // 이미 처리한 itemId 추적 (같은 itemId는 한 번만 체크)
        Set<Long> processedItemIds = new HashSet<>();

        // 2단계: 달성 체크 (오늘 아직 달성하지 않은 아이템만)
        for (MemberItem memberItem : incompleteMemberItems) {
            Long itemId = memberItem.getItem().getId();

            // 이미 처리한 itemId는 스킵
            if (processedItemIds.contains(itemId)) {
                continue;
            }
            processedItemIds.add(itemId);

            // 오늘 이미 달성한 아이템 ID는 스킵 (같은 아이템은 하루에 1개만 달성)
            if (achievedItemIdsToday.contains(itemId)) {
                continue;
            }

            String itemName = memberItem.getItem().getName();
            boolean isAchieved = false;

            // Item.name으로 switch 분기
            try {
                isAchieved = switch (itemName) {
                    case "집중력 폭발" -> checkConsecutiveFocus(memberItem, dailyGoal, serviceDate);
                    case "시작 시간 사수" -> checkMorningStart(memberItem, dailyGoal, serviceDate);
                    case "마지막 생존자" -> checkLateNightEnd(memberItem, dailyGoal, serviceDate);
                    case "휴식은 사치" -> checkLimitedRest(memberItem, memberId, serviceDate, dailyGoal);
                    case "약점 극복" -> checkWeakestDayImprovement(memberItem, memberId, serviceDate);
                    case "저지 불가" -> checkSevenDaysStreak(memberItem, memberId, serviceDate);
                    case "과거 나와 대결" -> checkBeatLastWeek(memberItem, memberId, serviceDate);
                    case "누적 집중의 대가" -> checkWeeklyAccumulation(memberItem, memberId, serviceDate);
                    default -> {
                        log.warn("Unknown item name: {}", itemName);
                        yield false;
                    }
                };

                if (isAchieved) {
                    memberItem.complete(serviceDate);
                    achievedItemIdsToday.add(itemId);
                    log.info("Item achieved: memberId={}, itemName={}, serviceDate={}", memberId, itemName, serviceDate);
                }
            } catch (Exception e) {
                log.error("Error checking achievement for item: {}", itemName, e);
            }
        }

        // 3단계: 모두 달성된 itemId의 achievedDay 통일 처리
        unifyAchievedDaysForCompletedItems(memberItemsByItemId);
    }

    /**
     * 모두 달성된 itemId의 achievedDay를 통일 (예: "수요일, 목요일")
     */
    private void unifyAchievedDaysForCompletedItems(Map<Long, List<MemberItem>> memberItemsByItemId) {
        for (Map.Entry<Long, List<MemberItem>> entry : memberItemsByItemId.entrySet()) {
            List<MemberItem> items = entry.getValue();

            // 모든 아이템이 달성되었는지 확인
            boolean allCompleted = items.stream().allMatch(MemberItem::getIsCompleted);
            if (!allCompleted || items.size() < 2) {
                continue;
            }

            // 달성된 요일들 수집 (중복 제거, 순서 유지)
            List<String> achievedDays = new ArrayList<>();
            for (MemberItem item : items) {
                if (item.getCompletedDate() != null) {
                    String dayKr = DAY_OF_WEEK_KR.get(item.getCompletedDate().getDayOfWeek());
                    if (!achievedDays.contains(dayKr)) {
                        achievedDays.add(dayKr);
                    }
                }
            }

            if (achievedDays.isEmpty()) {
                continue;
            }

            String unifiedAchievedDay = String.join(", ", achievedDays);

            // 모든 아이템의 progressData에 통일된 achievedDay 설정
            for (MemberItem item : items) {
                try {
                    String progressDataStr = item.getProgressData();
                    if (progressDataStr != null && !progressDataStr.isEmpty()) {
                        Map<String, Object> progressData = objectMapper.readValue(progressDataStr, Map.class);
                        progressData.put("achievedDay", unifiedAchievedDay);
                        item.updateProgressData(objectMapper.writeValueAsString(progressData));
                    }
                } catch (Exception e) {
                    log.error("Error unifying achievedDay for item: {}", item.getId(), e);
                }
            }
            log.info("Unified achievedDay for itemId={}: {}", entry.getKey(), unifiedAchievedDay);
        }
    }

    /**
     * 1. 집중력 폭발: 연속 집중 시간 >= parameter (60/90/120분)
     * DailyGoal의 maxConsecutiveSeconds를 Single Source of Truth로 사용
     */
    private boolean checkConsecutiveFocus(MemberItem memberItem, DailyGoal dailyGoal, LocalDate serviceDate) {
        int requiredMinutes = memberItem.getSelection();

        // DailyGoal에서 최대 집중 시간 가져오기 (Single Source of Truth)
        int maxConsecutiveMinutes = dailyGoal.getMaxConsecutiveSeconds() / 60;

        // progressData 업데이트 (표시용으로만 사용, DailyGoal의 값을 반영)
        Map<String, Object> progressData = new HashMap<>();
        progressData.put("maxConsecutiveMinutes", maxConsecutiveMinutes); // DailyGoal에서 읽은 값
        progressData.put("requiredMinutes", requiredMinutes);

        // 달성 여부는 DailyGoal의 최대값으로 판단
        boolean isAchieved = maxConsecutiveMinutes >= requiredMinutes;

        // 달성 여부와 관계없이 키 유지 (달성 시 값 설정, 미달성 시 null)
        progressData.put("achievedDate", isAchieved ? serviceDate.format(DATE_FORMATTER) : null);
        progressData.put("achievedDay", isAchieved ? DAY_OF_WEEK_KR.get(serviceDate.getDayOfWeek()) : null);

        try {
            memberItem.updateProgressData(objectMapper.writeValueAsString(progressData));
        } catch (Exception e) {
            log.error("Error updating progressData for 집중력 폭발", e);
        }

        return isAchieved;
    }

    /**
     * 2. 시작 시간 사수: 시작 시각 < parameter (6시/7시/8시)
     * 단, 새벽 4시 이후 시작만 인정
     * DailyGoal.earliestStartTime을 Single Source of Truth로 사용
     */
    private boolean checkMorningStart(MemberItem memberItem, DailyGoal dailyGoal, LocalDate serviceDate) {
        int requiredHour = memberItem.getSelection();

        // DailyGoal에서 가장 빠른 시작 시간 가져오기 (Single Source of Truth)
        LocalTime earliestStartTime = dailyGoal.getEarliestStartTime();

        // progressData 업데이트
        Map<String, Object> progressData = new HashMap<>();
        progressData.put("requiredHour", requiredHour);

        // 집중한 적 없으면 "--:--", 있으면 시간 문자열
        if (earliestStartTime != null) {
            progressData.put("earliestStartTime", earliestStartTime.format(TIME_FORMATTER));
        } else {
            progressData.put("earliestStartTime", "--:--");
        }

        // 달성 조건: 새벽 4시 이후 && 요구 시간보다 이른 경우
        boolean isAchieved = earliestStartTime != null &&
                earliestStartTime.getHour() >= 4 &&
                earliestStartTime.getHour() < requiredHour;

        // 달성 여부와 관계없이 키 유지 (달성 시 값 설정, 미달성 시 null)
        progressData.put("achievedDate", isAchieved ? serviceDate.format(DATE_FORMATTER) : null);
        progressData.put("achievedDay", isAchieved ? DAY_OF_WEEK_KR.get(serviceDate.getDayOfWeek()) : null);

        try {
            memberItem.updateProgressData(objectMapper.writeValueAsString(progressData));
        } catch (Exception e) {
            log.error("Error updating progressData for 시작 시간 사수", e);
        }

        return isAchieved;
    }

    /**
     * 3. 마지막 생존자: 종료 시각 >= parameter (22시/23시/자정)
     * DailyGoal.latestEndTime을 Single Source of Truth로 사용
     */
    private boolean checkLateNightEnd(MemberItem memberItem, DailyGoal dailyGoal, LocalDate serviceDate) {
        int requiredHour = memberItem.getSelection();

        // DailyGoal에서 가장 늦은 종료 시간 가져오기 (Single Source of Truth)
        LocalTime latestEndTime = dailyGoal.getLatestEndTime();

        // progressData 업데이트
        Map<String, Object> progressData = new HashMap<>();
        progressData.put("requiredHour", requiredHour);

        // 집중한 적 없으면 "--:--", 있으면 시간 문자열
        if (latestEndTime != null) {
            progressData.put("latestEndTime", latestEndTime.format(TIME_FORMATTER));
        } else {
            progressData.put("latestEndTime", "--:--");
        }

        // 달성 조건: 자정(0시)의 경우 0-4시 사이도 인정
        boolean isAchieved = false;
        if (latestEndTime != null) {
            int latestHour = latestEndTime.getHour();
            isAchieved = (requiredHour == 0 && latestHour >= 0 && latestHour < 4) ||
                         (requiredHour > 0 && latestHour >= requiredHour);
        }

        // 달성 여부와 관계없이 키 유지 (달성 시 값 설정, 미달성 시 null)
        progressData.put("achievedDate", isAchieved ? serviceDate.format(DATE_FORMATTER) : null);
        progressData.put("achievedDay", isAchieved ? DAY_OF_WEEK_KR.get(serviceDate.getDayOfWeek()) : null);

        try {
            memberItem.updateProgressData(objectMapper.writeValueAsString(progressData));
        } catch (Exception e) {
            log.error("Error updating progressData for 마지막 생존자", e);
        }

        return isAchieved;
    }

    /**
     * 4. 휴식은 사치: 하루 쉬는 시간 < parameter (4/5/6시간)
     * 쉬는 시간 = (오늘 마지막 종료 시각 - 오늘 첫 시작 시각) - 총 집중 시간
     *
     * 주의: 이 미션은 하루가 끝나기 전까지 휴식 시간이 계속 증가할 수 있으므로,
     * 집중 세션 종료 시에는 성공 판정을 하지 않고 progressData만 업데이트합니다.
     * 실제 성공 판정은 "오늘의 학습 종료" 시점 또는 새벽 4시 Daily Batch에서 수행됩니다.
     * @see #checkRestIsLuxuryOnStudyEnd(Long, LocalDate, DailyGoal)
     * @see com.studioedge.focus_to_levelup_server.global.batch.step.daily.CheckRestIsLuxuryStep
     */
    private boolean checkLimitedRest(MemberItem memberItem, Long memberId, LocalDate serviceDate, DailyGoal dailyGoal) {
        // progressData 업데이트는 updateLimitedRestProgress에서 수행
        // 성공 판정은 학습 종료 시점 또는 Daily Batch에서 수행하므로 항상 false 반환
        return false;
    }

    /**
     * "휴식은 사치" 미션 성공 판정 (오늘의 학습 종료 시점에 호출)
     *
     * @param memberId 회원 ID
     * @param serviceDate 서비스 날짜
     * @param dailyGoal 오늘의 목표 정보
     */
    public void checkRestIsLuxuryOnStudyEnd(Long memberId, LocalDate serviceDate, DailyGoal dailyGoal) {
        log.info("=== checkRestIsLuxuryOnStudyEnd called: memberId={}, serviceDate={}", memberId, serviceDate);

        // "휴식은 사치" 미완료 아이템 조회
        List<MemberItem> memberItems = memberItemRepository.findAllByMemberIdWithItem(memberId).stream()
                .filter(mi -> !mi.getIsCompleted())
                .filter(mi -> "휴식은 사치".equals(mi.getItem().getName()))
                .toList();

        if (memberItems.isEmpty()) {
            log.debug("No incomplete '휴식은 사치' items for memberId={}", memberId);
            return;
        }

        // DailyGoal에서 시작/종료 시각 조회
        LocalTime earliestStartTime = dailyGoal.getEarliestStartTime();
        LocalTime latestEndTime = dailyGoal.getLatestEndTime();

        if (earliestStartTime == null || latestEndTime == null) {
            log.debug("No start/end time for memberId={} on {}", memberId, serviceDate);
            return;
        }

        // 총 집중 시간 조회
        List<DailySubject> todaySubjects = dailySubjectRepository.findAllByMemberIdAndDateRangeWithSubject(
                memberId, serviceDate, serviceDate
        );

        int totalFocusSeconds = todaySubjects.stream()
                .mapToInt(DailySubject::getFocusSeconds)
                .sum();

        // 활동 시간대 계산 (종료 - 시작)
        long activitySeconds;
        if (latestEndTime.isBefore(earliestStartTime)) {
            // 자정을 넘긴 경우 (예: 시작 22:00, 종료 02:00)
            activitySeconds = (24 * 3600) - earliestStartTime.toSecondOfDay() + latestEndTime.toSecondOfDay();
        } else {
            activitySeconds = latestEndTime.toSecondOfDay() - earliestStartTime.toSecondOfDay();
        }

        // 쉬는 시간 = 활동 시간대 - 총 집중 시간 (음수면 0으로 처리)
        long restSeconds = Math.max(0, activitySeconds - totalFocusSeconds);
        double restHours = restSeconds / 3600.0;
        int restMinutes = (int) (restSeconds / 60);

        log.info("REST_IS_LUXURY check: memberId={}, restHours={:.1f}, activitySeconds={}, totalFocusSeconds={}",
                memberId, restHours, activitySeconds, totalFocusSeconds);

        // 오늘 이미 달성된 아이템 ID 추적 (같은 아이템은 하루에 1개만 달성)
        Set<Long> achievedItemIdsToday = new HashSet<>();

        for (MemberItem memberItem : memberItems) {
            Long itemId = memberItem.getItem().getId();

            // 이미 오늘 같은 itemId가 달성되었으면 스킵
            if (achievedItemIdsToday.contains(itemId)) {
                continue;
            }

            int requiredRestHours = memberItem.getSelection();
            boolean isAchieved = restHours < requiredRestHours;

            // progressData 업데이트
            Map<String, Object> progressData = new HashMap<>();
            progressData.put("todayRestHours", Math.round(restHours * 10) / 10.0);
            progressData.put("todayRestMinutes", restMinutes);
            progressData.put("requiredRestHours", requiredRestHours);
            progressData.put("achievedDate", isAchieved ? serviceDate.format(DATE_FORMATTER) : null);
            progressData.put("achievedDay", isAchieved ? DAY_OF_WEEK_KR.get(serviceDate.getDayOfWeek()) : null);

            try {
                memberItem.updateProgressData(objectMapper.writeValueAsString(progressData));
            } catch (Exception e) {
                log.error("Error updating progressData for 휴식은 사치", e);
            }

            if (isAchieved) {
                memberItem.complete(serviceDate);
                achievedItemIdsToday.add(itemId);
                log.info("REST_IS_LUXURY achieved: memberId={}, selection={}h, restHours={:.1f}h",
                        memberId, requiredRestHours, restHours);
            }
        }
    }

    /**
     * 5. 약점 극복: 지난 주 가장 약한 요일에서 지난 주 평균 이상 집중하면 달성
     * - 지난 주 가장 집중 시간이 적은 요일 찾기 (동점이면 가장 늦은 요일)
     * - 이번 주 해당 요일 집중 시간 >= 지난 주 평균이면 달성
     */
    private boolean checkWeakestDayImprovement(MemberItem memberItem, Long memberId, LocalDate serviceDate) {
        LocalDate weekStart = serviceDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = serviceDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        // 지난 주 범위
        LocalDate lastWeekStart = weekStart.minusWeeks(1);
        LocalDate lastWeekEnd = weekEnd.minusWeeks(1);

        // 지난 주 집중 기록 조회
        List<DailySubject> lastWeekSubjects = dailySubjectRepository.findAllByMemberIdAndDateRangeWithSubject(
                memberId, lastWeekStart, lastWeekEnd
        );

        // 지난 주 날짜별 집중 시간 집계
        Map<LocalDate, Integer> lastWeekDailyMap = lastWeekSubjects.stream()
                .collect(Collectors.groupingBy(
                        DailySubject::getDate,
                        Collectors.summingInt(DailySubject::getFocusSeconds)
                ));

        Map<String, Object> progressData = new HashMap<>();

        // 지난 주 7일의 집중 시간 (기록 없는 날은 0으로 채움)
        Map<DayOfWeek, Integer> lastWeekByDayOfWeek = new HashMap<>();
        for (int i = 0; i < 7; i++) {
            LocalDate date = lastWeekStart.plusDays(i);
            lastWeekByDayOfWeek.put(date.getDayOfWeek(), lastWeekDailyMap.getOrDefault(date, 0));
        }

        // 지난 주 최소값 (가장 약한 요일)
        int lastWeekMinSeconds = lastWeekByDayOfWeek.values().stream()
                .mapToInt(Integer::intValue)
                .min()
                .orElse(0);

        // 지난 주 평균 계산
        double lastWeekAverageSeconds = lastWeekByDayOfWeek.values().stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);

        // 가장 늦은 약한 요일 하나만 선택 (표시 및 달성 조건 통일)
        DayOfWeek weakestDay = lastWeekByDayOfWeek.entrySet().stream()
                .filter(e -> e.getValue() == lastWeekMinSeconds)
                .map(Map.Entry::getKey)
                .max(Comparator.comparingInt(DayOfWeek::getValue))
                .orElse(DayOfWeek.SUNDAY);

        // 이번 주 집중 기록 조회
        List<DailySubject> thisWeekSubjects = dailySubjectRepository.findAllByMemberIdAndDateRangeWithSubject(
                memberId, weekStart, weekEnd
        );

        // 이번 주 날짜별 집중 시간 집계
        Map<LocalDate, Integer> thisWeekDailyMap = thisWeekSubjects.stream()
                .collect(Collectors.groupingBy(
                        DailySubject::getDate,
                        Collectors.summingInt(DailySubject::getFocusSeconds)
                ));

        // 약한 요일의 시간 표시 (요일 - HM 형식)
        int weakestMinutes = lastWeekMinSeconds / 60;
        int hours = weakestMinutes / 60;
        int mins = weakestMinutes % 60;

        // "일 - 0H0M" 형식으로 표시
        String displayText = DAY_OF_WEEK_SHORT.get(weakestDay) + " - " + hours + "H" + mins + "M";

        // 달성 조건: 이번 주 해당 요일 집중 시간 >= 지난 주 평균
        LocalDate thisWeekTargetDay = weekStart.with(TemporalAdjusters.nextOrSame(weakestDay));
        int thisWeekTargetDaySeconds = thisWeekDailyMap.getOrDefault(thisWeekTargetDay, 0);
        int thisWeekTargetDayMinutes = thisWeekTargetDaySeconds / 60;

        progressData.put("weakestDay", DAY_OF_WEEK_SHORT.get(weakestDay));
        progressData.put("weakestDayMinutes", weakestMinutes);
        progressData.put("displayText", displayText);
        progressData.put("lastWeekAverageMinutes", (int) (lastWeekAverageSeconds / 60));
        progressData.put("thisWeekTargetDayMinutes", thisWeekTargetDayMinutes);

        boolean isAchieved = lastWeekAverageSeconds > 0 && thisWeekTargetDaySeconds >= lastWeekAverageSeconds;

        // 달성 여부와 관계없이 키 유지 (달성 시 값 설정, 미달성 시 null)
        progressData.put("achievedDate", isAchieved ? serviceDate.format(DATE_FORMATTER) : null);
        progressData.put("achievedDay", isAchieved ? DAY_OF_WEEK_KR.get(serviceDate.getDayOfWeek()) : null);

        try {
            memberItem.updateProgressData(objectMapper.writeValueAsString(progressData));
        } catch (Exception e) {
            log.error("Error updating progressData for 약점 극복", e);
        }

        return isAchieved;
    }

    /**
     * 6. 저지 불가: 7일 모두 30분 이상
     * - 30분 이상 집중한 요일 없으면 "--" 반환
     * - 있으면 가장 늦은 요일 하나만 표시 (예: "목요일")
     */
    private boolean checkSevenDaysStreak(MemberItem memberItem, Long memberId, LocalDate serviceDate) {
        LocalDate weekStart = serviceDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = serviceDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        // 이번 주 집중 기록 조회
        List<DailySubject> weeklySubjects = dailySubjectRepository.findAllByMemberIdAndDateRangeWithSubject(
                memberId, weekStart, weekEnd
        );

        // 날짜별 집중 시간 집계
        Map<LocalDate, Integer> dailyFocusMap = weeklySubjects.stream()
                .collect(Collectors.groupingBy(
                        DailySubject::getDate,
                        Collectors.summingInt(DailySubject::getFocusSeconds)
                ));

        // 30분 이상 집중한 요일 찾기
        List<DayOfWeek> achievedDays = dailyFocusMap.entrySet().stream()
                .filter(e -> e.getValue() >= 1800)
                .map(e -> e.getKey().getDayOfWeek())
                .sorted()
                .toList();

        // progressData 업데이트
        Map<String, Object> progressData = new HashMap<>();
        progressData.put("achievedDaysCount", achievedDays.size());

        // 30분 이상 집중한 요일 없으면 "--", 있으면 가장 늦은 요일 하나만 표시
        if (achievedDays.isEmpty()) {
            progressData.put("displayText", "--");
        } else {
            // 가장 늦은 요일 (일요일에 가까운)
            DayOfWeek latestDay = achievedDays.stream()
                    .max(Comparator.comparingInt(DayOfWeek::getValue))
                    .orElse(DayOfWeek.MONDAY);
            progressData.put("displayText", DAY_OF_WEEK_KR.get(latestDay));
        }

        boolean isAchieved = achievedDays.size() == 7;

        // 달성 여부와 관계없이 키 유지 (달성 시 값 설정, 미달성 시 null)
        progressData.put("achievedDate", isAchieved ? serviceDate.format(DATE_FORMATTER) : null);
        progressData.put("achievedDay", isAchieved ? DAY_OF_WEEK_KR.get(serviceDate.getDayOfWeek()) : null);

        try {
            memberItem.updateProgressData(objectMapper.writeValueAsString(progressData));
        } catch (Exception e) {
            log.error("Error updating progressData for 저지 불가", e);
        }

        return isAchieved;
    }

    /**
     * 7. 과거 나와 대결: 이번 주 > 지난 주
     */
    private boolean checkBeatLastWeek(MemberItem memberItem, Long memberId, LocalDate serviceDate) {
        // 이번 주 범위
        LocalDate thisWeekStart = serviceDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate thisWeekEnd = serviceDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        // 지난 주 범위
        LocalDate lastWeekStart = thisWeekStart.minusWeeks(1);
        LocalDate lastWeekEnd = thisWeekEnd.minusWeeks(1);

        // 이번 주 총 집중 시간
        List<DailySubject> thisWeekSubjects = dailySubjectRepository.findAllByMemberIdAndDateRangeWithSubject(
                memberId, thisWeekStart, thisWeekEnd
        );
        int thisWeekSeconds = thisWeekSubjects.stream()
                .mapToInt(DailySubject::getFocusSeconds)
                .sum();

        // 지난 주 총 집중 시간
        List<DailySubject> lastWeekSubjects = dailySubjectRepository.findAllByMemberIdAndDateRangeWithSubject(
                memberId, lastWeekStart, lastWeekEnd
        );
        int lastWeekSeconds = lastWeekSubjects.stream()
                .mapToInt(DailySubject::getFocusSeconds)
                .sum();

        // progressData 업데이트
        Map<String, Object> progressData = new HashMap<>();
        progressData.put("lastWeekMinutes", lastWeekSeconds / 60);
        progressData.put("thisWeekMinutes", thisWeekSeconds / 60);

        // 이번 주가 지난 주보다 많아야 함 (지난 주가 0이어도 이번 주가 0보다 크면 달성)
        boolean isAchieved = thisWeekSeconds > lastWeekSeconds;

        // 달성 여부와 관계없이 키 유지 (달성 시 값 설정, 미달성 시 null)
        progressData.put("achievedDate", isAchieved ? serviceDate.format(DATE_FORMATTER) : null);
        progressData.put("achievedDay", isAchieved ? DAY_OF_WEEK_KR.get(serviceDate.getDayOfWeek()) : null);

        try {
            memberItem.updateProgressData(objectMapper.writeValueAsString(progressData));
        } catch (Exception e) {
            log.error("Error updating progressData for 과거 나와 대결", e);
        }

        return isAchieved;
    }

    /**
     * 8. 누적 집중의 대가: 주간 누적 >= parameter (25/30/35/40/45/50/55/60시간)
     */
    private boolean checkWeeklyAccumulation(MemberItem memberItem, Long memberId, LocalDate serviceDate) {
        LocalDate weekStart = serviceDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = serviceDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        // 이번 주 총 집중 시간 조회
        List<DailySubject> weeklySubjects = dailySubjectRepository.findAllByMemberIdAndDateRangeWithSubject(
                memberId, weekStart, weekEnd
        );

        int totalSeconds = weeklySubjects.stream()
                .mapToInt(DailySubject::getFocusSeconds)
                .sum();

        double totalHours = totalSeconds / 3600.0;
        int totalMinutes = totalSeconds / 60;
        int requiredHours = memberItem.getSelection();

        // progressData 업데이트 (항상)
        Map<String, Object> progressData = new HashMap<>();
        progressData.put("thisWeekMinutes", totalMinutes);
        progressData.put("targetHours", requiredHours);

        boolean isAchieved = totalHours >= requiredHours;

        // 달성 여부와 관계없이 키 유지 (달성 시 값 설정, 미달성 시 null)
        progressData.put("achievedDate", isAchieved ? serviceDate.format(DATE_FORMATTER) : null);
        progressData.put("achievedDay", isAchieved ? DAY_OF_WEEK_KR.get(serviceDate.getDayOfWeek()) : null);

        try {
            memberItem.updateProgressData(objectMapper.writeValueAsString(progressData));
        } catch (Exception e) {
            log.error("Error updating progressData for 누적 집중의 대가", e);
        }

        return isAchieved;
    }

    // ========== progressData만 업데이트하는 헬퍼 메서드들 (달성 처리 없이) ==========

    /**
     * 집중력 폭발 - progressData만 업데이트
     */
    private void updateConsecutiveFocusProgress(MemberItem memberItem, DailyGoal dailyGoal) {
        int requiredMinutes = memberItem.getSelection();
        int maxConsecutiveMinutes = dailyGoal.getMaxConsecutiveSeconds() / 60;

        Map<String, Object> progressData = new HashMap<>();
        progressData.put("maxConsecutiveMinutes", maxConsecutiveMinutes);
        progressData.put("requiredMinutes", requiredMinutes);

        try {
            memberItem.updateProgressData(objectMapper.writeValueAsString(progressData));
        } catch (Exception e) {
            log.error("Error updating progressData for 집중력 폭발 (sibling)", e);
        }
    }

    /**
     * 시작 시간 사수 - progressData만 업데이트 (DailyGoal 기반)
     */
    private void updateMorningStartProgress(MemberItem memberItem, DailyGoal dailyGoal, LocalDate serviceDate) {
        int requiredHour = memberItem.getSelection();
        LocalTime earliestStartTime = dailyGoal.getEarliestStartTime();

        Map<String, Object> progressData = new HashMap<>();
        progressData.put("requiredHour", requiredHour);

        if (earliestStartTime != null) {
            progressData.put("earliestStartTime", earliestStartTime.format(TIME_FORMATTER));
        } else {
            progressData.put("earliestStartTime", "--:--");
        }

        try {
            memberItem.updateProgressData(objectMapper.writeValueAsString(progressData));
        } catch (Exception e) {
            log.error("Error updating progressData for 시작 시간 사수 (sibling)", e);
        }
    }

    /**
     * 마지막 생존자 - progressData만 업데이트 (DailyGoal 기반)
     */
    private void updateLateNightEndProgress(MemberItem memberItem, DailyGoal dailyGoal, LocalDate serviceDate) {
        int requiredHour = memberItem.getSelection();
        LocalTime latestEndTime = dailyGoal.getLatestEndTime();

        Map<String, Object> progressData = new HashMap<>();
        progressData.put("requiredHour", requiredHour);

        if (latestEndTime != null) {
            progressData.put("latestEndTime", latestEndTime.format(TIME_FORMATTER));
        } else {
            progressData.put("latestEndTime", "--:--");
        }

        try {
            memberItem.updateProgressData(objectMapper.writeValueAsString(progressData));
        } catch (Exception e) {
            log.error("Error updating progressData for 마지막 생존자 (sibling)", e);
        }
    }

    /**
     * 휴식은 사치 - progressData만 업데이트
     */
    private void updateLimitedRestProgress(MemberItem memberItem, Long memberId, LocalDate serviceDate, DailyGoal dailyGoal) {
        LocalTime earliestStartTime = dailyGoal.getEarliestStartTime();
        LocalTime latestEndTime = dailyGoal.getLatestEndTime();

        if (earliestStartTime == null || latestEndTime == null) {
            return;
        }

        List<DailySubject> todaySubjects = dailySubjectRepository.findAllByMemberIdAndDateRangeWithSubject(
                memberId, serviceDate, serviceDate
        );

        int totalFocusSeconds = todaySubjects.stream()
                .mapToInt(DailySubject::getFocusSeconds)
                .sum();

        long activitySeconds;
        if (latestEndTime.isBefore(earliestStartTime)) {
            activitySeconds = (24 * 3600) - earliestStartTime.toSecondOfDay() + latestEndTime.toSecondOfDay();
        } else {
            activitySeconds = latestEndTime.toSecondOfDay() - earliestStartTime.toSecondOfDay();
        }

        long restSeconds = Math.max(0, activitySeconds - totalFocusSeconds);
        double restHours = restSeconds / 3600.0;
        int restMinutes = (int) (restSeconds / 60);
        int requiredRestHours = memberItem.getSelection();

        Map<String, Object> progressData = new HashMap<>();
        progressData.put("todayRestHours", Math.round(restHours * 10) / 10.0);
        progressData.put("todayRestMinutes", restMinutes);
        progressData.put("requiredRestHours", requiredRestHours);

        try {
            memberItem.updateProgressData(objectMapper.writeValueAsString(progressData));
        } catch (Exception e) {
            log.error("Error updating progressData for 휴식은 사치 (sibling)", e);
        }
    }

    /**
     * 약점 극복 - progressData만 업데이트 (늦은 요일 하나만, 요일+시간 형식)
     */
    private void updateWeakestDayProgress(MemberItem memberItem, Long memberId, LocalDate serviceDate) {
        LocalDate weekStart = serviceDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = serviceDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        LocalDate lastWeekStart = weekStart.minusWeeks(1);
        LocalDate lastWeekEnd = weekEnd.minusWeeks(1);

        List<DailySubject> lastWeekSubjects = dailySubjectRepository.findAllByMemberIdAndDateRangeWithSubject(
                memberId, lastWeekStart, lastWeekEnd
        );

        Map<LocalDate, Integer> lastWeekDailyMap = lastWeekSubjects.stream()
                .collect(Collectors.groupingBy(
                        DailySubject::getDate,
                        Collectors.summingInt(DailySubject::getFocusSeconds)
                ));

        // 지난 주 7일의 집중 시간 (기록 없는 날은 0으로 채움)
        Map<DayOfWeek, Integer> lastWeekByDayOfWeek = new HashMap<>();
        for (int i = 0; i < 7; i++) {
            LocalDate date = lastWeekStart.plusDays(i);
            lastWeekByDayOfWeek.put(date.getDayOfWeek(), lastWeekDailyMap.getOrDefault(date, 0));
        }

        // 지난 주 최소값 (가장 약한 요일)
        int lastWeekMinSeconds = lastWeekByDayOfWeek.values().stream()
                .mapToInt(Integer::intValue)
                .min()
                .orElse(0);

        // 지난 주 평균 계산
        double lastWeekAverageSeconds = lastWeekByDayOfWeek.values().stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);

        // 가장 늦은 요일 하나만
        DayOfWeek weakestDay = lastWeekByDayOfWeek.entrySet().stream()
                .filter(e -> e.getValue() == lastWeekMinSeconds)
                .map(Map.Entry::getKey)
                .max(Comparator.comparingInt(DayOfWeek::getValue))
                .orElse(DayOfWeek.SUNDAY);

        // 이번 주 집중 기록 조회
        List<DailySubject> thisWeekSubjects = dailySubjectRepository.findAllByMemberIdAndDateRangeWithSubject(
                memberId, weekStart, weekEnd
        );

        Map<LocalDate, Integer> thisWeekDailyMap = thisWeekSubjects.stream()
                .collect(Collectors.groupingBy(
                        DailySubject::getDate,
                        Collectors.summingInt(DailySubject::getFocusSeconds)
                ));

        // 이번 주 해당 요일 집중 시간
        LocalDate thisWeekTargetDay = weekStart.with(TemporalAdjusters.nextOrSame(weakestDay));
        int thisWeekTargetDaySeconds = thisWeekDailyMap.getOrDefault(thisWeekTargetDay, 0);
        int thisWeekTargetDayMinutes = thisWeekTargetDaySeconds / 60;

        // 요일 - HM 형식
        int weakestMinutes = lastWeekMinSeconds / 60;
        int hours = weakestMinutes / 60;
        int mins = weakestMinutes % 60;

        // "일 - 0H0M" 형식으로 표시
        String displayText = DAY_OF_WEEK_SHORT.get(weakestDay) + " - " + hours + "H" + mins + "M";

        Map<String, Object> progressData = new HashMap<>();
        progressData.put("weakestDay", DAY_OF_WEEK_SHORT.get(weakestDay));
        progressData.put("weakestDayMinutes", weakestMinutes);
        progressData.put("displayText", displayText);
        progressData.put("lastWeekAverageMinutes", (int) (lastWeekAverageSeconds / 60));
        progressData.put("thisWeekTargetDayMinutes", thisWeekTargetDayMinutes);

        try {
            memberItem.updateProgressData(objectMapper.writeValueAsString(progressData));
        } catch (Exception e) {
            log.error("Error updating progressData for 약점 극복 (sibling)", e);
        }
    }

    /**
     * 저지 불가 - progressData만 업데이트 (30분 이상 없으면 "--", 있으면 늦은 요일 하나)
     */
    private void updateSevenDaysStreakProgress(MemberItem memberItem, Long memberId, LocalDate serviceDate) {
        LocalDate weekStart = serviceDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = serviceDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        List<DailySubject> weeklySubjects = dailySubjectRepository.findAllByMemberIdAndDateRangeWithSubject(
                memberId, weekStart, weekEnd
        );

        Map<LocalDate, Integer> dailyFocusMap = weeklySubjects.stream()
                .collect(Collectors.groupingBy(
                        DailySubject::getDate,
                        Collectors.summingInt(DailySubject::getFocusSeconds)
                ));

        List<DayOfWeek> achievedDays = dailyFocusMap.entrySet().stream()
                .filter(e -> e.getValue() >= 1800)
                .map(e -> e.getKey().getDayOfWeek())
                .sorted()
                .toList();

        Map<String, Object> progressData = new HashMap<>();
        progressData.put("achievedDaysCount", achievedDays.size());

        // 30분 이상 집중한 요일 없으면 "--", 있으면 가장 늦은 요일 하나만
        if (achievedDays.isEmpty()) {
            progressData.put("displayText", "--");
        } else {
            DayOfWeek latestDay = achievedDays.stream()
                    .max(Comparator.comparingInt(DayOfWeek::getValue))
                    .orElse(DayOfWeek.MONDAY);
            progressData.put("displayText", DAY_OF_WEEK_KR.get(latestDay));
        }

        try {
            memberItem.updateProgressData(objectMapper.writeValueAsString(progressData));
        } catch (Exception e) {
            log.error("Error updating progressData for 저지 불가 (sibling)", e);
        }
    }

    /**
     * 과거 나와 대결 - progressData만 업데이트
     */
    private void updateBeatLastWeekProgress(MemberItem memberItem, Long memberId, LocalDate serviceDate) {
        LocalDate thisWeekStart = serviceDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate thisWeekEnd = serviceDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        LocalDate lastWeekStart = thisWeekStart.minusWeeks(1);
        LocalDate lastWeekEnd = thisWeekEnd.minusWeeks(1);

        List<DailySubject> thisWeekSubjects = dailySubjectRepository.findAllByMemberIdAndDateRangeWithSubject(
                memberId, thisWeekStart, thisWeekEnd
        );
        int thisWeekSeconds = thisWeekSubjects.stream()
                .mapToInt(DailySubject::getFocusSeconds)
                .sum();

        List<DailySubject> lastWeekSubjects = dailySubjectRepository.findAllByMemberIdAndDateRangeWithSubject(
                memberId, lastWeekStart, lastWeekEnd
        );
        int lastWeekSeconds = lastWeekSubjects.stream()
                .mapToInt(DailySubject::getFocusSeconds)
                .sum();

        Map<String, Object> progressData = new HashMap<>();
        progressData.put("lastWeekMinutes", lastWeekSeconds / 60);
        progressData.put("thisWeekMinutes", thisWeekSeconds / 60);

        try {
            memberItem.updateProgressData(objectMapper.writeValueAsString(progressData));
        } catch (Exception e) {
            log.error("Error updating progressData for 과거 나와 대결 (sibling)", e);
        }
    }

    /**
     * 누적 집중의 대가 - progressData만 업데이트
     */
    private void updateWeeklyAccumulationProgress(MemberItem memberItem, Long memberId, LocalDate serviceDate) {
        LocalDate weekStart = serviceDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = serviceDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        List<DailySubject> weeklySubjects = dailySubjectRepository.findAllByMemberIdAndDateRangeWithSubject(
                memberId, weekStart, weekEnd
        );

        int totalSeconds = weeklySubjects.stream()
                .mapToInt(DailySubject::getFocusSeconds)
                .sum();

        int totalMinutes = totalSeconds / 60;
        int requiredHours = memberItem.getSelection();

        Map<String, Object> progressData = new HashMap<>();
        progressData.put("thisWeekMinutes", totalMinutes);
        progressData.put("targetHours", requiredHours);

        try {
            memberItem.updateProgressData(objectMapper.writeValueAsString(progressData));
        } catch (Exception e) {
            log.error("Error updating progressData for 누적 집중의 대가 (sibling)", e);
        }
    }

    // ========== 아이템 구매 시 초기 progressData 설정 ==========

    /**
     * 아이템 구매 시 초기 progressData 생성
     * 구매 시점의 실제 데이터를 기반으로 progressData 생성
     *
     * @param memberId 회원 ID
     * @param itemName 아이템 이름
     * @param selection 선택한 옵션 (parameter)
     * @return JSON 형식의 초기 progressData
     */
    public String createInitialProgressData(Long memberId, String itemName, Integer selection) {
        LocalDate serviceDate = getServiceDate();

        try {
            return switch (itemName) {
                case "집중력 폭발" -> createInitialConsecutiveFocusProgress(memberId, selection, serviceDate);
                case "시작 시간 사수" -> createInitialMorningStartProgress(memberId, selection, serviceDate);
                case "마지막 생존자" -> createInitialLateNightEndProgress(memberId, selection, serviceDate);
                case "휴식은 사치" -> createInitialLimitedRestProgress(memberId, selection, serviceDate);
                case "약점 극복" -> createInitialWeakestDayProgress(memberId, serviceDate);
                case "저지 불가" -> createInitialSevenDaysStreakProgress(memberId, serviceDate);
                case "과거 나와 대결" -> createInitialBeatLastWeekProgress(memberId, serviceDate);
                case "누적 집중의 대가" -> createInitialWeeklyAccumulationProgress(memberId, selection, serviceDate);
                default -> {
                    log.warn("Unknown item name for initial progressData: {}", itemName);
                    yield null;
                }
            };
        } catch (Exception e) {
            log.error("Error creating initial progressData for item: {}", itemName, e);
            return null;
        }
    }

    // ========== 아이템별 초기 progressData 생성 메서드 ==========

    private String createInitialConsecutiveFocusProgress(Long memberId, Integer selection, LocalDate serviceDate) throws Exception {
        Map<String, Object> progressData = new HashMap<>();
        progressData.put("requiredMinutes", selection);

        // 오늘의 DailyGoal에서 최대 연속 집중 시간 조회
        var dailyGoalOpt = dailyGoalRepository.findByMemberIdAndDailyGoalDate(memberId, serviceDate);
        int maxConsecutiveMinutes = dailyGoalOpt
                .map(dg -> dg.getMaxConsecutiveSeconds() / 60)
                .orElse(0);
        progressData.put("maxConsecutiveMinutes", maxConsecutiveMinutes);
        progressData.put("achievedDate", null);
        progressData.put("achievedDay", null);

        return objectMapper.writeValueAsString(progressData);
    }

    private String createInitialMorningStartProgress(Long memberId, Integer selection, LocalDate serviceDate) throws Exception {
        Map<String, Object> progressData = new HashMap<>();
        progressData.put("requiredHour", selection);

        // 오늘의 DailyGoal에서 가장 빠른 시작 시간 조회
        var dailyGoalOpt = dailyGoalRepository.findByMemberIdAndDailyGoalDate(memberId, serviceDate);
        if (dailyGoalOpt.isPresent() && dailyGoalOpt.get().getEarliestStartTime() != null) {
            progressData.put("earliestStartTime", dailyGoalOpt.get().getEarliestStartTime().format(TIME_FORMATTER));
        } else {
            progressData.put("earliestStartTime", "--:--");
        }
        progressData.put("achievedDate", null);
        progressData.put("achievedDay", null);

        return objectMapper.writeValueAsString(progressData);
    }

    private String createInitialLateNightEndProgress(Long memberId, Integer selection, LocalDate serviceDate) throws Exception {
        Map<String, Object> progressData = new HashMap<>();
        progressData.put("requiredHour", selection);

        // 오늘의 DailyGoal에서 가장 늦은 종료 시간 조회
        var dailyGoalOpt = dailyGoalRepository.findByMemberIdAndDailyGoalDate(memberId, serviceDate);
        if (dailyGoalOpt.isPresent() && dailyGoalOpt.get().getLatestEndTime() != null) {
            progressData.put("latestEndTime", dailyGoalOpt.get().getLatestEndTime().format(TIME_FORMATTER));
        } else {
            progressData.put("latestEndTime", "--:--");
        }
        progressData.put("achievedDate", null);
        progressData.put("achievedDay", null);

        return objectMapper.writeValueAsString(progressData);
    }

    private String createInitialLimitedRestProgress(Long memberId, Integer selection, LocalDate serviceDate) throws Exception {
        Map<String, Object> progressData = new HashMap<>();
        progressData.put("requiredRestHours", selection);

        // 오늘의 쉬는 시간 계산
        var dailyGoalOpt = dailyGoalRepository.findByMemberIdAndDailyGoalDate(memberId, serviceDate);
        int restMinutes = 0;
        double restHours = 0.0;

        if (dailyGoalOpt.isPresent()) {
            var dailyGoal = dailyGoalOpt.get();
            LocalTime earliestStartTime = dailyGoal.getEarliestStartTime();
            LocalTime latestEndTime = dailyGoal.getLatestEndTime();

            if (earliestStartTime != null && latestEndTime != null) {
                List<DailySubject> todaySubjects = dailySubjectRepository.findAllByMemberIdAndDateRangeWithSubject(
                        memberId, serviceDate, serviceDate
                );
                int totalFocusSeconds = todaySubjects.stream()
                        .mapToInt(DailySubject::getFocusSeconds)
                        .sum();

                long activitySeconds;
                if (latestEndTime.isBefore(earliestStartTime)) {
                    activitySeconds = (24 * 3600) - earliestStartTime.toSecondOfDay() + latestEndTime.toSecondOfDay();
                } else {
                    activitySeconds = latestEndTime.toSecondOfDay() - earliestStartTime.toSecondOfDay();
                }

                long restSeconds = Math.max(0, activitySeconds - totalFocusSeconds);
                restHours = restSeconds / 3600.0;
                restMinutes = (int) (restSeconds / 60);
            }
        }

        progressData.put("todayRestHours", Math.round(restHours * 10) / 10.0);
        progressData.put("todayRestMinutes", restMinutes);
        progressData.put("achievedDate", null);
        progressData.put("achievedDay", null);

        return objectMapper.writeValueAsString(progressData);
    }

    private String createInitialWeakestDayProgress(Long memberId, LocalDate serviceDate) throws Exception {
        LocalDate weekStart = serviceDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = serviceDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        LocalDate lastWeekStart = weekStart.minusWeeks(1);
        LocalDate lastWeekEnd = weekStart.minusWeeks(1).with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        List<DailySubject> lastWeekSubjects = dailySubjectRepository.findAllByMemberIdAndDateRangeWithSubject(
                memberId, lastWeekStart, lastWeekEnd
        );

        Map<LocalDate, Integer> lastWeekDailyMap = lastWeekSubjects.stream()
                .collect(Collectors.groupingBy(
                        DailySubject::getDate,
                        Collectors.summingInt(DailySubject::getFocusSeconds)
                ));

        // 지난 주 7일의 집중 시간 (기록 없는 날은 0으로 채움)
        Map<DayOfWeek, Integer> lastWeekByDayOfWeek = new HashMap<>();
        for (int i = 0; i < 7; i++) {
            LocalDate date = lastWeekStart.plusDays(i);
            lastWeekByDayOfWeek.put(date.getDayOfWeek(), lastWeekDailyMap.getOrDefault(date, 0));
        }

        int lastWeekMinSeconds = lastWeekByDayOfWeek.values().stream()
                .mapToInt(Integer::intValue)
                .min()
                .orElse(0);

        // 지난 주 평균 계산
        double lastWeekAverageSeconds = lastWeekByDayOfWeek.values().stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);

        DayOfWeek weakestDay = lastWeekByDayOfWeek.entrySet().stream()
                .filter(e -> e.getValue() == lastWeekMinSeconds)
                .map(Map.Entry::getKey)
                .max(Comparator.comparingInt(DayOfWeek::getValue))
                .orElse(DayOfWeek.SUNDAY);

        // 이번 주 집중 기록 조회
        List<DailySubject> thisWeekSubjects = dailySubjectRepository.findAllByMemberIdAndDateRangeWithSubject(
                memberId, weekStart, weekEnd
        );

        Map<LocalDate, Integer> thisWeekDailyMap = thisWeekSubjects.stream()
                .collect(Collectors.groupingBy(
                        DailySubject::getDate,
                        Collectors.summingInt(DailySubject::getFocusSeconds)
                ));

        // 이번 주 해당 요일 집중 시간
        LocalDate thisWeekTargetDay = weekStart.with(TemporalAdjusters.nextOrSame(weakestDay));
        int thisWeekTargetDaySeconds = thisWeekDailyMap.getOrDefault(thisWeekTargetDay, 0);
        int thisWeekTargetDayMinutes = thisWeekTargetDaySeconds / 60;

        int weakestMinutes = lastWeekMinSeconds / 60;
        int hours = weakestMinutes / 60;
        int mins = weakestMinutes % 60;

        // "일 - 0H0M" 형식으로 표시
        String displayText = DAY_OF_WEEK_SHORT.get(weakestDay) + " - " + hours + "H" + mins + "M";

        Map<String, Object> progressData = new HashMap<>();
        progressData.put("weakestDay", DAY_OF_WEEK_SHORT.get(weakestDay));
        progressData.put("weakestDayMinutes", weakestMinutes);
        progressData.put("displayText", displayText);
        progressData.put("lastWeekAverageMinutes", (int) (lastWeekAverageSeconds / 60));
        progressData.put("thisWeekTargetDayMinutes", thisWeekTargetDayMinutes);
        progressData.put("achievedDate", null);
        progressData.put("achievedDay", null);

        return objectMapper.writeValueAsString(progressData);
    }

    private String createInitialSevenDaysStreakProgress(Long memberId, LocalDate serviceDate) throws Exception {
        LocalDate weekStart = serviceDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = serviceDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        List<DailySubject> weeklySubjects = dailySubjectRepository.findAllByMemberIdAndDateRangeWithSubject(
                memberId, weekStart, weekEnd
        );

        Map<LocalDate, Integer> dailyFocusMap = weeklySubjects.stream()
                .collect(Collectors.groupingBy(
                        DailySubject::getDate,
                        Collectors.summingInt(DailySubject::getFocusSeconds)
                ));

        List<DayOfWeek> achievedDays = dailyFocusMap.entrySet().stream()
                .filter(e -> e.getValue() >= 1800)
                .map(e -> e.getKey().getDayOfWeek())
                .sorted()
                .toList();

        Map<String, Object> progressData = new HashMap<>();
        progressData.put("achievedDaysCount", achievedDays.size());

        if (achievedDays.isEmpty()) {
            progressData.put("displayText", "--");
        } else {
            DayOfWeek latestDay = achievedDays.stream()
                    .max(Comparator.comparingInt(DayOfWeek::getValue))
                    .orElse(DayOfWeek.MONDAY);
            progressData.put("displayText", DAY_OF_WEEK_KR.get(latestDay));
        }
        progressData.put("achievedDate", null);
        progressData.put("achievedDay", null);

        return objectMapper.writeValueAsString(progressData);
    }

    private String createInitialBeatLastWeekProgress(Long memberId, LocalDate serviceDate) throws Exception {
        LocalDate thisWeekStart = serviceDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate thisWeekEnd = serviceDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        LocalDate lastWeekStart = thisWeekStart.minusWeeks(1);
        LocalDate lastWeekEnd = thisWeekEnd.minusWeeks(1);

        List<DailySubject> thisWeekSubjects = dailySubjectRepository.findAllByMemberIdAndDateRangeWithSubject(
                memberId, thisWeekStart, thisWeekEnd
        );
        int thisWeekSeconds = thisWeekSubjects.stream()
                .mapToInt(DailySubject::getFocusSeconds)
                .sum();

        List<DailySubject> lastWeekSubjects = dailySubjectRepository.findAllByMemberIdAndDateRangeWithSubject(
                memberId, lastWeekStart, lastWeekEnd
        );
        int lastWeekSeconds = lastWeekSubjects.stream()
                .mapToInt(DailySubject::getFocusSeconds)
                .sum();

        Map<String, Object> progressData = new HashMap<>();
        progressData.put("lastWeekMinutes", lastWeekSeconds / 60);
        progressData.put("thisWeekMinutes", thisWeekSeconds / 60);
        progressData.put("achievedDate", null);
        progressData.put("achievedDay", null);

        return objectMapper.writeValueAsString(progressData);
    }

    private String createInitialWeeklyAccumulationProgress(Long memberId, Integer selection, LocalDate serviceDate) throws Exception {
        LocalDate weekStart = serviceDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = serviceDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        List<DailySubject> weeklySubjects = dailySubjectRepository.findAllByMemberIdAndDateRangeWithSubject(
                memberId, weekStart, weekEnd
        );

        int totalSeconds = weeklySubjects.stream()
                .mapToInt(DailySubject::getFocusSeconds)
                .sum();

        Map<String, Object> progressData = new HashMap<>();
        progressData.put("thisWeekMinutes", totalSeconds / 60);
        progressData.put("targetHours", selection);
        progressData.put("achievedDate", null);
        progressData.put("achievedDay", null);

        return objectMapper.writeValueAsString(progressData);
    }
}
