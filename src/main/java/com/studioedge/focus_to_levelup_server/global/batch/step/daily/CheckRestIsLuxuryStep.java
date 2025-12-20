package com.studioedge.focus_to_levelup_server.global.batch.step.daily;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studioedge.focus_to_levelup_server.domain.focus.dao.DailyGoalRepository;
import com.studioedge.focus_to_levelup_server.domain.focus.dao.DailySubjectRepository;
import com.studioedge.focus_to_levelup_server.domain.focus.entity.DailyGoal;
import com.studioedge.focus_to_levelup_server.domain.focus.entity.DailySubject;
import com.studioedge.focus_to_levelup_server.domain.store.dao.MemberItemRepository;
import com.studioedge.focus_to_levelup_server.domain.store.entity.MemberItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import com.studioedge.focus_to_levelup_server.global.common.AppConstants;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * "휴식은 사치" 미션 성공 판정 배치 Step
 *
 * 이 미션은 하루가 끝나기 전까지 휴식 시간이 계속 증가할 수 있으므로,
 * 새벽 4시 Daily Batch에서 전날의 최종 휴식 시간을 기준으로 성공 판정을 수행합니다.
 *
 * 대상: "오늘의 학습 종료"(보상 수령)를 하지 않은 유저만
 * - 보상 수령한 유저는 receiveDailyGoal()에서 이미 판정됨
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class CheckRestIsLuxuryStep {

    private static final String ITEM_NAME = "휴식은 사치";
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

    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;

    private final MemberItemRepository memberItemRepository;
    private final DailyGoalRepository dailyGoalRepository;
    private final DailySubjectRepository dailySubjectRepository;
    private final ObjectMapper objectMapper;

    @Bean
    public Step checkRestIsLuxury() {
        return new StepBuilder("checkRestIsLuxury", jobRepository)
                .tasklet(((contribution, chunkContext) -> {
                    log.info(">> Step: checkRestIsLuxury started.");

                    // 전날 서비스 날짜 (새벽 4시 기준)
                    LocalDate yesterday = AppConstants.getServiceDate().minusDays(1);
                    log.info(">> Checking REST_IS_LUXURY for date: {}", yesterday);

                    // "휴식은 사치" 미완료 아이템을 가진 모든 MemberItem 조회
                    List<MemberItem> targetItems = memberItemRepository.findAllNotCompletedByItemName(ITEM_NAME);
                    log.info(">> Found {} incomplete '{}' items", targetItems.size(), ITEM_NAME);

                    int achievedCount = 0;
                    int skippedCount = 0;

                    // memberId별로 그룹화하여 처리 (같은 유저의 아이템들)
                    Map<Long, List<MemberItem>> itemsByMember = targetItems.stream()
                            .collect(Collectors.groupingBy(mi -> mi.getMember().getId()));

                    for (Map.Entry<Long, List<MemberItem>> entry : itemsByMember.entrySet()) {
                        Long memberId = entry.getKey();
                        List<MemberItem> memberItems = entry.getValue();

                        try {
                            // 전날의 DailyGoal 조회
                            Optional<DailyGoal> dailyGoalOpt = dailyGoalRepository.findByMemberIdAndDailyGoalDate(memberId, yesterday);
                            if (dailyGoalOpt.isEmpty()) {
                                log.debug(">> No DailyGoal for memberId={} on {}", memberId, yesterday);
                                skippedCount += memberItems.size();
                                continue;
                            }

                            DailyGoal dailyGoal = dailyGoalOpt.get();

                            // 이미 보상 수령(학습 종료)한 유저는 스킵 (receiveDailyGoal에서 이미 판정됨)
                            if (dailyGoal.getIsReceived()) {
                                log.debug(">> DailyGoal already received for memberId={} on {}", memberId, yesterday);
                                skippedCount += memberItems.size();
                                continue;
                            }

                            LocalTime earliestStartTime = dailyGoal.getEarliestStartTime();
                            LocalTime latestEndTime = dailyGoal.getLatestEndTime();

                            // 시작/종료 시각 정보가 없으면 스킵
                            if (earliestStartTime == null || latestEndTime == null) {
                                log.debug(">> No start/end time for memberId={} on {}", memberId, yesterday);
                                skippedCount += memberItems.size();
                                continue;
                            }

                            // 전날 총 집중 시간 조회
                            List<DailySubject> yesterdaySubjects = dailySubjectRepository.findAllByMemberIdAndDateRangeWithSubject(
                                    memberId, yesterday, yesterday
                            );

                            int totalFocusSeconds = yesterdaySubjects.stream()
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

                            // 오늘 이미 달성된 아이템 ID 추적 (같은 아이템은 하루에 1개만 달성)
                            Set<Long> achievedItemIdsToday = new HashSet<>();

                            // 각 MemberItem에 대해 성공 판정
                            for (MemberItem memberItem : memberItems) {
                                Long itemId = memberItem.getItem().getId();

                                // 이미 오늘 같은 itemId가 달성되었으면 스킵
                                if (achievedItemIdsToday.contains(itemId)) {
                                    continue;
                                }

                                int requiredRestHours = memberItem.getSelection();
                                boolean isAchieved = restHours < requiredRestHours;

                                if (isAchieved) {
                                    memberItem.complete(yesterday);
                                    achievedItemIdsToday.add(itemId);
                                    achievedCount++;

                                    // progressData 업데이트
                                    updateProgressData(memberItem, restHours, requiredRestHours, yesterday);

                                    log.info(">> REST_IS_LUXURY achieved: memberId={}, selection={}h, restHours={:.1f}h",
                                            memberId, requiredRestHours, restHours);
                                }
                            }

                        } catch (Exception e) {
                            log.error(">> Error processing memberId={}", memberId, e);
                        }
                    }

                    log.info(">> Step: checkRestIsLuxury finished. achieved={}, skipped={}", achievedCount, skippedCount);
                    return RepeatStatus.FINISHED;
                }), platformTransactionManager)
                .build();
    }

    private void updateProgressData(MemberItem memberItem, double restHours, int requiredRestHours, LocalDate date) {
        try {
            int restMinutes = (int) (restHours * 60);

            Map<String, Object> progressData = new HashMap<>();
            progressData.put("todayRestHours", Math.round(restHours * 10) / 10.0);
            progressData.put("todayRestMinutes", restMinutes);
            progressData.put("requiredRestHours", requiredRestHours);
            progressData.put("achievedDate", date.format(DATE_FORMATTER));
            progressData.put("achievedDay", DAY_OF_WEEK_KR.get(date.getDayOfWeek()));

            memberItem.updateProgressData(objectMapper.writeValueAsString(progressData));
        } catch (Exception e) {
            log.error(">> Error updating progressData for memberItemId={}", memberItem.getId(), e);
        }
    }
}
