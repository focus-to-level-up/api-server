package com.studioedge.domain.item.business;

import com.studioedge.character.repository.MemberCharacterRepository;
import com.studioedge.character.entity.MemberCharacter;
import com.studioedge.character.exception.CharacterDefaultNotFoundException;
import com.studioedge.member.repository.MemberInfoRepository;
import com.studioedge.member.repository.MemberRepository;
import com.studioedge.member.entity.Member;
import com.studioedge.member.entity.MemberInfo;
import com.studioedge.member.exception.MemberInfoInvalidException;
import com.studioedge.member.exception.MemberNotFoundException;
import com.studioedge.item.repository.ItemDetailRepository;
import com.studioedge.item.repository.MemberItemRepository;
import com.studioedge.item.entity.ItemDetail;
import com.studioedge.item.entity.MemberItem;
import com.studioedge.item.exception.ItemNotCompletedException;
import com.studioedge.item.exception.MemberItemNotFoundException;
import com.studioedge.item.exception.RewardAlreadyReceivedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ItemRewardService {

    private final MemberItemRepository memberItemRepository;
    private final ItemDetailRepository itemDetailRepository;
    private final MemberRepository memberRepository;
    private final MemberInfoRepository memberInfoRepository;
    private final MemberCharacterRepository memberCharacterRepository;
    /**
     * 아이템 달성 보상 수령
     *
     * @param memberId 회원 ID
     * @param memberItemId 아이템 ID
     */
    public void claimReward(Long memberId, Long memberItemId) {
        // MemberItem 조회
        MemberItem memberItem = memberItemRepository.findById(memberItemId)
                .orElseThrow(MemberItemNotFoundException::new);

        // 본인 아이템인지 확인
        if (!memberItem.getMember().getId().equals(memberId)) {
            throw new MemberItemNotFoundException();
        }

        // 달성 완료 여부 확인
        if (!memberItem.getIsCompleted()) {
            throw new ItemNotCompletedException();
        }

        // 이미 보상 수령 여부 확인
        if (memberItem.getIsRewardReceived()) {
            throw new RewardAlreadyReceivedException();
        }

        // Member, MemberInfo 재조회 (Managed 상태)
        Member member = memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);
        MemberInfo memberInfo = memberInfoRepository.findByMemberId(memberId)
                .orElseThrow(MemberInfoInvalidException::new);

        // ItemDetail 조회하여 rewardLevel 가져오기
        ItemDetail itemDetail = itemDetailRepository.findByItemIdAndParameter(
                memberItem.getItem().getId(),
                memberItem.getSelection()
        ).orElseThrow(() -> new IllegalStateException("ItemDetail not found"));

        int rewardLevel = itemDetail.getRewardLevel();

        // 레벨 직접 증가
        member.levelUp(rewardLevel);
        memberInfo.totalLevelUp(rewardLevel);

        // 골드 지급 (rewardLevel 그대로)
        memberInfo.addGold(rewardLevel);

        // 보상 수령 처리
        memberItem.receiveReward();

        // 대표 캐릭터 레벨업
        MemberCharacter memberCharacter = memberCharacterRepository.findByMemberIdAndIsDefaultTrue(memberId)
                .orElseThrow(CharacterDefaultNotFoundException::new);
        memberCharacter.levelUp(rewardLevel);

        log.info("Reward claimed: memberId={}, memberItemId={}, rewardLevel={}", memberId, memberItemId, rewardLevel);
    }
}
