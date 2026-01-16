package com.studioedge.focus_to_levelup_server.domain.attendance.entity;

import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "attendances")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Attendance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attendance_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // 연속 출석 일수
    @Column(nullable = false)
    private Integer consecutiveDays;

    // 마지막 출석 날짜 (04:00 기준 보정된 날짜)
    private LocalDate lastAttendanceDate;

    @Builder
    public Attendance(Member member) {
        this.member = member;
        this.consecutiveDays = 0;
    }

    // --- 비즈니스 로직 ---
    /**
     * 출석 체크 수행
     * - 날짜 갱신
     * - 연속 일수 +1
     */
    public void checkIn(LocalDate today) {
        this.lastAttendanceDate = today;
        this.consecutiveDays++;
    }

    /**
     * 연속 출석 초기화
     * - 어제 출석하지 않아 연속이 끊긴 경우 호출
     */
    public void resetConsecutiveDays() {
        this.consecutiveDays = 0;
    }
}
