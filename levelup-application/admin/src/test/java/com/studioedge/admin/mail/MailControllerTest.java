package com.studioedge.admin.mail;

import com.studioedge.admin.mail.dto.SendMailRequest;
import com.studioedge.admin.member.MemberService;
import com.studioedge.member.enums.MemberStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.ui.ConcurrentModel;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.security.Principal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MailControllerTest {

    private final MailService mailService = mock(MailService.class);
    private final MemberService memberService = mock(MemberService.class);
    private final Principal principal = () -> "operator";
    private MailController controller;

    @BeforeEach
    void setUp() {
        controller = new MailController(mailService, memberService);
        when(mailService.getRecentRewardMails(20)).thenReturn(List.of());
    }

    @Test
    void rendersMailPageWithMemberSearchAndRecentMails() {
        when(memberService.searchMembers(any(), any(), any())).thenReturn(new PageImpl<>(List.of()));

        ConcurrentModel model = new ConcurrentModel();
        String view = controller.mails(principal, "NICKNAME", "focus", 0, null, model);

        assertThat(view).isEqualTo("mails/index");
        assertThat(model.getAttribute("currentAdmin")).isEqualTo("operator");
        assertThat(model.getAttribute("recentMails")).isEqualTo(List.of());
        assertThat(model.getAttribute("mailRequest")).isInstanceOf(SendMailRequest.class);
    }

    @Test
    void sendsMailAndRedirectsToSelectedMember() {
        SendMailRequest request = request();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(request, "mailRequest");
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        String view = controller.sendMail(
                request,
                bindingResult,
                "ID",
                "1",
                0,
                redirectAttributes
        );

        verify(mailService).sendRewardMail(request);
        assertThat(view).isEqualTo("redirect:/mails?type=ID&keyword=1&memberId=1");
        assertThat(redirectAttributes.getFlashAttributes().get("message")).isEqualTo("보상 우편을 발송했습니다.");
    }

    private SendMailRequest request() {
        return new SendMailRequest(1L, "보상", "설명", "", "", 100, 0, 0, 30, true);
    }
}
