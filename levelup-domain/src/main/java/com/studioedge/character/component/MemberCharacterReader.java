package com.studioedge.character.component;

import com.studioedge.character.entity.MemberCharacter;
import com.studioedge.character.exception.MemberCharacterNotFoundException;
import com.studioedge.character.repository.MemberCharacterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class MemberCharacterReader {

    private final MemberCharacterRepository memberCharacterRepository;

    public boolean exist(Long memberId, Long characterId) {
        log.info("Check if member with id {} owns character with id {}", memberId, characterId);
        return memberCharacterRepository.existsByMemberIdAndCharacterId(memberId, characterId);
    }

    public MemberCharacter findOne(Long memberCharacterId) {
        log.info("Find any character owned by member with id {}", memberCharacterId);
        return memberCharacterRepository.findByIdWithAssets(memberCharacterId)
                .orElseThrow(MemberCharacterNotFoundException::new);
    }

    public MemberCharacter findOne(Long memberId, Long characterId) {
        log.info("Find character with id {} owned by member with id {}", characterId, memberId);
        return memberCharacterRepository.findByMemberIdAndCharacterId(memberId, characterId)
                .orElseThrow(MemberCharacterNotFoundException::new);
    }

    public List<MemberCharacter> findAllByMemberId(Long memberId) {
        log.info("Find all characters owned by member with id {}", memberId);
        return memberCharacterRepository.findAllByMemberIdWithCharacter(memberId);
    }

    public MemberCharacter findDefaultCharacter(Long memberId) {
        log.info("Find default character for member with id {}", memberId);
        return memberCharacterRepository.findByMemberIdAndIsDefaultTrue(memberId)
                .orElseThrow(MemberCharacterNotFoundException::new);
    }
}
