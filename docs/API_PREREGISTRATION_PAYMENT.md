# 사전예약 & 결제 API 가이드 (Front-end)

> 이 문서는 Flutter 프론트팀을 위한 API 가이드입니다.

---

## 목차
1. [사전예약 플로우](#1-사전예약-플로우)
2. [RevenueCat 연동 (인앱결제)](#3-revenuecat-연동-인앱결제)

---

## 1. 사전예약 플로우

### 전체 플로우
```
[앱 실행] → [사전예약 확인 버튼] → POST /check → [결과 표시]
                                                    ↓
                                   (사전예약 O) → POST /claim → [우편함 확인 안내]
                                   (사전예약 X) → [사전예약 안내 팝업]
```

### 1.1 사전예약 확인

**전화번호로 사전예약 여부를 확인합니다.**

```
POST /api/v1/pre-registration/check?phoneNumber={phoneNumber}
```

#### Request
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| phoneNumber | Query String | O | 사전예약 시 등록한 전화번호 (예: `01012345678`) |

#### Response
```json
{
  "message": "ok",
  "data": {
    "isPreRegistered": true,
    "isRewardClaimed": false,
    "registrationDate": "2025-10-22"
  }
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| isPreRegistered | Boolean | 사전예약 여부 |
| isRewardClaimed | Boolean | 보상 수령 완료 여부 |
| registrationDate | String | 사전예약 날짜 (yyyy-MM-dd), 미등록 시 null |

#### UI 분기 처리
```dart
if (!response.isPreRegistered) {
  // 사전예약 안됨 → 안내 팝업
  showDialog("사전예약 정보가 없습니다.");
} else if (response.isRewardClaimed) {
  // 이미 수령함 → 안내 팝업
  showDialog("이미 보상을 수령하셨습니다.");
} else {
  // 수령 가능 → claim 버튼 활성화
  showRewardClaimButton();
}
```

#### Error Cases
| HTTP Code | 상황 | 메시지 |
|-----------|------|--------|
| 400 | 다른 계정에서 이미 사용 중인 번호 | "이미 다른 계정에서 사용 중인 전화번호입니다." |
| 404 | 회원 정보 없음 | "회원 정보가 존재하지 않습니다." |

---

### 1.2 사전예약 보상 수령

**사전예약 보상을 수령합니다. 보상은 우편함으로 지급됩니다.**

```
POST /api/v1/pre-registration/claim
```

#### Request
- Body 없음 (인증 토큰만 필요)

#### Response
```json
{
  "message": "ok",
  "data": {
    "mailIds": [1, 2]
  }
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| mailIds | List<Long> | 생성된 우편 ID 목록 |

#### 지급되는 보상
| 보상 | 우편 타입 | 설명 |
|------|----------|------|
| 다이아 3,000개 | `EVENT` | 즉시 수령 가능 |
| 프리미엄 구독권 14일 | - | **RevenueCat으로 즉시 적용** (우편 아님) |
| RARE 캐릭터 선택권 | `CHARACTER_SELECTION_TICKET` | 우편 수령 시 캐릭터 선택 |

> **중요**: 프리미엄 구독권은 RevenueCat Promotional Entitlement로 즉시 적용됩니다.
> Flutter에서 RevenueCat SDK로 `CustomerInfo`를 다시 조회하면 `premium_subscription` entitlement가 활성화되어 있습니다.

#### Error Cases
| HTTP Code | 상황 | 메시지 |
|-----------|------|--------|
| 400 | /check를 먼저 호출하지 않음 | "사전예약 정보가 없습니다. 먼저 /check를 호출하세요." |
| 500 | 이미 보상 수령함 | "이미 사전예약 보상을 받으셨습니다." |

## 2. RevenueCat 연동 (인앱결제)

### 아키텍처
```
[Flutter App] ──(1)─→ [RevenueCat SDK] ──(2)─→ [App Store / Play Store]
                              │
                              ├──(3)─→ 영수증 검증 (RevenueCat 서버)
                              │
                              └──(4)─→ [Backend Webhook] → DB 업데이트
```

### Flutter에서 처리할 것

1. **RevenueCat SDK 초기화**
   - `app_user_id`에 **서버 Member ID** 설정 (로그인 후)

2. **구매 플로우**
   - RevenueCat SDK의 `purchaseProduct()` 사용
   - 서버 API 직접 호출 불필요 (Webhook으로 자동 처리)

3. **구독 상태 확인**
   - `Purchases.getCustomerInfo()` 호출
   - `customerInfo.entitlements.active["premium_subscription"]` 확인

4. **사전예약 프리미엄 확인**
   - `/claim` 호출 후 `CustomerInfo` 재조회
   - `rc_promo_premium_subscription_custom` entitlement 활성화 확인

### 주의사항
- 서버로 영수증을 직접 전송하지 마세요 (RevenueCat이 처리)
- `app_user_id`는 반드시 서버 Member ID를 사용하세요
- Sandbox/Production 환경에 맞는 API Key 사용