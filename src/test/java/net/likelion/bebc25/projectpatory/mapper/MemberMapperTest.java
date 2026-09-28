package net.likelion.bebc25.projectpatory.mapper;

import net.likelion.bebc25.projectpatory.domain.Member;
import net.likelion.bebc25.projectpatory.dto.MemberProfileEditRequest;
import net.likelion.bebc25.projectpatory.dto.SignUpRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Sql(scripts = "classpath:sql/member-mapper-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class MemberMapperTest {

    @Autowired
    private MemberMapper memberMapper;

    @Test
    @DisplayName("회원가입 정보를 저장하면 회원이 생성된다")
    void createMemberTest() {
        // given
        SignUpRequest request = SignUpRequest.builder()
                .email("signup-test@petory.com")
                .password("{bcrypt}encoded-password")
                .nickname("가입테스트")
                .species("고양이")
                .sex("암")
                .birthDate(LocalDate.of(2021, 3, 15))
                .intro("반가워요")
                .profileImage("https://example.com/profile/signup-test.png")
                .address("서울시 노원구")
                .isAgreed(true)
                .build();
        LocalDateTime infoProvideAgreement = LocalDateTime.of(2026, 9, 24, 11, 0, 0);

        // when
        memberMapper.createMember(request, infoProvideAgreement);

        // then
        Member saved = memberMapper.findByEmail("signup-test@petory.com");
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isPositive();
        assertThat(saved.getEmail()).isEqualTo("signup-test@petory.com");
        assertThat(saved.getPassword()).isEqualTo("{bcrypt}encoded-password");
        assertThat(saved.getNickname()).isEqualTo("가입테스트");
        assertThat(saved.getSpecies()).isEqualTo("고양이");
        assertThat(saved.getSex()).isEqualTo("암");
        assertThat(saved.getBirthDate()).isEqualTo(LocalDate.of(2021, 3, 15));
        assertThat(saved.getIntro()).isEqualTo("반가워요");
        assertThat(saved.getProfileImage()).isEqualTo("https://example.com/profile/signup-test.png");
        assertThat(saved.getAddress()).isEqualTo("서울시 노원구");
        assertThat(saved.getInfoProvideAgreement()).isEqualTo(infoProvideAgreement);
        assertThat(saved.getStatus()).isEqualTo("ACTIVE");
        assertThat(saved.getRole()).isEqualTo("ROLE_USER");
    }

    @Test
    @DisplayName("개인정보 제공 비동의 시 infoProvideAgreement가 null로 저장된다")
    void createMember_whenNoAgreement_savesNullAgreement() {
        // given
        SignUpRequest request = SignUpRequest.builder()
                .email("signup-no-agree@petory.com")
                .password("{bcrypt}encoded-password")
                .nickname("비동의테스트")
                .species("개")
                .sex("수")
                .birthDate(LocalDate.of(2022, 1, 1))
                .isAgreed(false)
                .build();

        // when
        memberMapper.createMember(request, null);

        // then
        Member saved = memberMapper.findByEmail("signup-no-agree@petory.com");
        assertThat(saved).isNotNull();
        assertThat(saved.getInfoProvideAgreement()).isNull();
        assertThat(saved.getIntro()).isEqualTo("안녕하세요");
        assertThat(saved.getProfileImage()).isEmpty();
        assertThat(saved.getAddress()).isEmpty();
    }

    @Test
    @DisplayName("이메일로 회원 정보 조회 테스트")
    void findByEmailTest() {
        // given
        String email = "mapper-test@petory.com";

        // when
        Member member = memberMapper.findByEmail(email);

        // then
        assertThat(member).isNotNull();
        assertThat(member.getId()).isPositive();
        assertThat(member.getEmail()).isEqualTo(email);
        assertThat(member.getNickname()).isEqualTo("멍치테스트");
        assertThat(member.getPassword()).isEqualTo("{bcrypt}encoded-password");
        assertThat(member.getProfileImage()).isEqualTo("https://example.com/profile/mapper-test.png");
        assertThat(member.getRole()).isEqualTo("ROLE_USER");
        assertThat(member.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 조회하면 null을 반환한다")
    void findByEmail_whenNotExists_returnsNull() {
        // given
        String email = "unknown@petory.com";

        // when
        Member member = memberMapper.findByEmail(email);

        // then
        assertThat(member).isNull();
    }

    @Test
    @DisplayName("전체 회원 목록을 조회한다")
    void findAllTest() {

        // when
        List<Member> members = memberMapper.findAll();

        // then
        assertThat(members)
                .allSatisfy(member -> {
//                    member domain 완성되는데로 추가 필요
                    assertThat(member.getId()).isNotNull();
                    assertThat(member.getNickname()).isNotNull();
                    assertThat(member.getRole()).isNotNull();
                });
    }

    @Test
    @DisplayName("내 프로필을 수정하면 프로필 컬럼만 변경된다")
    void editMyProfileTest() {
        // given
        Member before = memberMapper.findByEmail("mapper-test@petory.com");
        assertThat(before).isNotNull();

        Member other = memberMapper.findById(1L);
        assertThat(other).isNotNull();
        assertThat(other.getId()).isNotEqualTo(before.getId());

        MemberProfileEditRequest request = MemberProfileEditRequest.builder()
                .nickname("수정멍치")
                .species("고양이")
                .sex("여")
                .birthDate("2021-08-20")
                .intro("프로필을 수정했어요")
                .profileImage("https://example.com/profile/edited.png")
                .address("서울시 마포구")
                .build();

        // when
        memberMapper.editMyProfile(request, before.getId());

        // then
        Member after = memberMapper.findById(before.getId());
        assertThat(after.getNickname()).isEqualTo("수정멍치");
        assertThat(after.getSpecies()).isEqualTo("고양이");
        assertThat(after.getSex()).isEqualTo("여");
        assertThat(after.getBirthDate()).isEqualTo(LocalDate.of(2021, 8, 20));
        assertThat(after.getIntro()).isEqualTo("프로필을 수정했어요");
        assertThat(after.getProfileImage()).isEqualTo("https://example.com/profile/edited.png");
        assertThat(after.getAddress()).isEqualTo("서울시 마포구");
        assertThat(after.getEmail()).isEqualTo(before.getEmail());
        assertThat(after.getPassword()).isEqualTo(before.getPassword());
        assertThat(after.getRole()).isEqualTo(before.getRole());
        assertThat(after.getStatus()).isEqualTo(before.getStatus());

        Member unchanged = memberMapper.findById(other.getId());
        assertThat(unchanged.getNickname()).isEqualTo(other.getNickname());
        assertThat(unchanged.getSpecies()).isEqualTo(other.getSpecies());
        assertThat(unchanged.getAddress()).isEqualTo(other.getAddress());
    }

    @Test
    @DisplayName("회원 계정을 정지하면 status가 BLOCKED로 변경된다")
    void updateStatusToBlockedTest() {

        // given
        Long id = 1L;

        // when
        int result = memberMapper.updateStatusToBlocked(id);

        // then
        assertThat(result).isEqualTo(1);

        Member member = memberMapper.findById(id);

        assertThat(member).isNotNull();
        assertThat(member.getStatus()).isEqualTo("BLOCKED");
    }

    @Test
    @DisplayName("회원 ID로 회원을 삭제한다")
    void deleteByIdTest() {

        // given
        Long id = 1L;

        // 삭제 전에 회원이 실제로 존재하는지 확인
        Member beforeMember = memberMapper.findById(id);
        assertThat(beforeMember).isNotNull();

        // when
        int result = memberMapper.deleteById(id);

        // then
        assertThat(result).isEqualTo(1);

        Member afterMember = memberMapper.findById(id);
        assertThat(afterMember).isNull();
    }
}
