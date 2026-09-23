package net.likelion.bebc25.projectpatory.security;

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
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // REST API 환경 설정을 위해 CSRF, Form 로그인, Basic Auth 비활성화
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                // WebConfig의 CORS 설정을 Security 필터에서도 적용
                .cors(Customizer.withDefaults())

                // 세션을 사용하지 않는 Stateless 방식 적용
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // API 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // 0. 예외 발생 시 /error 포워딩이 403으로 가려지지 않도록 허용
                        .requestMatchers("/error").permitAll()

                        // 1. /api/v1/posts 단일 경로 및 하위 경로 모든 GET 요청 허용
                        .requestMatchers(HttpMethod.GET, "/api/v1/posts", "/api/v1/posts/**").permitAll()

                        // 2. Swagger UI 및 API 문서 경로 허용
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/v3/api-docs"
                        ).permitAll()

                        // 3. 그 외 나머지 요청은 인증 필요
                        .anyRequest().authenticated()
                )

                // form/basic 비활성 시 기본이 403이라, 미인증은 401로 명확히 응답
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                );

        return http.build();
    }
}