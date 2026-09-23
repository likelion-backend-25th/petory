package net.likelion.bebc25.projectpatory.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // REST API 환경: CSRF, Form 로그인 비활성화
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)

                // HTTP Basic 인증 활성화 (학습/테스트용)
                .httpBasic(Customizer.withDefaults())

                // WebConfig의 CORS 설정을 Security 필터에서도 적용
                .cors(Customizer.withDefaults())

                // 세션을 사용하지 않는 Stateless 방식
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // URL 엔드포인트별 접근 인가
                .authorizeHttpRequests(auth -> auth
                        // 예외 포워딩이 403으로 가려지지 않도록 허용
                        .requestMatchers("/error").permitAll()

                        // 인증 API
                        .requestMatchers("/api/v1/auth/**").permitAll()

                        // 게시글 GET 공개
                        .requestMatchers(HttpMethod.GET, "/api/v1/posts", "/api/v1/posts/**").permitAll()

                        // Swagger UI 및 API 문서
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/v3/api-docs"
                        ).permitAll()

                        // 그 외는 인증 필요
                        .anyRequest().authenticated()
                )

                // 미인증 요청은 401로 응답
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                );

        return http.build();
    }
}
