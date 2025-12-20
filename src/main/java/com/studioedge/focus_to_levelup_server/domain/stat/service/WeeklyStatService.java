package com.studioedge.focus_to_levelup_server.domain.stat.service;

import com.studioedge.focus_to_levelup_server.domain.character.dao.CharacterImageRepository;
import com.studioedge.focus_to_levelup_server.domain.character.dao.MemberCharacterRepository;
import com.studioedge.focus_to_levelup_server.domain.character.entity.CharacterImage;
import com.studioedge.focus_to_levelup_server.domain.character.entity.MemberCharacter;
import com.studioedge.focus_to_levelup_server.domain.character.enums.CharacterImageType;
import com.studioedge.focus_to_levelup_server.domain.character.exception.CharacterDefaultNotFoundException;
import com.studioedge.focus_to_levelup_server.domain.focus.dao.DailyGoalRepository;
import com.studioedge.focus_to_levelup_server.domain.focus.dao.DailySubjectRepository;
import com.studioedge.focus_to_levelup_server.domain.focus.entity.DailyGoal;
import com.studioedge.focus_to_levelup_server.domain.focus.entity.DailySubject;
import com.studioedge.focus_to_levelup_server.domain.focus.entity.Subject;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberInfoRepository;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.member.entity.MemberInfo;
import com.studioedge.focus_to_levelup_server.domain.member.exception.InvalidMemberException;
import com.studioedge.focus_to_levelup_server.domain.stat.dao.WeeklyStatRepository;
import com.studioedge.focus_to_levelup_server.domain.stat.dao.WeeklySubjectStatRepository;
import com.studioedge.focus_to_levelup_server.domain.stat.dto.SubjectStatResponse;
import com.studioedge.focus_to_levelup_server.domain.stat.dto.WeeklyStatListResponse;
import com.studioedge.focus_to_levelup_server.domain.stat.dto.WeeklyStatResponse;
import com.studioedge.focus_to_levelup_server.domain.stat.entity.WeeklyStat;
import com.studioedge.focus_to_levelup_server.domain.stat.entity.WeeklySubjectStat;
import com.studioedge.focus_to_levelup_server.global.common.AppConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WeeklyStatService {

    private final WeeklyStatRepository weeklyStatRepository;
    private final DailyGoalRepository dailyGoalRepository;
    private final MemberInfoRepository memberInfoRepository;
    private final WeeklySubjectStatRepository weeklySubjectStatRepository;
    private final DailySubjectRepository dailySubjectRepository;
    private final MemberCharacterRepository memberCharacterRepository;
    private final CharacterImageRepository characterImageRepository;

    @Transactional(readOnly = true)
    public WeeklyStatListResponse getWeeklyStats(Long memberId, int year, int month) {
        LocalDate serviceDate = AppConstants.getServiceDate();
        LocalDate startOfThisWeek = serviceDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        // [수정] 1. 조회할 월의 '진짜' 시작/종료 범위 계산
        LocalDate firstDayOfMonth = LocalDate.of(year, month, 1);
        LocalDate lastDayOfMonth = firstDayOfMonth.with(TemporalAdjusters.lastDayOfMonth());

        // e.g., 11월 1일(토) -> 10월 27일(월)
        LocalDate queryStartDate = firstDayOfMonth.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        // e.g., 11월 30일(일) -> 11월 30일(일)
        LocalDate queryEndDate = lastDayOfMonth.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));


        // 2. [집계 데이터] "이번 주"를 제외한 모든 'WeeklyStat' 조회
        List<WeeklyStat> weeklyStats = weeklyStatRepository.findAllByMemberIdAndDateRange(
                memberId, queryStartDate, startOfThisWeek.minusDays(1)
        );
        Map<LocalDate, WeeklyStat> weeklyStatMap = weeklyStats.stream()
                .collect(Collectors.toMap(WeeklyStat::getStartDate, stat -> stat));

        // 3. [상세 데이터 준비] 조회 범위 전체의 'DailyGoal'을 한 번에 조회 (N+1 방지 & 그래프용 일별 데이터)
        List<DailyGoal> allDailyGoals = dailyGoalRepository.findAllByMemberIdAndDailyGoalDateBetween(
                memberId, queryStartDate, queryEndDate
        );
        Map<LocalDate, Integer> dailySecondsMap = allDailyGoals.stream()
                .collect(Collectors.toMap(
                        DailyGoal::getDailyGoalDate,
                        DailyGoal::getCurrentSeconds
                ));

        // 4. [핵심 수정] 캘린더 기준으로 주차별 데이터 생성 (빈 주차도 포함)
        List<WeeklyStatResponse> responses = new ArrayList<>();
        LocalDate currentMonday = queryStartDate;

        while (!currentMonday.isAfter(queryEndDate)) {
            // 현재 반복중인 주차의 범위
            LocalDate currentSunday = currentMonday.plusDays(6);

            // 4-1. 이번 주(This Week)인 경우 -> 실시간 집계
            if (currentMonday.isEqual(startOfThisWeek)) {
                responses.add(createCurrentWeekStat(memberId, currentMonday, currentSunday, dailySecondsMap));
            }
            // 4-2. 과거 주차인 경우 -> DB 데이터 사용 (없으면 0으로 채움)
            else if (currentMonday.isBefore(startOfThisWeek)) {
                WeeklyStat stat = weeklyStatMap.get(currentMonday);
                if (stat != null) {
                    // DB에 데이터가 있는 경우
                    List<Integer> secondsList = createDailySecondsList(currentMonday, dailySecondsMap);
                    responses.add(WeeklyStatResponse.of(stat, secondsList));
                } else {
                    // DB에 데이터가 없는 경우 (누락된 주차) -> 빈 객체 생성
                    responses.add(createEmptyWeekStat(currentMonday, currentSunday));
                }
            }
            // (미래 주차는 포함하지 않음, 필요하다면 else if로 추가 가능)

            // 다음 주로 이동
            currentMonday = currentMonday.plusWeeks(1);
        }

        // 한 달동안의 총 집중 시간 합
        int totalFocusMinutes = responses.stream()
                .mapToInt(WeeklyStatResponse::totalFocusMinutes)
                .sum();

        return WeeklyStatListResponse.of(responses, totalFocusMinutes);
    }

    @Transactional(readOnly = true)
    public List<SubjectStatResponse> getSubjectStatsByPeriod(Member member, LocalDate startDate, LocalDate endDate) {

        LocalDate serviceDate = AppConstants.getServiceDate();
        LocalDate startOfThisWeek = serviceDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        Map<Subject, Integer> totalSecondsPerSubject = new HashMap<>();

        // Case 1: 요청 기간이 완전히 "과거(지난주 이전)"인 경우 -> 집계 테이블 사용
        if (endDate.isBefore(startOfThisWeek)) {
            List<WeeklySubjectStat> pastStats = weeklySubjectStatRepository
                    .findAllByMemberIdAndDateRangeWithSubject(member.getId(), startDate, endDate);

            for (WeeklySubjectStat stat : pastStats) {
                totalSecondsPerSubject.merge(stat.getSubject(), stat.getTotalMinutes() * 60, Integer::sum);
            }
        }
        // Case 2: 요청 기간이 "이번 주"인 경우 -> 실시간 테이블 사용
        else {
            List<DailySubject> realtimeStats = dailySubjectRepository
                    .findAllByMemberIdAndDateRangeWithSubject(member.getId(), startDate, endDate);

            for (DailySubject stat : realtimeStats) {
                int seconds = stat.getFocusSeconds();
                totalSecondsPerSubject.merge(stat.getSubject(), seconds, Integer::sum);
            }
        }

        return convertToSubjectStatResponse(totalSecondsPerSubject);
    }

    // ----------------------------- PRIVATE METHOD ---------------------------------

    // [Helper] 이번 주 실시간 통계 생성
    private WeeklyStatResponse createCurrentWeekStat(Long memberId, LocalDate startData, LocalDate endDate, Map<LocalDate, Integer> dailySecondsMap) {
        // 일별 데이터 리스트 생성
        List<Integer> secondsList = createDailySecondsList(startData, dailySecondsMap);
        int totalMinutes = secondsList.stream().mapToInt(s -> s / 60).sum();

        // 유저 정보 조회 (레벨, 이미지)
        MemberInfo memberInfo = memberInfoRepository.findByMemberId(memberId)
                .orElseThrow(InvalidMemberException::new);
        MemberCharacter memberCharacter = memberCharacterRepository.findByMemberIdAndIsDefaultTrue(memberId)
                .orElseThrow(CharacterDefaultNotFoundException::new);
        String imageUrl = characterImageRepository.findByCharacterIdAndEvolutionAndImageType(
                        memberCharacter.getCharacter().getId(),
                        memberCharacter.getDefaultEvolution(),
                        CharacterImageType.PICTURE
                )
                .map(CharacterImage::getImageUrl)
                .orElse(null);
        return WeeklyStatResponse.of(
                startData,
                endDate,
                totalMinutes,
                memberInfo.getMember().getCurrentLevel(),
                imageUrl,
                secondsList
        );
    }

    // [Helper] 데이터가 없는 주차 생성 (0으로 채움)
    private WeeklyStatResponse createEmptyWeekStat(LocalDate startDate, LocalDate endDate) {
        return WeeklyStatResponse.of(
                startDate,
                endDate,
                0,
                0, // 혹은 1 (기본 레벨)
                null, // 기본 이미지 URL 혹은 null
                List.of(0, 0, 0, 0, 0, 0, 0)
        );
    }

    private List<SubjectStatResponse> convertToSubjectStatResponse(Map<Subject, Integer> totalSecondsPerSubject) {
        double totalAllSubjectsSeconds = totalSecondsPerSubject.values().stream()
                .mapToDouble(Integer::doubleValue)
                .sum();

        return totalSecondsPerSubject.entrySet().stream()
                .map(entry -> SubjectStatResponse.of(
                        entry.getKey(),
                        entry.getValue(),
                        totalAllSubjectsSeconds
                ))
                .sorted(Comparator.comparing(SubjectStatResponse::totalSeconds).reversed())
                .collect(Collectors.toList());
    }

    /**
     * 시작일(월요일)로부터 7일간의 공부 시간(초) 리스트를 생성합니다.
     * 데이터가 없는 날은 0으로 채웁니다.
     * @return [월, 화, 수, 목, 금, 토, 일] 순서의 초 단위 시간 리스트
     */
    private List<Integer> createDailySecondsList(LocalDate startDate, Map<LocalDate, Integer> dataMap) {
        List<Integer> secondsList = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate date = startDate.plusDays(i);
            Integer seconds = dataMap.getOrDefault(date, 0);
            secondsList.add(seconds); // 분 -> 초 변환 (DTO 요구사항에 맞춤)
        }
        return secondsList;
    }
}
