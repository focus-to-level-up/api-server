package com.studioedge.focus_to_levelup_server.domain.stat.dto;

import com.studioedge.focus_to_levelup_server.domain.focus.entity.Subject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record SubjectStatResponse(
        @Schema(description = "과목 PK", example = "1")
        Long subjectId,
        @Schema(description = "과목 이름", example = "국어")
        String subjectName,
        @Schema(description = "과목 색상 헥스코드", example = "#FF5733")
        String color,
        @Schema(description = "총 학습 시간(분)", example = "120")
        Integer totalMinutes,
        @Schema(description = "전체 시간 대비 비율(%)", example = "35.5")
        Double percentage
) {
    /**
     * 집계된 정보로부터 DTO를 생성합니다.
     */
    public static SubjectStatResponse of(Subject subject, Integer totalMinutes, double totalAllSubjectsMinutes) {

        // 0으로 나누기 방지
        double percentage = (totalAllSubjectsMinutes == 0) ? 0.0 :
                (double) totalMinutes / totalAllSubjectsMinutes * 100.0;

        // 소수점 2자리까지 반올림
        percentage = Math.round(percentage * 100.0) / 100.0;

        return SubjectStatResponse.builder()
                .subjectId(subject.getId())
                .subjectName(subject.getName())
                .color(subject.getColor())
                .totalMinutes(totalMinutes)
                .percentage(percentage)
                .build();
    }
}
