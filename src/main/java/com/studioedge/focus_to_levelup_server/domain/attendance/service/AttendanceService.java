package com.studioedge.focus_to_levelup_server.domain.attendance.service;

import com.studioedge.focus_to_levelup_server.domain.attendance.dao.AttendanceRepository;
import com.studioedge.focus_to_levelup_server.domain.attendance.dto.AttendanceCheckResponse;
import com.studioedge.focus_to_levelup_server.domain.attendance.dto.AttendanceInfoResponse;
import com.studioedge.focus_to_levelup_server.domain.attendance.entity.Attendance;
import com.studioedge.focus_to_levelup_server.domain.attendance.enums.AttendanceCycleReward;
import com.studioedge.focus_to_levelup_server.domain.attendance.exception.AttendanceAlreadyCheckedException;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberInfoRepository;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberRepository;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.member.entity.MemberInfo;
import com.studioedge.focus_to_levelup_server.domain.member.exception.InvalidMemberException;
import com.studioedge.focus_to_levelup_server.domain.member.exception.MemberNotFoundException;
import com.studioedge.focus_to_levelup_server.domain.payment.dao.SubscriptionRepository;
import com.studioedge.focus_to_levelup_server.domain.payment.entity.Subscription;
import com.studioedge.focus_to_levelup_server.domain.payment.enums.SubscriptionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static com.studioedge.focus_to_levelup_server.global.common.AppConstants.getServiceDate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final MemberRepository memberRepository;
    private final MemberInfoRepository memberInfoRepository;
    private final SubscriptionRepository subscriptionRepository;

    private static final int JACKPOT_DAYS = 50;
    private static final int JACKPOT_REWARD = 500;

    /**
     * 출석 현황 조회
     */
    public AttendanceInfoResponse getAttendanceInfo(Long memberId) {
        LocalDate today = getServiceDate();
        Member member = memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);

        // 데이터 없으면 임시 객체 (consecutiveDays = 0)
        Attendance attendance = attendanceRepository.findByMemberId(memberId)
                .orElseGet(() -> Attendance.builder().member(member).build());

        // 출석의 연속여부를 판단. 리셋 여부를 확인
        boolean needsReset = shouldResetConsecutive(attendance, today);
        boolean checkedToday = today.equals(attendance.getLastAttendanceDate());
        boolean isVip = isVipMember(member);

        // 변경된 DTO 메서드 호출
        return AttendanceInfoResponse.of(attendance, checkedToday, isVip, needsReset);
    }

    /**
     * 출석 체크 실행
     */
    @Transactional
    public AttendanceCheckResponse checkIn(Long memberId) {
        LocalDate today = getServiceDate();
        Member member = memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);
        MemberInfo memberInfo = memberInfoRepository.findByMemberId(memberId)
                .orElseThrow(InvalidMemberException::new);
        Attendance attendance = attendanceRepository.findByMemberId(memberId)
                .orElseGet(() -> {
                    Attendance newAtt = Attendance.builder().member(member).build();
                    return attendanceRepository.save(newAtt);
                });

        // 1. 이미 출석했는지 확인
        if (today.equals(attendance.getLastAttendanceDate())) {
            throw new AttendanceAlreadyCheckedException();
        }

        // 2. 연속 출석 리셋 체크 (어제 안 왔으면 초기화)
        if (shouldResetConsecutive(attendance, today)) {
            attendance.resetConsecutiveDays();
        }

        // 3. 출석 처리
        attendance.checkIn(today);

        // 4. 보상 계산 (변경된 7일 주기 로직)
        int currentCycleDay = (attendance.getConsecutiveDays() - 1) % 7 + 1; // 1~7
        int totalReward = calculateReward(currentCycleDay, member);
        boolean isJackpot = false;

        // 5. 잭팟 보상
        if (attendance.getConsecutiveDays() > 0 && attendance.getConsecutiveDays() % JACKPOT_DAYS == 0) {
            totalReward += JACKPOT_REWARD;
            isJackpot = true;
        }

        // 6. 재화 지급
        memberInfo.addDiamond(totalReward);

        String message = isJackpot
                ? "축하합니다! " + attendance.getConsecutiveDays() + "일 연속 달성! 잭팟 보상을 받았습니다!"
                : "출석체크 완료! " + currentCycleDay + "일차 보상을 받았습니다.";

        return AttendanceCheckResponse.builder()
                .receivedDiamond(totalReward)
                .consecutiveDays(attendance.getConsecutiveDays())
                .isJackpot(isJackpot)
                .message(message)
                .build();
    }

    // --- Helper Methods ---

    // 연속 출석이 끊겼는지 확인 ("어제" 출석 안 했으면 리셋 대상)
    private boolean shouldResetConsecutive(Attendance attendance, LocalDate today) {
        if (attendance.getLastAttendanceDate() == null) return false; // 신규는 리셋 아님

        // 이미 오늘 출석했다면 리셋 아님
        if (today.equals(attendance.getLastAttendanceDate())) return false;

        LocalDate yesterday = today.minusDays(1);
        // 마지막 출석일이 어제가 아니면 끊긴 것 (예: 그저께)
        return !yesterday.equals(attendance.getLastAttendanceDate());
    }

    private int calculateReward(int dayOfCycle, Member member) {
        AttendanceCycleReward cycleReward = AttendanceCycleReward.findByDayOfCycle(dayOfCycle);
        int reward = cycleReward.getReward();

        // VIP 2배 적용
        if (isVipMember(member)) {
            reward *= 2;
        }
        return reward;
    }

    private boolean isVipMember(Member member) {
        // SubscriptionType이 NONE이 아니면 VIP로 간주
        Subscription subscription = subscriptionRepository
                .findByMemberId(member.getId())
                .orElse(null);
        return subscription != null && !subscription.getType().equals(SubscriptionType.NONE);
    }
}
