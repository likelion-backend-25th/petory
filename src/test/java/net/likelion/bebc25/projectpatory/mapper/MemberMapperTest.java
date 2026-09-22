package net.likelion.bebc25.projectpatory.mapper;

import net.likelion.bebc25.projectpatory.domain.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

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
}
