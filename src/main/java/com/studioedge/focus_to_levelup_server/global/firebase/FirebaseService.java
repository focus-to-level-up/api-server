package com.studioedge.focus_to_levelup_server.global.firebase;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import com.studioedge.focus_to_levelup_server.domain.system.dto.response.PreRegistrationData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Firebase Firestore 연동 서비스
 * 사전예약 데이터 조회
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FirebaseService {

    /**
     * Firestore에서 사전예약 데이터 조회
     *
     * @param phoneNumber 전화번호 (예: "01036624249")
     * @return 사전예약 데이터
     * @throws Exception Firestore 조회 실패 또는 데이터 없음
     */
    public PreRegistrationData getPreRegistrationData(String phoneNumber) throws Exception {
        try {
            Firestore db = FirestoreClient.getFirestore();

            // Firestore에서 reservation.phone 필드로 쿼리
            var querySnapshot = db.collection("users")
                    .whereEqualTo("reservation.phone", phoneNumber)
                    .get()
                    .get();

            if (querySnapshot.isEmpty()) {
                throw new Exception("No pre-registration data found for phone: " + phoneNumber);
            }

            // 첫 번째 매칭 문서 가져오기
            DocumentSnapshot document = querySnapshot.getDocuments().get(0);
            Map<String, Object> data = document.getData();

            if (data == null) {
                throw new Exception("Pre-registration data is null for phone: " + phoneNumber);
            }

            log.info("[FirebaseService] Pre-registration data found for phone: {} (userId: {})", phoneNumber, document.getId());
            return PreRegistrationData.from(data);

        } catch (InterruptedException | ExecutionException e) {
            log.error("[FirebaseService] Failed to fetch Firestore data for phone: {}", phoneNumber, e);
            throw new Exception("Firestore 조회 실패: " + e.getMessage(), e);
        }
    }
}
