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
            case "집중력 폭발" -> "일시정지를 누르지 않고 연속해서 집중한 시간이 " + parameter + "분 달성하면 레벨이 오른다.\n(하루에 한 번 가능)";
            case "시작 시간 사수" -> "일어나서 집중 시작 버튼을 누른 시각이 오전 " + parameter + "시일 경우 레벨이 오른다.";
            case "마지막 생존자" -> {
                if (parameter == 0) {
                    yield "오늘의 학습 종료 버튼을 누른 시각이 자정 이후일 경우 레벨이 오른다.\n(종료 버튼을 누르지 않았다면 마지막 집중 시간이 종료 버튼을 누른 시간이 된다.)";
                }
                yield "오늘의 학습 종료 버튼을 누른 시각이 오후 " + parameter + "시일 경우 레벨이 오른다.\n(종료 버튼을 누르지 않았다면 마지막 집중 시간이 종료 버튼을 누른 시간이 된다.)";
            }
            case "휴식은 사치" -> "집중하지 않는 쉬는 시간이 " + parameter + "시간을 넘지 않을 경우 레벨이 오른다.\n(하루에 한 번 가능)";
            case "약점 극복" -> "지난 주 집중 시간이 가장 적었던 날에 평균 집중 시간 이상 집중하면 레벨이 오른다.";
            case "저지 불가" -> "하루도 쉬지 않고 1주일간 매일 집중을 30분 이상할 경우에 레벨이 오른다.";
            case "과거의 나와 대결" -> "지난주보다 주간 총 집중시간이 더 긴 경우에 레벨이 오른다.";
            case "누적 집중의 대가" -> "이번 주 총 집중 시간이 " + parameter + "시간일 경우에 레벨이 오른다.";
            default -> "미션을 달성하세요";
        };
    }
}