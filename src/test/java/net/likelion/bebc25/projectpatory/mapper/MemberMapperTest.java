package net.likelion.bebc25.projectpatory.mapper;

import net.likelion.bebc25.projectpatory.domain.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Sql(scripts = "classpath:sql/member-mapper-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class MemberMapperTest {

    @Autowired
    private MemberMapper memberMapper;

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
