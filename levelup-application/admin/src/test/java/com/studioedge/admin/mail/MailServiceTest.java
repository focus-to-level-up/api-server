package com.studioedge.admin.mail;

import com.studioedge.admin.mail.dto.MailResponse;
import com.studioedge.admin.mail.dto.SendMailRequest;
import com.studioedge.mail.entity.Mail;
import com.studioedge.mail.enums.MailType;
import com.studioedge.mail.repository.MailRepository;
import com.studioedge.member.entity.Member;
import com.studioedge.member.enums.MemberStatus;
import com.studioedge.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MailServiceTest {

    @Mock
    private MailRepository mailRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MailService mailService;

    @Test
    void sendsRewardMailToBannedMember() {
        Member member = member(MemberStatus.BANNED);
        SendMailRequest request = request();
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(mailRepository.save(any(Mail.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mailService.sendRewardMail(request);

        ArgumentCaptor<Mail> captor = ArgumentCaptor.forClass(Mail.class);
        verify(mailRepository).save(captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo(MailType.ADMIN_REWARD);
        assertThat(captor.getValue().getDiamondAmount()).isEqualTo(100);
        assertThat(captor.getValue().getExpiredAt()).isEqualTo(LocalDate.now().plusDays(30));
    }

    @Test
    void rejectsRewardMailForWithdrawnMember() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member(MemberStatus.WITHDRAWN)));

        assertThatThrownBy(() -> mailService.sendRewardMail(request()))
                .isInstanceOf(InvalidMailOperationException.class)
                .hasMessage("탈퇴 또는 가입 미완료 회원에게는 우편을 발송할 수 없습니다.");
    }

    @Test
    void returnsRecentAdminRewardMails() {
        Mail mail = Mail.builder()
                .receiver(member(MemberStatus.ACTIVE))
                .type(MailType.ADMIN_REWARD)
                .title("보상")
                .description("설명")
                .diamondAmount(100)
                .expiredAt(LocalDate.now().plusDays(30))
                .build();
        when(mailRepository.findRecentByType(any(MailType.class), any(Pageable.class)))
                .thenReturn(List.of(mail));

        List<MailResponse> result = mailService.getRecentRewardMails(20);

        assertThat(result).singleElement().satisfies(response -> {
            assertThat(response.type()).isEqualTo(MailType.ADMIN_REWARD);
            assertThat(response.receiverNickname()).isEqualTo("focus");
        });
        verify(mailRepository).findRecentByType(
                org.mockito.ArgumentMatchers.eq(MailType.ADMIN_REWARD),
                org.mockito.ArgumentMatchers.argThat(pageable -> pageable.getPageSize() == 20)
        );
    }

    private SendMailRequest request() {
        return new SendMailRequest(
                1L,
                "CS 보상",
                "문의 보상입니다.",
                "",
                "",
                100,
                0,
                0,
                30,
                true
        );
    }

    private Member member(MemberStatus status) {
        return Member.builder()
                .socialId("social-id")
                .nickname("focus")
                .status(status)
                .build();
    }
}
