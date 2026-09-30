package net.likelion.bebc25.projectpatory.service;

import net.likelion.bebc25.projectpatory.domain.Subscription;
import net.likelion.bebc25.projectpatory.dto.SubscriptionCreateRequest;
import net.likelion.bebc25.projectpatory.dto.SubscriptionUpdateRequest;
import net.likelion.bebc25.projectpatory.mapper.SubscriptionMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceImplTest {

    @Mock
    private SubscriptionMapper subscriptionMapper;

    @InjectMocks
    private SubscriptionServiceImpl subscriptionService;

    @Test
    @DisplayName("본인 회원 아이디로 구독 플랜을 생성한다")
    void createSubscriptionPlan() {
        SubscriptionCreateRequest request = SubscriptionCreateRequest.builder()
                .memberId(2L)
                .planName("베이직")
                .price(4900)
                .description("월간 전용 피드")
                .build();

        subscriptionService.createSubscriptionPlan(request, 2L);

        verify(subscriptionMapper).createSubscription(request);
    }

    @Test
    @DisplayName("다른 회원 명의로 구독 플랜을 생성하면 AccessDeniedException")
    void createSubscriptionPlan_whenOtherMember_throwsAccessDenied() {
        SubscriptionCreateRequest request = SubscriptionCreateRequest.builder()
                .memberId(3L)
                .planName("베이직")
                .price(4900)
                .description("월간 전용 피드")
                .build();

        assertThatThrownBy(() -> subscriptionService.createSubscriptionPlan(request, 2L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("비정상적인 접근입니다.");

        verify(subscriptionMapper, never()).createSubscription(request);
    }

    @Test
    @DisplayName("아이디로 구독 플랜을 조회한다")
    void getSubscriptionById() {
        Subscription plan = Subscription.builder()
                .id(1L)
                .memberId(2L)
                .planName("베이직")
                .price(4900)
                .description("월간 전용 피드")
                .status("ACTIVE")
                .build();
        given(subscriptionMapper.getSubscriptionById(1L)).willReturn(plan);

        Subscription result = subscriptionService.getSubscriptionById(1L);

        assertThat(result).isEqualTo(plan);
    }

    @Test
    @DisplayName("회원 아이디로 구독 플랜 목록을 조회한다")
    void getSubscriptionsByMemberId() {
        List<Subscription> plans = List.of(
                Subscription.builder().id(1L).memberId(2L).planName("베이직").build()
        );
        given(subscriptionMapper.getSubscriptionsByMemberId(2L)).willReturn(plans);

        List<Subscription> result = subscriptionService.getSubscriptionsByMemberId(2L);

        assertThat(result).containsExactlyElementsOf(plans);
    }

    @Test
    @DisplayName("본인 구독 플랜을 수정한다")
    void updateSubscriptionPlan() {
        SubscriptionUpdateRequest request = SubscriptionUpdateRequest.builder()
                .id(1L)
                .planName("프리미엄")
                .price(9900)
                .description("수정 설명")
                .status("ACTIVE")
                .build();
        given(subscriptionMapper.getSubscriptionById(1L)).willReturn(ownedPlan());

        subscriptionService.updateSubscriptionPlan(request, 2L);

        verify(subscriptionMapper).updateSubscription(request);
    }

    @Test
    @DisplayName("없는 구독 플랜을 수정하면 NoSuchElementException")
    void updateSubscriptionPlan_whenMissing_throwsNoSuchElement() {
        SubscriptionUpdateRequest request = SubscriptionUpdateRequest.builder()
                .id(99L)
                .planName("프리미엄")
                .price(9900)
                .description("수정 설명")
                .status("ACTIVE")
                .build();
        given(subscriptionMapper.getSubscriptionById(99L)).willReturn(null);

        assertThatThrownBy(() -> subscriptionService.updateSubscriptionPlan(request, 2L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("더이상 존재하지 않는 구독플랜입니다.");

        verify(subscriptionMapper, never()).updateSubscription(request);
    }

    @Test
    @DisplayName("타인 구독 플랜을 수정하면 AccessDeniedException")
    void updateSubscriptionPlan_whenOtherOwner_throwsAccessDenied() {
        SubscriptionUpdateRequest request = SubscriptionUpdateRequest.builder()
                .id(1L)
                .memberId(2L)
                .planName("프리미엄")
                .price(9900)
                .description("수정 설명")
                .status("ACTIVE")
                .build();
        given(subscriptionMapper.getSubscriptionById(1L)).willReturn(ownedPlan());

        assertThatThrownBy(() -> subscriptionService.updateSubscriptionPlan(request, 3L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("비정상적인 접근입니다.");

        verify(subscriptionMapper, never()).updateSubscription(request);
    }

    @Test
    @DisplayName("본인 구독 플랜을 삭제한다")
    void deleteSubscriptionById() {
        given(subscriptionMapper.getSubscriptionById(1L)).willReturn(ownedPlan());

        subscriptionService.deleteSubscriptionById(1L, 2L);

        verify(subscriptionMapper).deleteSubscriptionById(1L);
    }

    @Test
    @DisplayName("없는 구독 플랜을 삭제하면 NoSuchElementException")
    void deleteSubscriptionById_whenMissing_throwsNoSuchElement() {
        given(subscriptionMapper.getSubscriptionById(99L)).willReturn(null);

        assertThatThrownBy(() -> subscriptionService.deleteSubscriptionById(99L, 2L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("더이상 존재하지 않는 구독플랜입니다.");

        verify(subscriptionMapper, never()).deleteSubscriptionById(99L);
    }

    @Test
    @DisplayName("타인 구독 플랜을 삭제하면 AccessDeniedException")
    void deleteSubscriptionById_whenOtherOwner_throwsAccessDenied() {
        given(subscriptionMapper.getSubscriptionById(1L)).willReturn(ownedPlan());

        assertThatThrownBy(() -> subscriptionService.deleteSubscriptionById(1L, 3L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("비정상적인 접근입니다.");

        verify(subscriptionMapper, never()).deleteSubscriptionById(1L);
    }

    private Subscription ownedPlan() {
        return Subscription.builder()
                .id(1L)
                .memberId(2L)
                .planName("베이직")
                .price(4900)
                .description("월간 전용 피드")
                .status("ACTIVE")
                .build();
    }
}
