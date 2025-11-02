package com.studioedge.focus_to_levelup_server.domain.store.dto.response;

import com.studioedge.focus_to_levelup_server.domain.store.entity.MemberItem;
import com.studioedge.focus_to_levelup_server.domain.store.enums.ItemType;
import lombok.Builder;

import java.time.LocalDate;

/**
 * 유저 아이템 상세 정보
 */
@Builder
public record MemberItemResponse(
        Long memberItemId,
        Long itemId,
        String itemName,
        ItemType itemType,
        Integer selectedParameter,   // 선택한 옵션 (60, 90, 120 등)
        Boolean isCompleted,
        LocalDate completedDate,
        Boolean isRewardReceived,
        Integer rewardLevel,         // 보상 레벨
        String guideMessage          // 달성 가이드 메시지
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