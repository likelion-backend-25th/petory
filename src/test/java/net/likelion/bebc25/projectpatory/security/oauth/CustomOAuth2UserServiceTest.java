package net.likelion.bebc25.projectpatory.security.oauth;

import net.likelion.bebc25.projectpatory.domain.Member;
import net.likelion.bebc25.projectpatory.domain.SocialAccount;
import net.likelion.bebc25.projectpatory.dto.SignUpRequest;
import net.likelion.bebc25.projectpatory.mapper.MemberMapper;
import net.likelion.bebc25.projectpatory.security.principal.CustomUserDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

    @Mock
    private MemberMapper memberMapper;

    @Test
    @DisplayName("최초 구글 로그인이면 회원을 만들고 linked_account에 GOOGLE로 연결한다")
    void loadUser_whenNewMember_createsMemberAndSocialAccount() {
        Map<String, Object> attributes = Map.of(
                "sub", "google-sub-1",
                "email", "user@company.com",
                "name", "병호",
                "picture", "https://example.com/profile.png"
        );
        OAuth2User googleUser = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("OAUTH2_USER")),
                attributes,
                "sub"
        );
        Member savedMember = Member.builder()
                .id(10L)
                .email("user@company.com")
                .nickname("병호")
                .role("ROLE_USER")
                .build();
        given(memberMapper.findByEmail("user@company.com")).willReturn(null, savedMember);

        CustomOAuth2UserService service = serviceReturning(googleUser);

        OAuth2User result = service.loadUser(googleUserRequest());

        ArgumentCaptor<SignUpRequest> requestCaptor = ArgumentCaptor.forClass(SignUpRequest.class);
        ArgumentCaptor<LocalDateTime> agreementCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(memberMapper).createMember(requestCaptor.capture(), agreementCaptor.capture());

        SignUpRequest savedRequest = requestCaptor.getValue();
        assertThat(savedRequest.getEmail()).isEqualTo("user@company.com");
        assertThat(savedRequest.getPassword()).isEmpty();
        assertThat(savedRequest.getNickname()).isEqualTo("병호");
        assertThat(savedRequest.getProfileImage()).isEqualTo("https://example.com/profile.png");
        assertThat(savedRequest.getBirthDate()).isEqualTo(LocalDate.now());
        assertThat(agreementCaptor.getValue()).isNotNull();

        ArgumentCaptor<SocialAccount> socialCaptor = ArgumentCaptor.forClass(SocialAccount.class);
        verify(memberMapper).registerSocialAccount(socialCaptor.capture());
        SocialAccount socialAccount = socialCaptor.getValue();
        assertThat(socialAccount.getMemberId()).isEqualTo(10L);
        assertThat(socialAccount.getProvider()).isEqualTo("GOOGLE");
        assertThat(socialAccount.getProviderUserId()).isEqualTo("google-sub-1");
        assertThat(socialAccount.getProviderEmail()).isEqualTo("user@company.com");

        assertThat(result).isInstanceOf(CustomUserDetails.class);
        CustomUserDetails userDetails = (CustomUserDetails) result;
        assertThat(userDetails.getMember()).isEqualTo(savedMember);
        assertThat(userDetails.getUsername()).isEqualTo("user@company.com");
        assertThat(userDetails.getAttributes()).containsEntry("sub", "google-sub-1");
        assertThat(userDetails.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_USER");
    }

    @Test
    @DisplayName("이미 같은 이메일의 회원이 있으면 회원가입과 소셜 계정 연결을 하지 않는다")
    void loadUser_whenEmailExists_returnsExistingMember() {
        Map<String, Object> attributes = Map.of(
                "sub", "google-sub-2",
                "email", "existing@petory.com",
                "name", "기존회원",
                "picture", "https://example.com/existing.png"
        );
        OAuth2User googleUser = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("OAUTH2_USER")),
                attributes,
                "sub"
        );
        Member existingMember = Member.builder()
                .id(3L)
                .email("existing@petory.com")
                .nickname("기존회원")
                .role("ROLE_USER")
                .build();
        given(memberMapper.findByEmail("existing@petory.com")).willReturn(existingMember);

        CustomOAuth2UserService service = serviceReturning(googleUser);

        OAuth2User result = service.loadUser(googleUserRequest());

        verify(memberMapper, never()).createMember(any(), any());
        verify(memberMapper, never()).registerSocialAccount(any());
        assertThat(result).isInstanceOf(CustomUserDetails.class);
        assertThat(((CustomUserDetails) result).getId()).isEqualTo(3L);
    }

    private CustomOAuth2UserService serviceReturning(OAuth2User oauth2User) {
        return new CustomOAuth2UserService(memberMapper) {
            @Override
            protected OAuth2User fetchOAuth2User(OAuth2UserRequest userRequest) {
                return oauth2User;
            }
        };
    }

    private OAuth2UserRequest googleUserRequest() {
        ClientRegistration registration = ClientRegistration.withRegistrationId("google")
                .clientId("test-client-id")
                .clientSecret("test-client-secret")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("http://localhost:8080/login/oauth2/code/google")
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                .tokenUri("https://www.googleapis.com/oauth2/v4/token")
                .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
                .userNameAttributeName("sub")
                .build();
        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                "google-access-token",
                Instant.now(),
                Instant.now().plusSeconds(60)
        );
        return new OAuth2UserRequest(registration, accessToken);
    }
}
