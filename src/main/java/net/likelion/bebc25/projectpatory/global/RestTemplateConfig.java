package net.likelion.bebc25.projectpatory.global;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

// 변경: 신규 추가 - 외부 API(PortOne) 호출용 RestTemplate을 Bean으로 등록
// 서비스 안에서 new로 만들지 않고 주입받으면, 테스트에서 가짜(Mock) RestTemplate으로 바꿔 끼우기 쉽다
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();

        // PortOne 서버가 응답이 없을 때 무한정 기다리지 않도록 제한 시간을 둔다
        factory.setConnectTimeout(Duration.ofSeconds(3));  // 연결까지 최대 3초
        factory.setReadTimeout(Duration.ofSeconds(10));    // 응답까지 최대 10초

        return new RestTemplate(factory);
    }
}
