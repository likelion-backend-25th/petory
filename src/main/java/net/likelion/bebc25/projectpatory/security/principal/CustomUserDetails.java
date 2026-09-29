package net.likelion.bebc25.projectpatory.security.principal;

import net.likelion.bebc25.projectpatory.domain.Member;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CustomUserDetails implements UserDetails, OAuth2User {

    private final Member member;
    private final Map<String, Object> attributes;

    // 일반 폼 로그인 및 JWT 필터용 생성자
    public CustomUserDetails(Member member) {
        this.member = member;
        this.attributes = Collections.emptyMap();
    }

    // OAuth 2.0 소셜 로그인용 신규 생성자 추가
    public CustomUserDetails(Member member, Map<String, Object> attributes) {
        this.member = member;
        this.attributes = attributes;
    }

    public Member getMember() {
        return member;
    }

    public Long getId() {
        return member.getId();
    }

    // 권한 목록 반환
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 권한 문자열을 SimpleGrantedAuthority 객체로 변환 (ROLE_ 접두사 필수)
        return List.of(new SimpleGrantedAuthority(member.getRole()));
    }

    @Override
    public String getPassword() {
        return member.getPassword();
    }

    @Override
    public String getUsername() {
        return member.getEmail();
    }

    
    // 2. OAuth2User 인터페이스 구현 메서드 추가
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return member.getEmail();
    }
}