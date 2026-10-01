package net.likelion.bebc25.projectpatory.security.config;

import net.likelion.bebc25.projectpatory.security.handler.CustomAccessDeniedHandler;
import net.likelion.bebc25.projectpatory.security.handler.CustomAuthenticationEntryPoint;
import net.likelion.bebc25.projectpatory.security.handler.OAuth2SuccessHandler;
import net.likelion.bebc25.projectpatory.security.jwt.JwtAuthenticationFilter;
import net.likelion.bebc25.projectpatory.security.jwt.JwtProvider;
import net.likelion.bebc25.projectpatory.security.oauth.CustomOAuth2UserService;
import net.likelion.bebc25.projectpatory.security.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final JwtProvider jwtProvider;
    private final CustomUserDetailsService userDetailsService;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    public SecurityConfig(
            JwtProvider jwtProvider,
            CustomUserDetailsService userDetailsService,
            CustomOAuth2UserService customOAuth2UserService,
            OAuth2SuccessHandler oAuth2SuccessHandler) {
        this.jwtProvider = jwtProvider;
        this.userDetailsService = userDetailsService;
        this.customOAuth2UserService = customOAuth2UserService;
        this.oAuth2SuccessHandler = oAuth2SuccessHandler;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            CustomAuthenticationEntryPoint customAuthenticationEntryPoint,
            CustomAccessDeniedHandler customAccessDeniedHandler
    ) throws Exception {
        http
                // REST API 환경: CSRF, Form 로그인 비활성화
                .csrf(AbstractHttpConfigurer::disable)

                // HTTP Basic 인증을 비활성화하고 무상태 JWT 인증 체계로 전환
                .httpBasic(AbstractHttpConfigurer::disable)

                // 기본 폼 로그인 비활성화
                .formLogin(AbstractHttpConfigurer::disable)

                // WebConfig의 CORS 설정을 Security 필터에서도 적용
                .cors(Customizer.withDefaults())

                // 세션을 사용하지 않는 Stateless 방식
                .sessionManagement(session ->
                                           session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 커스텀 JWT 인증 필터를 UsernamePasswordAuthenticationFilter 바로 앞에 배치
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtProvider, userDetailsService),
                        UsernamePasswordAuthenticationFilter.class
                )

                // URL 엔드포인트별 접근 인가
                .authorizeHttpRequests(auth -> auth
                        // 예외 포워딩이 403으로 가려지지 않도록 허용
                        .requestMatchers("/error").permitAll()

                        // 인증 API (login, refresh)
                        .requestMatchers("/api/v1/login", "/api/v1/refresh").permitAll()

                        // 회원가입과 가입 전 중복 확인
                        .requestMatchers("/api/v1/signup").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/email/exists", "/api/v1/nickname/exists").permitAll()

                        // 게시글 GET 공개
                        .requestMatchers(HttpMethod.GET, "/api/v1/posts", "/api/v1/posts/**").permitAll()

                        // 상대 프로필과 그 회원의 피드 글 목록은 비로그인 조회 가능
                        .requestMatchers(HttpMethod.GET, "/api/v1/profile/*", "/api/v1/profile/*/posts").permitAll()

                        // PortOne 웹훅 (PortOne 서버가 호출하므로 JWT가 없다)
                        .requestMatchers(HttpMethod.POST, "/api/v1/payments/webhook").permitAll()

                        // Swagger UI 및 API 문서
                        .requestMatchers(
                                "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/v3/api-docs"
                        ).permitAll()

                        .requestMatchers("/favicon.ico", "/oauth/**").permitAll()

                        // 그 외는 인증 필요
                        .anyRequest().authenticated()
                )

                // 미인증 요청은 401로 응답
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler)
                )
                .oauth2Login(oauth2 -> oauth2
                        // 1. 소셜 사용자 프로필 조회 및 DB 저장 커스텀 서비스 등록
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        // 2. 소셜 인증 성공 후 자체 JWT 발급 및 프론트엔드 리다이렉트 핸들러 등록
                        .successHandler(oAuth2SuccessHandler)
                );

        return http.build();
    }
}
