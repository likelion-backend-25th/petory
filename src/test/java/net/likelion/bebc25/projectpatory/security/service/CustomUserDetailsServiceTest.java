package net.likelion.bebc25.projectpatory.security.service;

import net.likelion.bebc25.projectpatory.domain.Member;
import net.likelion.bebc25.projectpatory.mapper.MemberMapper;
import net.likelion.bebc25.projectpatory.security.principal.CustomUserDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private MemberMapper memberMapper;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    @Test
    @DisplayName("이메일로 회원을 조회하면 CustomUserDetails를 반환한다")
    void loadUserByUsername_whenExists_returnsUserDetails() {
        // given
        String email = "test@petory.com";
        Member member = Member.builder()
                .id(1L)
                .email(email)
                .password("{bcrypt}encoded")
                .nickname("멍치")
                .role("ROLE_USER")
                .build();
        given(memberMapper.findByEmail(email)).willReturn(member);

        // when
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        // then
        assertThat(userDetails).isInstanceOf(CustomUserDetails.class);
        assertThat(userDetails.getUsername()).isEqualTo(email);
        assertThat(userDetails.getPassword()).isEqualTo("{bcrypt}encoded");
        assertThat(((CustomUserDetails) userDetails).getId()).isEqualTo(1L);
        verify(memberMapper).findByEmail(email);
    }

    @Test
    @DisplayName("존재하지 않는 이메일이면 UsernameNotFoundException을 던진다")
    void loadUserByUsername_whenNotExists_throwsException() {
        // given
        String email = "unknown@petory.com";
        given(memberMapper.findByEmail(email)).willReturn(null);

        // when & then
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername(email))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("해당 사용자가 존재하지 않습니다.");
    }

    @Test
    @DisplayName("회원 ID로 조회하면 CustomUserDetails를 반환한다")
    void loadUserById_whenExists_returnsUserDetails() {
        // given
        Long memberId = 2L;
        Member member = Member.builder()
                .id(memberId)
                .email("id-test@petory.com")
                .password("{bcrypt}encoded")
                .nickname("별이")
                .role("ROLE_ADMIN")
                .build();
        given(memberMapper.findById(memberId)).willReturn(member);

        // when
        UserDetails userDetails = userDetailsService.loadUserById(memberId);

        // then
        assertThat(userDetails).isInstanceOf(CustomUserDetails.class);
        assertThat(userDetails.getUsername()).isEqualTo("id-test@petory.com");
        assertThat(((CustomUserDetails) userDetails).getId()).isEqualTo(memberId);
        assertThat(userDetails.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_ADMIN");
        verify(memberMapper).findById(memberId);
    }

    @Test
    @DisplayName("존재하지 않는 회원 ID면 UsernameNotFoundException을 던진다")
    void loadUserById_whenNotExists_throwsException() {
        // given
        Long memberId = 999L;
        given(memberMapper.findById(memberId)).willReturn(null);

        // when & then
        assertThatThrownBy(() -> userDetailsService.loadUserById(memberId))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("해당 사용자가 존재하지 않습니다.");
    }
}
