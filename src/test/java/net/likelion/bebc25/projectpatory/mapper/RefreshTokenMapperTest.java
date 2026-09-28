package net.likelion.bebc25.projectpatory.mapper;

import net.likelion.bebc25.projectpatory.domain.Member;
import net.likelion.bebc25.projectpatory.domain.RefreshToken;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Sql(scripts = "classpath:sql/member-mapper-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class RefreshTokenMapperTest {

    @Autowired
    private RefreshTokenMapper refreshTokenMapper;

    @Autowired
    private MemberMapper memberMapper;

    @Test
    @DisplayName("refresh 토큰을 저장하면 토큰 값으로 조회된다")
    void saveRefreshToken_thenGetByToken() {
        // given
        Member member = memberMapper.findByEmail("mapper-test@petory.com");
        assertThat(member).isNotNull();
        LocalDateTime expiration = LocalDateTime.of(2026, 10, 5, 15, 0, 0);
        String token = "refresh-token-device-1";

        // when
        refreshTokenMapper.saveRefreshToken(member.getId(), token, expiration);

        // then
        RefreshToken saved = refreshTokenMapper.getRefreshToken(token);
        assertThat(saved).isNotNull();
        assertThat(saved.getMemberId()).isEqualTo(member.getId());
        assertThat(saved.getRefreshToken()).isEqualTo(token);
        assertThat(saved.getExpiration()).isEqualTo(expiration);
    }

    @Test
    @DisplayName("같은 회원의 refresh 토큰은 토큰 값으로 각각 조회된다")
    void getRefreshToken_whenSameMemberHasTwoTokens_returnsMatchingToken() {
        // given
        Member member = memberMapper.findByEmail("mapper-test@petory.com");
        assertThat(member).isNotNull();
        LocalDateTime expiration = LocalDateTime.of(2026, 10, 5, 15, 0, 0);
        refreshTokenMapper.saveRefreshToken(member.getId(), "refresh-token-phone", expiration);
        refreshTokenMapper.saveRefreshToken(member.getId(), "refresh-token-laptop", expiration.plusDays(1));

        // when
        RefreshToken phone = refreshTokenMapper.getRefreshToken("refresh-token-phone");
        RefreshToken laptop = refreshTokenMapper.getRefreshToken("refresh-token-laptop");

        // then
        assertThat(phone.getRefreshToken()).isEqualTo("refresh-token-phone");
        assertThat(phone.getExpiration()).isEqualTo(expiration);
        assertThat(laptop.getRefreshToken()).isEqualTo("refresh-token-laptop");
        assertThat(laptop.getExpiration()).isEqualTo(expiration.plusDays(1));
        assertThat(phone.getMemberId()).isEqualTo(laptop.getMemberId());
    }

    @Test
    @DisplayName("저장되지 않은 refresh 토큰을 조회하면 null을 반환한다")
    void getRefreshToken_whenNotExists_returnsNull() {
        // when
        RefreshToken found = refreshTokenMapper.getRefreshToken("unknown-refresh-token");

        // then
        assertThat(found).isNull();
    }
}
