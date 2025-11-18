package com.studioedge.focus_to_levelup_server.domain.system.dto.response;

import java.util.Map;

/**
 * Firebase에서 조회한 사전예약 데이터
 */
public record PreRegistrationData(
        Long createdAt,
        String os,
        String ageGroup,
        Boolean isReserved,
        String phone,
        Long reservedAt,
        Integer diamonds  // Firebase의 inventory.diamonds 값 (참고용, 보상은 일괄 3000개)
) {
    /**
     * Firebase JSON 데이터를 DTO로 변환
     */
    public static PreRegistrationData from(Map<String, Object> firebaseData) {
        @SuppressWarnings("unchecked")
        Map<String, Object> reservation = (Map<String, Object>) firebaseData.get("reservation");

        @SuppressWarnings("unchecked")
        Map<String, Object> inventory = (Map<String, Object>) firebaseData.get("inventory");

        return new PreRegistrationData(
                getLongValue(firebaseData, "createdAt"),
                (String) firebaseData.get("os"),
                reservation != null ? (String) reservation.get("ageGroup") : null,
                reservation != null ? (Boolean) reservation.get("isReserved") : false,
                reservation != null ? (String) reservation.get("phone") : null,
                reservation != null ? getLongValue(reservation, "reservedAt") : null,
                inventory != null ? getIntValue(inventory, "diamonds") : null
        );
    }

    private static Long getLongValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return null;
    }

    private static Integer getIntValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return null;
    }
}
