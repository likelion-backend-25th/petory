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
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    @Test
    @DisplayName("최초 카카오 로그인이면 숫자 id를 문자열로 저장하고 KAKAO로 연결한다")
    void loadUser_whenNewKakaoMember_createsMemberAndSocialAccount() {
        Map<String, Object> profile = Map.of(
                "nickname", "카카오닉",
                "profile_image_url", "https://example.com/kakao.png"
        );
        Map<String, Object> kakaoAccount = Map.of(
                "email", "kakao-user@example.com",
                "profile", profile
        );
        Map<String, Object> attributes = Map.of(
                "id", 123456789L,
                "kakao_account", kakaoAccount
        );
        OAuth2User kakaoUser = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("OAUTH2_USER")),
                attributes,
                "id"
        );
        Member savedMember = Member.builder()
                .id(20L)
                .email("kakao-user@example.com")
                .nickname("카카오닉")
                .role("ROLE_USER")
                .build();
        given(memberMapper.findByEmail("kakao-user@example.com")).willReturn(null, savedMember);

        OAuth2User result = serviceReturning(kakaoUser).loadUser(kakaoUserRequest());

        ArgumentCaptor<SignUpRequest> requestCaptor = ArgumentCaptor.forClass(SignUpRequest.class);
        verify(memberMapper).createMember(requestCaptor.capture(), any());
        assertThat(requestCaptor.getValue().getEmail()).isEqualTo("kakao-user@example.com");
        assertThat(requestCaptor.getValue().getNickname()).isEqualTo("카카오닉");
        assertThat(requestCaptor.getValue().getProfileImage()).isEqualTo("https://example.com/kakao.png");

        ArgumentCaptor<SocialAccount> socialCaptor = ArgumentCaptor.forClass(SocialAccount.class);
        verify(memberMapper).registerSocialAccount(socialCaptor.capture());
        assertThat(socialCaptor.getValue().getProvider()).isEqualTo("KAKAO");
        assertThat(socialCaptor.getValue().getProviderUserId()).isEqualTo("123456789");
        assertThat(socialCaptor.getValue().getProviderEmail()).isEqualTo("kakao-user@example.com");
        assertThat(socialCaptor.getValue().getMemberId()).isEqualTo(20L);
        assertThat(((CustomUserDetails) result).getMember()).isEqualTo(savedMember);
    }

    @Test
    @DisplayName("카카오 이메일이 없으면 고유 id 기반 가상 이메일로 가입한다")
    void loadUser_whenKakaoEmailMissing_usesSyntheticEmail() {
        Map<String, Object> attributes = Map.of("id", 42L);
        OAuth2User kakaoUser = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("OAUTH2_USER")),
                attributes,
                "id"
        );
        Member savedMember = Member.builder()
                .id(21L)
                .email("kakao_42@kakao.social")
                .nickname("KakaoUser")
                .role("ROLE_USER")
                .build();
        given(memberMapper.findByEmail("kakao_42@kakao.social")).willReturn(null, savedMember);

        serviceReturning(kakaoUser).loadUser(kakaoUserRequest());

        ArgumentCaptor<SignUpRequest> requestCaptor = ArgumentCaptor.forClass(SignUpRequest.class);
        verify(memberMapper).createMember(requestCaptor.capture(), any());
        assertThat(requestCaptor.getValue().getEmail()).isEqualTo("kakao_42@kakao.social");
        assertThat(requestCaptor.getValue().getNickname()).isEqualTo("KakaoUser");

        ArgumentCaptor<SocialAccount> socialCaptor = ArgumentCaptor.forClass(SocialAccount.class);
        verify(memberMapper).registerSocialAccount(socialCaptor.capture());
        assertThat(socialCaptor.getValue().getProvider()).isEqualTo("KAKAO");
        assertThat(socialCaptor.getValue().getProviderUserId()).isEqualTo("42");
        assertThat(socialCaptor.getValue().getProviderEmail()).isEqualTo("kakao_42@kakao.social");
    }

    @Test
    @DisplayName("지원하지 않는 소셜 공급자면 예외를 던지고 회원을 만들지 않는다")
    void loadUser_whenUnknownProvider_throws() {
        OAuth2User oauth2User = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("OAUTH2_USER")),
                Map.of("sub", "naver-1"),
                "sub"
        );

        assertThatThrownBy(() -> serviceReturning(oauth2User).loadUser(userRequest("naver")))
                .isInstanceOf(OAuth2AuthenticationException.class);

        verify(memberMapper, never()).findByEmail(any());
        verify(memberMapper, never()).createMember(any(), any());
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
        return userRequest("google");
    }

    private OAuth2UserRequest kakaoUserRequest() {
        return userRequest("kakao");
    }

    private OAuth2UserRequest userRequest(String registrationId) {
        ClientRegistration registration = ClientRegistration.withRegistrationId(registrationId)
                .clientId("test-client-id")
                .clientSecret("test-client-secret")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("http://localhost:8080/login/oauth2/code/" + registrationId)
                .authorizationUri("https://example.com/oauth/authorize")
                .tokenUri("https://example.com/oauth/token")
                .userInfoUri("https://example.com/userinfo")
                .userNameAttributeName("google".equals(registrationId) ? "sub" : "id")
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
