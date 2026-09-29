package net.likelion.bebc25.projectpatory.security.oauth;

import lombok.extern.slf4j.Slf4j;
import net.likelion.bebc25.projectpatory.domain.Member;
import net.likelion.bebc25.projectpatory.domain.SocialAccount;
import net.likelion.bebc25.projectpatory.dto.SignUpRequest;
import net.likelion.bebc25.projectpatory.mapper.MemberMapper;
import net.likelion.bebc25.projectpatory.security.principal.CustomUserDetails;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberMapper memberMapper;

    public CustomOAuth2UserService(MemberMapper memberMapper) {
        this.memberMapper = memberMapper;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. 스프링 기본 구현체를 통해 구글 UserInfo 엔드포인트에서 프로필 JSON 정보 조회
        OAuth2User oAuth2User = fetchOAuth2User(userRequest);

        // 2. 구글 응답 JSON 속성 획득 (단순 1차원 구조)
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String id = (String) attributes.get("sub");
        String email = (String) attributes.get("email");
        String nickname = (String) attributes.get("name");
        String profileImage = (String) attributes.get("picture");
        String provider = userRequest.getClientRegistration().getRegistrationId().toUpperCase(Locale.ROOT);

        // 3. DB 조회 후 최초 로그인이면 자동 회원가입 진행
        Member member = saveOrUpdate(id, email, nickname, profileImage, provider);

        // 4. 통합 인증 주체 반환
        return new CustomUserDetails(member, attributes);
    }

    protected OAuth2User fetchOAuth2User(OAuth2UserRequest userRequest) {
        return super.loadUser(userRequest);
    }

    private Member saveOrUpdate(String id, String email, String nickname, String profileImage, String provider) {
        Member existingMember = memberMapper.findByEmail(email);
        if (existingMember == null) {
            SignUpRequest request = SignUpRequest.builder()
                    .email(email)
                    .password("") // 소셜 회원은 자체 비밀번호가 없으므로 빈 문자열 저장
                    .nickname(nickname)
                    .species("")
                    .sex("")
                    .birthDate(LocalDate.now())
                    .profileImage(profileImage)
                    .build();
            memberMapper.createMember(request, LocalDateTime.now());
            Member member = memberMapper.findByEmail(email);

            SocialAccount socialAccount = SocialAccount.builder()
                    .memberId(member.getId())
                    .provider(provider)
                    .providerUserId(id)
                    .providerEmail(email)
                    .build();
            memberMapper.registerSocialAccount(socialAccount);

            return member;
        }
        return existingMember;
    }
}
