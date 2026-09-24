package net.likelion.bebc25.projectpatory.service;

import net.likelion.bebc25.projectpatory.domain.Member;
import net.likelion.bebc25.projectpatory.dto.SignUpRequest;
import net.likelion.bebc25.projectpatory.mapper.MemberMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MemberServiceImplTest {

    @Mock
    private MemberMapper memberMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private MemberServiceImpl memberService;

    @Test
    @DisplayName("회원가입 시 비밀번호를 암호화하고, 동의 시 현재 시각과 함께 createMember를 호출한다")
    void signup_whenAgreed_encodesPasswordAndPassesAgreementTime() {
        // given
        SignUpRequest request = SignUpRequest.builder()
                .email("signup@petory.com")
                .password("raw-password")
                .nickname("뭉치")
                .species("개")
                .sex("수")
                .birthDate(LocalDate.of(2020, 5, 1))
                .isAgreed(true)
                .build();
        Member savedMember = Member.builder()
                .id(1L)
                .email("signup@petory.com")
                .password("{bcrypt}encoded-password")
                .nickname("뭉치")
                .build();

        given(passwordEncoder.encode("raw-password")).willReturn("{bcrypt}encoded-password");
        given(memberMapper.findByEmail("signup@petory.com")).willReturn(savedMember);

        // when
        Member result = memberService.signup(request);

        // then
        verify(passwordEncoder).encode("raw-password");

        ArgumentCaptor<SignUpRequest> requestCaptor = ArgumentCaptor.forClass(SignUpRequest.class);
        ArgumentCaptor<LocalDateTime> agreementCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(memberMapper).createMember(requestCaptor.capture(), agreementCaptor.capture());
        verify(memberMapper).findByEmail("signup@petory.com");

        SignUpRequest savedRequest = requestCaptor.getValue();
        assertThat(savedRequest.getEmail()).isEqualTo("signup@petory.com");
        assertThat(savedRequest.getPassword()).isEqualTo("{bcrypt}encoded-password");
        assertThat(savedRequest.getNickname()).isEqualTo("뭉치");
        assertThat(savedRequest.getSpecies()).isEqualTo("개");
        assertThat(savedRequest.getSex()).isEqualTo("수");
        assertThat(savedRequest.getBirthDate()).isEqualTo(LocalDate.of(2020, 5, 1));
        assertThat(agreementCaptor.getValue()).isNotNull();
        assertThat(result).isEqualTo(savedMember);
    }

    @Test
    @DisplayName("개인정보 제공 비동의 시 infoProvideAgreement를 null로 전달한다")
    void signup_whenNotAgreed_passesNullAgreementTime() {
        // given
        SignUpRequest request = SignUpRequest.builder()
                .email("signup-no-agree@petory.com")
                .password("raw-password")
                .nickname("별이")
                .species("고양이")
                .sex("암")
                .birthDate(LocalDate.of(2021, 3, 15))
                .isAgreed(false)
                .build();
        Member savedMember = Member.builder()
                .id(2L)
                .email("signup-no-agree@petory.com")
                .build();

        given(passwordEncoder.encode("raw-password")).willReturn("{bcrypt}encoded-password");
        given(memberMapper.findByEmail("signup-no-agree@petory.com")).willReturn(savedMember);

        // when
        Member result = memberService.signup(request);

        // then
        verify(memberMapper).createMember(eq(request), eq(null));
        verify(memberMapper).findByEmail("signup-no-agree@petory.com");
        assertThat(result).isEqualTo(savedMember);
    }
}
