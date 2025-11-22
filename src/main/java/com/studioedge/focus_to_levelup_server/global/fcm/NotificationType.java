package com.studioedge.focus_to_levelup_server.global.fcm;

/**
 * FCM 푸시 알림 유형
 */
public enum NotificationType {
    /**
     * 주간 보상 알림 (매주 월요일)
     * - 메시지: "지난주의 다이아 보상을 획득하세요!"
     * - 액션: 앱 메인 화면
     */
    WEEKLY_REWARD("지난주의 다이아 보상을 획득하세요!", "HOME"),

    /**
     * 1일 미접속 알림
     * - 메시지: "{닉네임}님이 안들어와서 화가 났어요"
     * - 액션: 앱 실행
     */
    INACTIVE_1DAY("%s님이 안들어와서 화가 났어요", "HOME"),

    /**
     * 3일 미접속 알림
     * - 메시지: "{닉네임}님을 기다리다가 누군지 까먹을 것 같다고 하네요"
     * - 액션: 앱 실행
     */
    INACTIVE_3DAY("%s님을 기다리다가 누군지 까먹을 것 같다고 하네요", "HOME"),

    /**
     * 길드 집중 요청 알림
     * - 메시지: "{길드원 닉네임}이 집중을 요청했어요!"
     * - 액션: 타이머 화면으로 이동
     */
    GUILD_FOCUS_REQUEST("%s이 집중을 요청했어요!", "TIMER");

    private final String messageTemplate;
    private final String targetScreen; // 클릭 시 이동할 화면 (Flutter 라우트)

    NotificationType(String messageTemplate, String targetScreen) {
        this.messageTemplate = messageTemplate;
        this.targetScreen = targetScreen;
    }

    /**
     * 메시지 템플릿에 파라미터를 삽입하여 최종 메시지 생성
     *
     * @param args 메시지 파라미터 (예: 닉네임)
     * @return 포맷팅된 메시지
     */
    public String formatMessage(String... args) {
        if (args == null || args.length == 0) {
            return messageTemplate;
        }
        return String.format(messageTemplate, (Object[]) args);
    }

    public String getMessageTemplate() {
        return messageTemplate;
    }

    public String getTargetScreen() {
        return targetScreen;
    }
}
