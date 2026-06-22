package com.studioedge.character.component;

import com.studioedge.character.entity.MemberCharacter;
import com.studioedge.character.repository.MemberCharacterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@RequiredArgsConstructor
@Slf4j
public class MemberCharacterWriter {

    private final MemberCharacterRepository memberCharacterRepository;

    public MemberCharacter save(MemberCharacter memberCharacter) {
        log.info("Save member character new");
        return memberCharacterRepository.save(memberCharacter);
    }
}
