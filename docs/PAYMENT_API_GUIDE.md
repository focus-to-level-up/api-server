# Payment API 사용 가이드 (프론트엔드용)

## 📱 개요

Focus to Level Up 서버의 인앱결제 API를 사용하는 방법을 설명합니다.

### 지원 플랫폼
- ✅ **Apple App Store** (iOS)
- ✅ **Google Play Store** (Android)

### 지원 상품
- **기본 구독권** (BASIC_SUBSCRIPTION): 30일
- **프리미엄 구독권** (PREMIUM_SUBSCRIPTION): 30일
- **다이아 팩** (DIAMOND_PACK): 소모성 아이템

---

## 🔄 결제 플로우

```
1. GET /api/v1/products
   → 상품 목록 조회

2. 플랫폼별 결제 진행
   → iOS: StoreKit2
   → Android: Google Play Billing

3. POST /api/v1/purchases
   → 영수증 검증 및 보상 지급

4. UI 업데이트
   → 구독권 활성화, 다이아 충전 완료
```

---

## 📋 API 엔드포인트

### 1. 상품 목록 조회

**GET** `/api/v1/products`

```http
GET /api/v1/products HTTP/1.1
Authorization: Bearer {accessToken}
```

**Response (200 OK)**
```json
{
  "status": 200,
  "message": "OK",
  "data": {
    "products": [
      {
        "id": 1,
        "name": "기본 구독권",
        "description": "한 달 동안 기본 기능을 이용할 수 있어요",
        "price": 4900,
        "type": "BASIC_SUBSCRIPTION",
        "diamondReward": 500,
        "isActive": true
      }
    ],
    "totalCount": 3
  }
}
```

---

### 2. 인앱결제 구매

**POST** `/api/v1/purchases`

#### Request Body

**Apple (iOS)**
```json
{
  "productId": 1,
  "platform": "APPLE",
  "receiptData": "eyJhbGciOiJIUzI1NiJ9..."
}
```

**Google (Android)**
```json
{
  "productId": 1,
  "platform": "GOOGLE",
  "purchaseToken": "abcdefghijklmnop...",
  "googleProductId": "premium_subscription"
}
```

#### Response (201 Created)
```json
{
  "status": 201,
  "message": "Created",
  "data": {
    "paymentLogId": 123,
    "productName": "기본 구독권",
    "paidAmount": 4900,
    "diamondRewarded": 500,
    "bonusTicketsRewarded": 5,
    "subscriptionCreated": true,
    "platform": "APPLE",
    "status": "COMPLETED",
    "purchasedAt": "2025-11-23T12:34:56"
  }
}
```

## 🧪 테스트 방법

### 1. Mock 모드 (로컬/개발 환경)

서버가 Mock 모드로 실행 중이면 영수증 검증을 스킵합니다.

```dart
// Mock 영수증 (아무 문자열이나 사용 가능)
final response = await dio.post(
  '/api/v1/purchases',
  data: {
    'productId': 1,
    'platform': 'APPLE',
    'receiptData': 'mock-receipt-12345',
  },
);
// → 201 Created (성공)
```

### 2. Sandbox 환경 (실제 영수증 테스트)

#### iOS Sandbox
1. App Store Connect → Users and Access → Sandbox Testers에서 계정 생성
2. 기기에서 로그아웃 후 Sandbox 계정으로 로그인
3. 앱에서 결제 진행 (실제 결제 안 됨)

#### Android Sandbox
1. Google Play Console → License Testing에서 계정 추가
2. Internal Testing 트랙에 앱 업로드
3. 테스트 계정으로 앱 다운로드 및 결제 진행

---

## 📊 Response 데이터 설명

### PurchaseResponse

```json
{
  "paymentLogId": 123,              // 결제 내역 ID
  "productName": "기본 구독권",      // 상품 이름
  "paidAmount": 4900,               // 실제 결제 금액
  "diamondRewarded": 500,           // 지급된 다이아 (0이면 미지급)
  "bonusTicketsRewarded": 5,        // 지급된 보너스 티켓 (0이면 미지급)
  "subscriptionCreated": true,      // 구독권 생성 여부
  "platform": "APPLE",              // APPLE 또는 GOOGLE
  "status": "COMPLETED",            // COMPLETED, REFUNDED
  "purchasedAt": "2025-11-23T12:34:56"
}
```