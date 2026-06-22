package com.studioedge.domain.character.business;

import com.studioedge.member.component.MemberInfoReader;
import com.studioedge.member.entity.MemberInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TrainingRewardQueryService {

    private final MemberInfoReader memberInfoReader;

    /**
     * 수령 가능한 다이아 조회
     */
    public int getClaimableDiamond(Long memberId) {
        MemberInfo memberInfo = memberInfoReader.findOne(memberId);
        return memberInfo.getTrainingReward() / 60;
    }

    /**
     * 현재 적립된 보상 조회
     */
    public int getAccumulatedReward(Long memberId) {
        MemberInfo memberInfo = memberInfoReader.findOne(memberId);
        return memberInfo.getTrainingReward();
    }
}
