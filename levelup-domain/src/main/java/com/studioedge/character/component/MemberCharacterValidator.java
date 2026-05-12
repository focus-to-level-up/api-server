package com.studioedge.character.component;

import com.studioedge.character.exception.CharacterSlotFullException;
import com.studioedge.character.repository.MemberCharacterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class MemberCharacterValidator {

    private final MemberCharacterRepository memberCharacterRepository;

    // 훈련소 캐릭터 배치 우선순위: 3 → 4 → 1 → 7 → 5 → 2 → 6
    private static final int POSITION_PRIORITY[] = {3, 4, 1, 7, 5, 2, 6};

    /**
     * 훈련소 캐릭터 배치 위치 자동 결정
     */
    public Integer assignFloor(Long memberId) {
        for (int position : POSITION_PRIORITY) {
            if (memberCharacterRepository.countByMemberIdAndFloor(memberId, position) == 0) {
                return position;
            }
        }
        throw new CharacterSlotFullException();
    }
}
