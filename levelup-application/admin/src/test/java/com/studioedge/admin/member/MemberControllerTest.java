package com.studioedge.admin.member;

import com.studioedge.admin.member.dto.MemberResponse;
import com.studioedge.admin.member.dto.UpdateNicknameRequest;
import com.studioedge.admin.ranking.RankingService;
import com.studioedge.member.enums.MemberStatus;
import com.studioedge.member.enums.SocialType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.ui.ConcurrentModel;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MemberControllerTest {

    private final MemberService memberService = mock(MemberService.class);
    private final RankingService rankingService = mock(RankingService.class);
    private final Principal principal = () -> "operator";
    private MemberController controller;

    @BeforeEach
    void setUp() {
        controller = new MemberController(memberService, rankingService);
    }

    @Test
    void rendersSearchResultsAndSelectedMember() {
        MemberResponse member = member(1L, MemberStatus.ACTIVE);
        when(memberService.searchMembers(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(member)));
        when(memberService.getMemberById(1L)).thenReturn(member);

        ConcurrentModel model = new ConcurrentModel();
        String view = controller.members(principal, "NICKNAME", "focus", 0, 1L, null, model);

        assertThat(view).isEqualTo("members/index");
        assertThat(model.getAttribute("currentAdmin")).isEqualTo("operator");
        assertThat(model.getAttribute("memberPage")).isEqualTo(new PageImpl<>(List.of(member)));
        assertThat(model.getAttribute("selectedMember")).isEqualTo(member);
    }

    @Test
    void updatesNicknameAndPreservesSearchCondition() {
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();
        UpdateNicknameRequest request = new UpdateNicknameRequest("new-name");
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(request, "nicknameRequest");

        String view = controller.updateNickname(
                1L,
                request,
                bindingResult,
                "NICKNAME",
                "focus",
                0,
                LocalDate.of(2026, 6, 9),
                redirectAttributes
        );

        verify(memberService).updateNickname(1L, "new-name");
        assertThat(view).isEqualTo("redirect:/members?type=NICKNAME&keyword=focus&memberId=1&endDate=2026-06-09");
        assertThat(redirectAttributes.getFlashAttributes().get("message")).isEqualTo("닉네임을 변경했습니다.");
    }

    @Test
    void excludesMemberFromRanking() {
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        String view = controller.excludeFromRanking(1L, "ID", "1", 0, null, redirectAttributes);

        verify(rankingService).excludeMemberFromRanking(1L);
        assertThat(view).isEqualTo("redirect:/members?type=ID&keyword=1&memberId=1");
    }

    @Test
    void requestsThirtyMembersSortedByNewestId() {
        MemberResponse member = member(1L, MemberStatus.ACTIVE);
        when(memberService.searchMembers(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(member)));

        ConcurrentModel model = new ConcurrentModel();
        controller.members(principal, "NICKNAME", "a", 2, null, null, model);

        verify(memberService).searchMembers(
                eq("NICKNAME"),
                eq("a"),
                org.mockito.ArgumentMatchers.argThat(pageable ->
                        pageable.getPageNumber() == 2
                                && pageable.getPageSize() == 30
                                && pageable.getSort().getOrderFor("id").isDescending())
        );
    }

    @Test
    void redirectsToMemberWithFlashErrorWhenRankingBanIsNotAllowed() {
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();
        when(rankingService.excludeMemberFromRanking(1L))
                .thenThrow(new InvalidMemberOperationException("활성 회원만 랭킹에서 정지할 수 있습니다."));

        String view = controller.excludeFromRanking(1L, "ID", "1", 0, null, redirectAttributes);

        assertThat(view).isEqualTo("redirect:/members?type=ID&keyword=1&memberId=1");
        assertThat(redirectAttributes.getFlashAttributes().get("error"))
                .isEqualTo("활성 회원만 랭킹에서 정지할 수 있습니다.");
    }

    private MemberResponse member(Long id, MemberStatus status) {
        return new MemberResponse(
                id,
                "focus",
                SocialType.KAKAO,
                status,
                10,
                "message",
                "school",
                "address",
                null,
                null,
                100,
                10,
                null,
                null
        );
    }
}
