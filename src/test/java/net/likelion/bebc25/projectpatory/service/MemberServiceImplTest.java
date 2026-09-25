package net.likelion.bebc25.projectpatory.service;

import net.likelion.bebc25.projectpatory.domain.Member;
import net.likelion.bebc25.projectpatory.dto.MemberProfileResponse;
import net.likelion.bebc25.projectpatory.dto.MyProfileResponse;
import net.likelion.bebc25.projectpatory.dto.ProfileResponse;
import net.likelion.bebc25.projectpatory.dto.SignUpRequest;
import net.likelion.bebc25.projectpatory.mapper.MemberMapper;
import net.likelion.bebc25.projectpatory.mapper.PostMapper;
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
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MemberServiceImplTest {

    @Mock
    private MemberMapper memberMapper;

    @Mock
    private PostMapper postMapper;

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

    @Test
    @DisplayName("회원 ID로 조회 시 회원이 없으면 NoSuchElementException을 던진다")
    void findMemberById_whenNotFound_throwsException() {
        // given
        given(memberMapper.findById(999L)).willReturn(null);

        // when & then
        assertThatThrownBy(() -> memberService.findMemberById(999L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("존재하지 않는 회원입니다");
    }

    @Test
    @DisplayName("본인 프로필 조회 시 이메일·주소 등 민감 정보를 포함한 MyProfileResponse를 반환한다")
    void getMyProfile_whenOwnProfile_returnsMyProfileResponseWithSensitiveFields() {
        // given
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 12, 0);
        LocalDateTime agreementAt = LocalDateTime.of(2024, 1, 1, 12, 5);
        Member member = Member.builder()
                .id(1L)
                .email("me@petory.com")
                .nickname("뭉치")
                .species("개")
                .sex("수")
                .birthDate(LocalDate.of(2020, 5, 1))
                .intro("안녕하세요")
                .profileImage("me.png")
                .address("서울시 강남구")
                .status("ACTIVE")
                .role("ROLE_USER")
                .createdAt(createdAt)
                .infoProvideAgreement(agreementAt)
                .build();

        given(postMapper.countMyPosts(1L)).willReturn(10L);
        given(memberMapper.countFollowers(1L)).willReturn(3L);
        given(memberMapper.countFollowings(1L)).willReturn(5L);

        // when
        ProfileResponse result = memberService.getMyProfile(member, 1L);

        // then
        assertThat(result).isInstanceOf(MyProfileResponse.class);
        MyProfileResponse profile = (MyProfileResponse) result;
        assertThat(profile.id()).isEqualTo(1L);
        assertThat(profile.email()).isEqualTo("me@petory.com");
        assertThat(profile.nickname()).isEqualTo("뭉치");
        assertThat(profile.species()).isEqualTo("개");
        assertThat(profile.sex()).isEqualTo("수");
        assertThat(profile.birthDate()).isEqualTo(LocalDate.of(2020, 5, 1));
        assertThat(profile.address()).isEqualTo("서울시 강남구");
        assertThat(profile.infoProvideAgreement()).isEqualTo(agreementAt);
        assertThat(profile.postsCount()).isEqualTo(10L);
        assertThat(profile.followers()).isEqualTo(3L);
        assertThat(profile.followings()).isEqualTo(5L);

        verify(postMapper).countMyPosts(1L);
        verify(memberMapper).countFollowers(1L);
        verify(memberMapper).countFollowings(1L);
    }

    @Test
    @DisplayName("타인 프로필 조회 시 민감 정보 없이 MemberProfileResponse를 반환한다")
    void getMyProfile_whenOtherProfile_returnsMemberProfileResponseWithoutSensitiveFields() {
        // given
        LocalDateTime createdAt = LocalDateTime.of(2024, 2, 1, 10, 0);
        Member other = Member.builder()
                .id(2L)
                .email("other@petory.com")
                .nickname("별이")
                .species("고양이")
                .sex("암")
                .birthDate(LocalDate.of(2021, 3, 15))
                .intro("안녕")
                .profileImage("other.png")
                .address("부산시 해운대구")
                .status("ACTIVE")
                .role("ROLE_USER")
                .createdAt(createdAt)
                .infoProvideAgreement(LocalDateTime.of(2024, 2, 1, 10, 1))
                .build();

        given(postMapper.countMyPosts(2L)).willReturn(4L);
        given(memberMapper.countFollowers(2L)).willReturn(1L);
        given(memberMapper.countFollowings(2L)).willReturn(2L);

        // when: 로그인 사용자는 1L, 조회 대상은 2L
        ProfileResponse result = memberService.getMyProfile(other, 1L);

        // then
        assertThat(result).isInstanceOf(MemberProfileResponse.class);
        MemberProfileResponse profile = (MemberProfileResponse) result;
        assertThat(profile.id()).isEqualTo(2L);
        assertThat(profile.nickname()).isEqualTo("별이");
        assertThat(profile.intro()).isEqualTo("안녕");
        assertThat(profile.profileImage()).isEqualTo("other.png");
        assertThat(profile.status()).isEqualTo("ACTIVE");
        assertThat(profile.role()).isEqualTo("ROLE_USER");
        assertThat(profile.createdAt()).isEqualTo(createdAt);
        assertThat(profile.postsCount()).isEqualTo(4L);
        assertThat(profile.followers()).isEqualTo(1L);
        assertThat(profile.followings()).isEqualTo(2L);

        // record 컴포넌트에 email/address 등이 없음을 타입으로 보장
        assertThat(profile.getClass().getRecordComponents())
                .extracting(rc -> rc.getName())
                .doesNotContain("email", "address", "species", "sex", "birthDate", "infoProvideAgreement");
    }
}
