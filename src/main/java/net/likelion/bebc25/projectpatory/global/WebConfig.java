package net.likelion.bebc25.projectpatory.global;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                // 로컬 개발 환경 및 Vercel/CloudFront 배포 주소 허용
                .allowedOriginPatterns(
                        "http://localhost:5173",
                        "http://localhost:3000",
                        "https://*.vercel.app",
                        "https://*.cloudfront.net"
                )
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}