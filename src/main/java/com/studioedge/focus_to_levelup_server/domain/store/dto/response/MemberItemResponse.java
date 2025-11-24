package com.studioedge.focus_to_levelup_server.domain.store.dto.response;

import com.studioedge.focus_to_levelup_server.domain.store.entity.MemberItem;
import com.studioedge.focus_to_levelup_server.domain.store.enums.ItemType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDate;

/**
 * 유저 아이템 상세 정보
 */
@Builder
public record MemberItemResponse(
        @Schema(description = "유저 아이템 ID", example = "1")
        Long memberItemId,

        @Schema(description = "아이템 ID", example = "1")
        Long itemId,

        @Schema(description = "아이템 이름", example = "집중력 폭발")
        String itemName,

        @Schema(description = "아이템 타입")
        ItemType itemType,

        @Schema(description = "선택한 옵션 (60, 90, 120 등)", example = "60")
        Integer selectedParameter,

        @Schema(description = "달성 완료 여부", example = "false")
        Boolean isCompleted,

        @Schema(description = "달성 완료 날짜", example = "2025-11-22")
        LocalDate completedDate,

        @Schema(description = "보상 수령 여부", example = "false")
        Boolean isRewardReceived,

        @Schema(description = "보상 레벨 (골드 지급량)", example = "1")
        Integer rewardLevel,

        @Schema(description = "달성 가이드 메시지", example = "60분 연속 집중하기")
        String guideMessage,

        @Schema(description = """
                진행 상황 데이터 (JSON 형식)
                - 집중력 폭발: {"requiredMinutes":60,"currentFocusMinutes":30,"maxConsecutiveMinutes:20"}
                - 시작 시간 사수: {"currentStartTime":"09:00","earliestStartTime":"07:00","requiredHour":8,"recordedDate":"2025-11-22"}
                - 마지막 생존자: {"currentEndTime":"20:00","latestEndTime":"22:30","requiredHour":22,"recordedDate":"2025-11-22"}
                - 달성 시 추가: "achievedDate":"2025-11-22", "achievedDay":"토요일"
                """,
                example = "{\"requiredMinutes\":60,\"currentFocusMinutes\":30,\"maxConsecutiveMinutes\":20}")
        String progressData
) {
    public static MemberItemResponse from(MemberItem memberItem, Integer rewardLevel) {
        return MemberItemResponse.builder()
                .memberItemId(memberItem.getId())
                .itemId(memberItem.getItem().getId())
                .itemName(memberItem.getItem().getName())
                .itemType(memberItem.getItem().getType())
                .selectedParameter(memberItem.getSelection())
                .isCompleted(memberItem.getIsCompleted())
                .completedDate(memberItem.getCompletedDate())
                .isRewardReceived(memberItem.getIsRewardReceived())
                .rewardLevel(rewardLevel)
                .guideMessage(generateGuideMessage(memberItem))
                .progressData(memberItem.getProgressData())
                .build();
    }

    private static String generateGuideMessage(MemberItem memberItem) {
        String itemName = memberItem.getItem().getName();
        Integer parameter = memberItem.getSelection();

        return switch (itemName) {
            case "집중력 폭발" -> parameter + "분 연속 집중하기";
            case "시작 시간 사수" -> "오전 " + parameter + "시 이전에 시작하기";
            case "마지막 생존자" -> {
                if (parameter == 0) {
                    yield "자정 이후에 종료하기";
                }
                yield "오후 " + parameter + "시 이후에 종료하기";
            }
            case "휴식은 사치" -> "하루 휴식 시간 " + parameter + "시간 미만 유지하기";
            case "약점 극복" -> "가장 약한 요일의 집중 시간을 평균 이상으로 만들기";
            case "저지 불가" -> "7일 모두 30분 이상 집중하기";
            case "과거 나와 대결" -> "이번 주 집중 시간을 지난 주보다 늘리기";
            case "누적 집중의 대가" -> "주간 누적 " + parameter + "시간 달성하기";
            default -> "미션을 달성하세요";
        };
    }
}