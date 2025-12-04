package com.studioedge.focus_to_levelup_server.domain.store.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studioedge.focus_to_levelup_server.domain.focus.dao.DailySubjectRepository;
import com.studioedge.focus_to_levelup_server.domain.focus.dto.request.SaveFocusRequest;
import com.studioedge.focus_to_levelup_server.domain.focus.entity.DailyGoal;
import com.studioedge.focus_to_levelup_server.domain.focus.entity.DailySubject;
import com.studioedge.focus_to_levelup_server.domain.store.entity.MemberItem;
import com.studioedge.focus_to_levelup_server.domain.store.dao.MemberItemRepository;
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

    /**
     * 집중 세션 종료 시 모든 달성 조건 체크
     *
     * @param memberId 회원 ID
     * @param request 요청온 유저의 집중 시간 정보
     * @param dailyGoal 오늘의 목표 정보 (최대 집중 시간 포함)
     */
    public void checkAchievements(Long memberId, SaveFocusRequest request, DailyGoal dailyGoal) {
        int focusSeconds = request.focusSeconds();
        LocalDateTime sessionStartTime = request.startTime();

        log.info("=== checkAchievements called: memberId={}, focusSeconds={}, startTime={}", memberId, focusSeconds, sessionStartTime);
        LocalDateTime sessionEndTime = LocalDateTime.now();
        LocalDate serviceDate = getServiceDate();

        // 미완료 아이템 조회
        List<MemberItem> incompleteMemberItems = memberItemRepository.findAllByMemberIdAndNotCompleted(memberId);
        log.info("Found {} incomplete member items", incompleteMemberItems.size());

        // 오늘 이미 달성된 아이템 ID 목록 조회
        List<MemberItem> allMemberItems = memberItemRepository.findAllByMemberIdWithItem(memberId);
        Set<Long> achievedItemIdsToday = allMemberItems.stream()
                .filter(mi -> mi.getIsCompleted() && mi.getCompletedDate() != null && mi.getCompletedDate().equals(serviceDate))
                .map(mi -> mi.getItem().getId())
                .collect(Collectors.toSet());

        for (MemberItem memberItem : incompleteMemberItems) {
            Long itemId = memberItem.getItem().getId();

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
                    case "시작 시간 사수" -> checkMorningStart(memberItem, sessionStartTime, serviceDate);
                    case "마지막 생존자" -> checkLateNightEnd(memberItem, sessionEndTime, serviceDate);
                    case "휴식은 사치" -> checkLimitedRest(memberItem, memberId, serviceDate, dailyGoal);
                    case "약점 극복" -> checkWeakestDayImprovement(memberId);
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
                    achievedItemIdsToday.add(itemId); // 달성한 아이템 ID 기록
                    log.info("Item achieved: memberId={}, itemName={}, serviceDate={}", memberId, itemName, serviceDate);
                }
            } catch (Exception e) {
                log.error("Error checking achievement for item: {}", itemName, e);
            }
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

        if (isAchieved) {
            progressData.put("achievedDate", serviceDate.format(DATE_FORMATTER));
            progressData.put("achievedDay", DAY_OF_WEEK_KR.get(serviceDate.getDayOfWeek()));
        }

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
     * 오늘 중 가장 빠른 시작 시각 추적
     */
    private boolean checkMorningStart(MemberItem memberItem, LocalDateTime sessionStartTime, LocalDate serviceDate) {
        int startHour = sessionStartTime.getHour();
        int requiredHour = memberItem.getSelection();

        // 기존 progressData 파싱
        String existingProgressData = memberItem.getProgressData();
        LocalDateTime earliestStartTime = sessionStartTime;
        String recordedDate = null;

        if (existingProgressData != null && !existingProgressData.isEmpty()) {
            try {
                Map<String, Object> existingData = objectMapper.readValue(existingProgressData, Map.class);
                recordedDate = (String) existingData.get("recordedDate");
                String earliestTimeStr = (String) existingData.get("earliestStartTime");

                // 같은 날짜이고 기존 기록이 있으면 비교
                if (serviceDate.format(DATE_FORMATTER).equals(recordedDate) && earliestTimeStr != null) {
                    LocalDateTime existingEarliestTime = LocalDateTime.parse(
                            serviceDate.format(DATE_FORMATTER) + "T" + earliestTimeStr
                    );
                    // 더 빠른 시각 선택
                    if (sessionStartTime.isBefore(existingEarliestTime)) {
                        earliestStartTime = sessionStartTime;
                    } else {
                        earliestStartTime = existingEarliestTime;
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to parse existing progressData for 시작 시간 사수", e);
            }
        }

        // progressData 업데이트
        Map<String, Object> progressData = new HashMap<>();
        progressData.put("recordedDate", serviceDate.format(DATE_FORMATTER));
        progressData.put("earliestStartTime", earliestStartTime.format(TIME_FORMATTER));
        progressData.put("currentStartTime", sessionStartTime.format(TIME_FORMATTER));
        progressData.put("requiredHour", requiredHour);

        // 달성 조건: 새벽 4시 이후 && 요구 시간보다 이른 경우
        boolean isAchieved = earliestStartTime.getHour() >= 4 && earliestStartTime.getHour() < requiredHour;

        if (isAchieved) {
            progressData.put("achievedDate", serviceDate.format(DATE_FORMATTER));
            progressData.put("achievedDay", DAY_OF_WEEK_KR.get(serviceDate.getDayOfWeek()));
        }

        try {
            memberItem.updateProgressData(objectMapper.writeValueAsString(progressData));
        } catch (Exception e) {
            log.error("Error updating progressData for 시작 시간 사수", e);
        }

        return isAchieved;
    }

    /**
     * 3. 마지막 생존자: 종료 시각 >= parameter (22시/23시/자정)
     * 오늘 중 가장 늦은 종료 시각 추적
     */
    private boolean checkLateNightEnd(MemberItem memberItem, LocalDateTime sessionEndTime, LocalDate serviceDate) {
        int endHour = sessionEndTime.getHour();
        int requiredHour = memberItem.getSelection();

        // 기존 progressData 파싱
        String existingProgressData = memberItem.getProgressData();
        LocalDateTime latestEndTime = sessionEndTime;
        String recordedDate = null;

        if (existingProgressData != null && !existingProgressData.isEmpty()) {
            try {
                Map<String, Object> existingData = objectMapper.readValue(existingProgressData, Map.class);
                recordedDate = (String) existingData.get("recordedDate");
                String latestTimeStr = (String) existingData.get("latestEndTime");

                // 같은 날짜이고 기존 기록이 있으면 비교
                if (serviceDate.format(DATE_FORMATTER).equals(recordedDate) && latestTimeStr != null) {
                    LocalDateTime existingLatestTime = LocalDateTime.parse(
                            serviceDate.format(DATE_FORMATTER) + "T" + latestTimeStr
                    );
                    // 더 늦은 시각 선택
                    if (sessionEndTime.isAfter(existingLatestTime)) {
                        latestEndTime = sessionEndTime;
                    } else {
                        latestEndTime = existingLatestTime;
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to parse existing progressData for 마지막 생존자", e);
            }
        }

        // progressData 업데이트
        Map<String, Object> progressData = new HashMap<>();
        progressData.put("recordedDate", serviceDate.format(DATE_FORMATTER));
        progressData.put("latestEndTime", latestEndTime.format(TIME_FORMATTER));
        progressData.put("currentEndTime", sessionEndTime.format(TIME_FORMATTER));
        progressData.put("requiredHour", requiredHour);

        // 달성 조건: 자정(0시)의 경우 0-4시 사이도 인정
        int latestHour = latestEndTime.getHour();
        boolean isAchieved = (requiredHour == 0 && latestHour >= 0 && latestHour < 4) ||
                             (requiredHour > 0 && latestHour >= requiredHour);

        if (isAchieved) {
            progressData.put("achievedDate", serviceDate.format(DATE_FORMATTER));
            progressData.put("achievedDay", DAY_OF_WEEK_KR.get(serviceDate.getDayOfWeek()));
        }

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
     */
    private boolean checkLimitedRest(MemberItem memberItem, Long memberId, LocalDate serviceDate, DailyGoal dailyGoal) {
        // DailyGoal에서 시작/종료 시각 조회
        LocalTime earliestStartTime = dailyGoal.getEarliestStartTime();
        LocalTime latestEndTime = dailyGoal.getLatestEndTime();

        // 시작/종료 시각 정보가 없으면 계산 불가
        if (earliestStartTime == null || latestEndTime == null) {
            return false;
        }

        // 서비스 날짜 기준 총 집중 시간 조회
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
        int requiredRestHours = memberItem.getSelection();

        // progressData 업데이트
        Map<String, Object> progressData = new HashMap<>();
        progressData.put("todayRestHours", Math.round(restHours * 10) / 10.0);
        progressData.put("todayRestMinutes", restMinutes);
        progressData.put("requiredRestHours", requiredRestHours);

        boolean isAchieved = restHours < requiredRestHours;

        if (isAchieved) {
            progressData.put("achievedDate", serviceDate.format(DATE_FORMATTER));
            progressData.put("achievedDay", DAY_OF_WEEK_KR.get(serviceDate.getDayOfWeek()));
        }

        try {
            memberItem.updateProgressData(objectMapper.writeValueAsString(progressData));
        } catch (Exception e) {
            log.error("Error updating progressData for 휴식은 사치", e);
        }

        return isAchieved;
    }

    /**
     * 5. 약점 극복: 지난 주 가장 약한 요일(들)에 이번 주 평균 이상 집중하면 달성
     * - 지난 주 데이터로 최소 집중 시간인 요일들 찾기
     * - 이번 주 해당 요일들 중 하나라도 이번 주 평균 이상이면 달성
     */
    private boolean checkWeakestDayImprovement(Long memberId) {
        LocalDate serviceDate = getServiceDate();
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

        // 지난 주 7일 모두 기록이 있어야 함
        if (lastWeekDailyMap.size() < 7) {
            return false;
        }

        // 지난 주 최소값 (가장 약한 요일)
        int lastWeekMinSeconds = lastWeekDailyMap.values().stream()
                .mapToInt(Integer::intValue)
                .min()
                .orElse(0);

        // 지난 주 최솟값을 가진 요일들 (DayOfWeek) 조회
        List<DayOfWeek> weakestDaysOfWeek = lastWeekDailyMap.entrySet().stream()
                .filter(e -> e.getValue() == lastWeekMinSeconds)
                .map(e -> e.getKey().getDayOfWeek())
                .toList();

        if (weakestDaysOfWeek.isEmpty()) {
            return false;
        }

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

        // 이번 주 평균 계산
        double thisWeekAverageSeconds = thisWeekDailyMap.values().stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);

        // 달성 조건: 약한 요일들 중 하나라도 이번 주 평균 이상이면 달성
        for (DayOfWeek weakestDayOfWeek : weakestDaysOfWeek) {
            LocalDate thisWeekTargetDay = weekStart.with(TemporalAdjusters.nextOrSame(weakestDayOfWeek));
            int thisWeekTargetDaySeconds = thisWeekDailyMap.getOrDefault(thisWeekTargetDay, 0);

            if (thisWeekTargetDaySeconds >= thisWeekAverageSeconds) {
                return true;
            }
        }

        return false;
    }

    /**
     * 6. 저지 불가: 7일 모두 30분 이상
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

        // 요일 표시 형식: 2일 이하면 "월요일, 화요일" / 3일 이상이면 "월,화,수"
        if (achievedDays.size() <= 2) {
            String daysStr = achievedDays.stream()
                    .map(DAY_OF_WEEK_KR::get)
                    .collect(Collectors.joining(", "));
            progressData.put("achievedDays", daysStr);
        } else {
            String daysStr = achievedDays.stream()
                    .map(day -> DAY_OF_WEEK_KR.get(day).substring(0, 1))
                    .collect(Collectors.joining(","));
            progressData.put("achievedDays", daysStr);
        }

        boolean isAchieved = achievedDays.size() == 7;

        if (isAchieved) {
            progressData.put("achievedDate", serviceDate.format(DATE_FORMATTER));
            progressData.put("achievedDay", DAY_OF_WEEK_KR.get(serviceDate.getDayOfWeek()));
        }

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

        // 지난 주 기록이 있어야 하고, 이번 주가 더 많아야 함
        boolean isAchieved = lastWeekSeconds > 0 && thisWeekSeconds > lastWeekSeconds;

        if (isAchieved) {
            progressData.put("achievedDate", serviceDate.format(DATE_FORMATTER));
            progressData.put("achievedDay", DAY_OF_WEEK_KR.get(serviceDate.getDayOfWeek()));
        }

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

        if (isAchieved) {
            progressData.put("achievedDate", serviceDate.format(DATE_FORMATTER));
            progressData.put("achievedDay", DAY_OF_WEEK_KR.get(serviceDate.getDayOfWeek()));
        }

        try {
            memberItem.updateProgressData(objectMapper.writeValueAsString(progressData));
        } catch (Exception e) {
            log.error("Error updating progressData for 누적 집중의 대가", e);
        }

        return isAchieved;
    }
}
