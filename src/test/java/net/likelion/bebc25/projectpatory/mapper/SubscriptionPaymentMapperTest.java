package net.likelion.bebc25.projectpatory.mapper;

import net.likelion.bebc25.projectpatory.domain.SubscriptionRecord;
import net.likelion.bebc25.projectpatory.dto.SubscriptionRecordCreateRequest;
import net.likelion.bebc25.projectpatory.dto.SubscriptionRecordUpdateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class SubscriptionPaymentMapperTest {

    @Autowired
    private SubscriptionPaymentMapper subscriptionPaymentMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("성공 - 동의(1) 구독은 agreement가 true로 조회된다")
    void getSubscriptionRecord_agreementTrue() {
        SubscriptionRecord found = subscriptionPaymentMapper.getSubscriptionRecordById(1L);

        assertThat(found).isNotNull();
        assertThat(found.getMemberId()).isEqualTo(4L);
        assertThat(found.getTargetMemberId()).isEqualTo(2L);
        assertThat(found.getPlanId()).isEqualTo(2L);
        assertThat(found.getBillingKey()).isEqualTo("billing_coco_001");
        assertThat(found.getStartedAt()).isEqualTo(LocalDate.of(2025, 7, 11));
        assertThat(found.getEndedAt()).isNull();
        assertThat(found.getNextBillingAt()).isEqualTo(LocalDate.of(2025, 8, 11));
        assertThat(found.getStatus()).isEqualTo("ACTIVE");
        assertThat(found.isAgreement()).isTrue();
    }

    @Test
    @DisplayName("성공 - 비동의(0) 구독은 agreement가 false로 조회된다")
    void getSubscriptionRecord_agreementFalse() {
        SubscriptionRecord found = subscriptionPaymentMapper.getSubscriptionRecordById(3L);

        assertThat(found.getStatus()).isEqualTo("CANCELLED");
        assertThat(found.getEndedAt()).isEqualTo(LocalDate.of(2025, 7, 20));
        assertThat(found.getNextBillingAt()).isNull();
        assertThat(found.isAgreement()).isFalse();
    }

    @Test
    @DisplayName("실패 - 없는 구독 id면 null을 반환한다")
    void getSubscriptionRecord_notFound() {
        assertThat(subscriptionPaymentMapper.getSubscriptionRecordById(9999L)).isNull();
    }

    @Test
    @DisplayName("성공 - 구독을 저장하면 다음 결제일과 동의(1)가 기록된다")
    void createSubscriptionRecord_success() {
        LocalDate nextBillingAt = LocalDate.of(2026, 11, 1);
        SubscriptionRecordCreateRequest request = SubscriptionRecordCreateRequest.builder()
                .targetMemberId(2L)
                .planId(1L)
                .billingKey("billing_test_create")
                .build();

        subscriptionPaymentMapper.createSubscriptionRecord(5L, request, nextBillingAt);

        Long id = jdbcTemplate.queryForObject(
                "SELECT id FROM subscription WHERE billing_key = ?", Long.class, "billing_test_create");
        SubscriptionRecord found = subscriptionPaymentMapper.getSubscriptionRecordById(id);

        assertThat(found.getMemberId()).isEqualTo(5L);
        assertThat(found.getTargetMemberId()).isEqualTo(2L);
        assertThat(found.getPlanId()).isEqualTo(1L);
        assertThat(found.getBillingKey()).isEqualTo("billing_test_create");
        assertThat(found.getStartedAt()).isEqualTo(LocalDate.now());
        assertThat(found.getNextBillingAt()).isEqualTo(nextBillingAt);
        assertThat(found.getEndedAt()).isNull();
        assertThat(found.getStatus()).isEqualTo("ACTIVE");
        assertThat(found.isAgreement()).isTrue();
        assertThat(agreementColumn(id)).isEqualTo(1);
    }

    @Test
    @DisplayName("성공 - 동의를 철회하면 DB에는 0, 조회는 false다")
    void updateSubscriptionRecord_disagree() {
        subscriptionPaymentMapper.updateSubscriptionRecord(new SubscriptionRecordUpdateRequest(2L, false));

        assertThat(agreementColumn(2L)).isEqualTo(0);
        assertThat(subscriptionPaymentMapper.getSubscriptionRecordById(2L).isAgreement()).isFalse();
    }

    @Test
    @DisplayName("성공 - 다시 동의하면 DB에는 1, 조회는 true다")
    void updateSubscriptionRecord_agree() {
        subscriptionPaymentMapper.updateSubscriptionRecord(new SubscriptionRecordUpdateRequest(3L, true));

        assertThat(agreementColumn(3L)).isEqualTo(1);
        assertThat(subscriptionPaymentMapper.getSubscriptionRecordById(3L).isAgreement()).isTrue();
    }

    @Test
    @DisplayName("성공 - 해지하면 CANCELLED가 되고 종료일은 오늘, 다음 결제일은 비운다")
    void cancelSubscription_success() {
        subscriptionPaymentMapper.cancelSubscription(2L);

        SubscriptionRecord found = subscriptionPaymentMapper.getSubscriptionRecordById(2L);
        assertThat(found.getStatus()).isEqualTo("CANCELLED");
        assertThat(found.getEndedAt()).isEqualTo(LocalDate.now());
        assertThat(found.getNextBillingAt()).isNull();
    }

    @Test
    @DisplayName("성공 - 회원의 ACTIVE 구독만 조회한다")
    void getSubscriptionRecordsByMemberId_onlyActive() {
        List<SubscriptionRecord> records = subscriptionPaymentMapper.getSubscriptionRecordsByMemberId(4L);

        assertThat(records).extracting(SubscriptionRecord::getId).containsExactly(1L, 6L);
        assertThat(subscriptionPaymentMapper.getSubscriptionRecordsByMemberId(6L)).isEmpty();
    }

    @Test
    @DisplayName("성공 - 동의했고 결제일이 오늘 이전인 ACTIVE 구독만 조회한다")
    void getAllDueSubscriptionRecords_onlyDue() {
        LocalDate future = LocalDate.now().plusMonths(2);
        subscriptionPaymentMapper.createSubscriptionRecord(5L, SubscriptionRecordCreateRequest.builder()
                .targetMemberId(2L)
                .planId(1L)
                .billingKey("billing_test_future")
                .build(), future);
        subscriptionPaymentMapper.updateSubscriptionRecord(new SubscriptionRecordUpdateRequest(1L, false));

        List<Long> dueIds = subscriptionPaymentMapper.getAllDueSubscriptionRecords().stream()
                .map(SubscriptionRecord::getId)
                .toList();

        assertThat(dueIds).contains(2L).doesNotContain(1L, 3L);
        assertThat(dueIds).doesNotContain(
                subscriptionPaymentMapper.getSubscriptionRecordsByMemberId(5L).get(0).getId());
    }

    @Test
    @DisplayName("성공 - 다음 결제일을 바꾼다")
    void renewNextBillingAt_success() {
        LocalDate nextBillingAt = LocalDate.of(2026, 12, 1);

        subscriptionPaymentMapper.renewNextBillingAt(1L, nextBillingAt);

        assertThat(subscriptionPaymentMapper.getSubscriptionRecordById(1L).getNextBillingAt()).isEqualTo(nextBillingAt);
    }

    private Integer agreementColumn(Long id) {
        return jdbcTemplate.queryForObject(
                "SELECT agreement FROM subscription WHERE id = ?", Integer.class, id);
    }
}
