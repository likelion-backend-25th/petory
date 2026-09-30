package net.likelion.bebc25.projectpatory.mapper;

import net.likelion.bebc25.projectpatory.domain.Subscription;
import net.likelion.bebc25.projectpatory.dto.SubscriptionCreateRequest;
import net.likelion.bebc25.projectpatory.dto.SubscriptionUpdateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class SubscriptionMapperTest {

    @Autowired
    private SubscriptionMapper subscriptionMapper;

    @Test
    @DisplayName("구독 플랜을 저장하면 ACTIVE 상태로 생성된다")
    void createSubscription() {
        SubscriptionCreateRequest request = SubscriptionCreateRequest.builder()
                .memberId(2L)
                .planName("신규플랜")
                .price(5900)
                .description("테스트 설명")
                .build();

        subscriptionMapper.createSubscription(request);

        Subscription created = subscriptionMapper.getSubscriptionsByMemberId(2L).stream()
                .filter(plan -> "신규플랜".equals(plan.getPlanName()))
                .findFirst()
                .orElseThrow();

        assertThat(created.getId()).isPositive();
        assertThat(created.getMemberId()).isEqualTo(2L);
        assertThat(created.getPrice()).isEqualTo(5900);
        assertThat(created.getDescription()).isEqualTo("테스트 설명");
        assertThat(created.getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("아이디로 구독 플랜을 조회한다")
    void getSubscriptionById() {
        Subscription plan = subscriptionMapper.getSubscriptionById(1L);

        assertThat(plan).isNotNull();
        assertThat(plan.getId()).isEqualTo(1L);
        assertThat(plan.getMemberId()).isEqualTo(2L);
        assertThat(plan.getPlanName()).isEqualTo("베이직");
        assertThat(plan.getPrice()).isEqualTo(4900);
        assertThat(plan.getDescription()).isEqualTo("월간 전용 피드 + 감사 메시지");
        assertThat(plan.getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("없는 아이디로 조회하면 null을 반환한다")
    void getSubscriptionById_whenMissing_returnsNull() {
        assertThat(subscriptionMapper.getSubscriptionById(9999L)).isNull();
    }

    @Test
    @DisplayName("회원 아이디로 해당 회원의 구독 플랜 목록을 조회한다")
    void getSubscriptionsByMemberId() {
        List<Subscription> plans = subscriptionMapper.getSubscriptionsByMemberId(2L);

        assertThat(plans)
                .extracting(Subscription::getPlanName)
                .containsExactlyInAnyOrder("베이직", "프리미엄", "올드플랜");
    }

    @Test
    @DisplayName("구독 플랜 이름, 설명, 상태를 수정하고 가격은 유지한다")
    void updateSubscription() {
        SubscriptionUpdateRequest request = SubscriptionUpdateRequest.builder()
                .id(4L)
                .planName("수정플랜")
                .description("수정 설명")
                .status("INACTIVE")
                .build();

        subscriptionMapper.updateSubscription(request);

        Subscription updated = subscriptionMapper.getSubscriptionById(4L);
        assertThat(updated.getPlanName()).isEqualTo("수정플랜");
        assertThat(updated.getPrice()).isEqualTo(2900);
        assertThat(updated.getDescription()).isEqualTo("수정 설명");
        assertThat(updated.getStatus()).isEqualTo("INACTIVE");
        assertThat(updated.getMemberId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("아이디로 구독 플랜을 삭제한다")
    void deleteSubscriptionById() {
        subscriptionMapper.deleteSubscriptionById(4L);

        assertThat(subscriptionMapper.getSubscriptionById(4L)).isNull();
    }
}
