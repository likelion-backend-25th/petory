package net.likelion.bebc25.projectpatory.mapper;

import net.likelion.bebc25.projectpatory.dto.PaymentRequestDto;
import net.likelion.bebc25.projectpatory.dto.PaymentResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

// 변경: 신규 추가 - PaymentMapper SQL 테스트 (실제 DB 사용)
@SpringBootTest // @MyBatisTest 대신 SpringBootTest 사용
@Transactional // 테스트 완료 후 자동 ROLLBACK
class PaymentMapperTest {

    @Autowired
    private PaymentMapper paymentMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate; // cancel_payment 행 개수 확인용

    // 테스트용 READY 결제 1건 저장 (data.sql 기준: 3번 나비가 2번 멍치에게 후원)
    private PaymentRequestDto saveReadyPayment(String paymentId, int totalAmount) {
        PaymentRequestDto request = PaymentRequestDto.builder()
                .memberId(3L)
                .targetMemberId(2L)
                .paymentId(paymentId)
                .orderName("간식 쏘기 테스트")
                .currency("KRW")
                .totalAmount(totalAmount)
                .payMethod("간식 쏘기")
                .build();
        paymentMapper.savePayment(request);
        return request;
    }

    // 1. 결제 저장 / 조회 테스트
    @Nested
    @DisplayName("savePayment / findByPaymentId 테스트")
    class SaveAndFindTest {

        @Test
        @DisplayName("성공 - READY 상태로 저장되고 생성된 PK(id)를 가져온다")
        void savePayment_Success() {
            // when
            PaymentRequestDto saved = saveReadyPayment("ORD_TEST_SAVE", 5000);

            // then
            assertThat(saved.getId()).isNotNull(); // useGeneratedKeys로 id 세팅 확인

            PaymentResponseDto found = paymentMapper.findByPaymentId("ORD_TEST_SAVE");
            assertThat(found).isNotNull();
            assertThat(found.getMemberId()).isEqualTo(3L);
            assertThat(found.getTargetMemberId()).isEqualTo(2L);
            assertThat(found.getTotalAmount()).isEqualTo(5000);
            assertThat(found.getStatus()).isEqualTo("READY");
            assertThat(found.getPaidAmount()).isNull();
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 주문번호면 null을 반환한다")
        void findByPaymentId_NotFound() {
            PaymentResponseDto found = paymentMapper.findByPaymentId("ORD_NOT_EXIST");

            assertThat(found).isNull();
        }

        @Test
        @DisplayName("성공 - FOR UPDATE 조회도 검증에 필요한 값을 가져온다")
        void findByPaymentIdForUpdate_Success() {
            // given
            saveReadyPayment("ORD_TEST_LOCK", 3000);

            // when
            PaymentResponseDto found = paymentMapper.findByPaymentIdForUpdate("ORD_TEST_LOCK");

            // then
            assertThat(found.getMemberId()).isEqualTo(3L);
            assertThat(found.getTotalAmount()).isEqualTo(3000);
            assertThat(found.getStatus()).isEqualTo("READY");
        }
    }

    // 2. 결제 성공(PAID) 처리 테스트
    @Nested
    @DisplayName("updatePaymentSuccess 테스트")
    class UpdateSuccessTest {

        @Test
        @DisplayName("성공 - READY 결제가 PAID로 바뀌고 거래 정보가 저장된다")
        void updatePaymentSuccess_Success() {
            // given
            saveReadyPayment("ORD_TEST_PAID", 5000);
            LocalDateTime paidAt = LocalDateTime.of(2026, 9, 28, 13, 0, 0);

            // when
            int updatedRows = paymentMapper.updatePaymentSuccess("ORD_TEST_PAID",
                    "tx_test", "pg_test", "https://receipt.example.com/test", 5000, paidAt);

            // then
            assertThat(updatedRows).isEqualTo(1);

            PaymentResponseDto found = paymentMapper.findByPaymentId("ORD_TEST_PAID");
            assertThat(found.getStatus()).isEqualTo("PAID");
            assertThat(found.getTransactionId()).isEqualTo("tx_test");
            assertThat(found.getPgTxId()).isEqualTo("pg_test");
            assertThat(found.getPaidAmount()).isEqualTo(5000);
            assertThat(found.getPaidAt()).isEqualTo(paidAt);
        }

        @Test
        @DisplayName("실패 - 이미 PAID인 결제는 다시 수정되지 않는다 (0행)")
        void updatePaymentSuccess_AlreadyPaid() {
            // given (한 번 PAID로 만들어 둔다)
            saveReadyPayment("ORD_TEST_TWICE", 5000);
            paymentMapper.updatePaymentSuccess("ORD_TEST_TWICE", "tx_first", null, null, 5000, null);

            // when (한 번 더 PAID 처리 시도)
            int updatedRows = paymentMapper.updatePaymentSuccess("ORD_TEST_TWICE", "tx_second", null, null, 5000, null);

            // then
            assertThat(updatedRows).isEqualTo(0);

            PaymentResponseDto found = paymentMapper.findByPaymentId("ORD_TEST_TWICE");
            assertThat(found.getTransactionId()).isEqualTo("tx_first"); // 처음 값이 그대로 남아 있다
        }
    }

    // 3. 결제 실패/취소 처리 테스트
    @Nested
    @DisplayName("updatePaymentFail 테스트")
    class UpdateFailTest {

        @Test
        @DisplayName("성공 - FAILED로 바뀌고 실패 코드/사유가 저장된다")
        void updatePaymentFail_Failed() {
            // given
            saveReadyPayment("ORD_TEST_FAILED", 5000);

            // when
            int updatedRows = paymentMapper.updatePaymentFail("ORD_TEST_FAILED", "FAILED",
                    "PAY_PROCESS_FAILED", "잔액 부족", null, null);

            // then
            assertThat(updatedRows).isEqualTo(1);

            PaymentResponseDto found = paymentMapper.findByPaymentId("ORD_TEST_FAILED");
            assertThat(found.getStatus()).isEqualTo("FAILED");
            assertThat(found.getFailCode()).isEqualTo("PAY_PROCESS_FAILED");
            assertThat(found.getFailMessage()).isEqualTo("잔액 부족");
            assertThat(found.getCancelledAt()).isNull(); // 실패는 취소가 아니므로 취소 시각이 없다
        }

        @Test
        @DisplayName("성공 - CANCELLED로 바뀌고 취소 금액/사유/취소 시각이 저장된다")
        void updatePaymentFail_Cancelled() {
            // given
            saveReadyPayment("ORD_TEST_CANCEL", 5000);

            // when
            int updatedRows = paymentMapper.updatePaymentFail("ORD_TEST_CANCEL", "CANCELLED",
                    "AMOUNT_MISMATCH", "금액 불일치", 1000, "위변조 자동 취소");

            // then
            assertThat(updatedRows).isEqualTo(1);

            PaymentResponseDto found = paymentMapper.findByPaymentId("ORD_TEST_CANCEL");
            assertThat(found.getStatus()).isEqualTo("CANCELLED");
            assertThat(found.getCancelAmount()).isEqualTo(1000);
            assertThat(found.getCancelReason()).isEqualTo("위변조 자동 취소");
            assertThat(found.getCancelledAt()).isNotNull();
        }

        @Test
        @DisplayName("실패 - 이미 PAID인 결제는 CANCELLED로 바뀌지 않는다 (0행)")
        void updatePaymentFail_NotReady() {
            // given (PAID로 만들어 둔다)
            saveReadyPayment("ORD_TEST_PAID_CANCEL", 5000);
            paymentMapper.updatePaymentSuccess("ORD_TEST_PAID_CANCEL", "tx_test", null, null, 5000, null);

            // when
            int updatedRows = paymentMapper.updatePaymentFail("ORD_TEST_PAID_CANCEL", "CANCELLED",
                    "AMOUNT_MISMATCH", "금액 불일치", 5000, "위변조 자동 취소");

            // then
            assertThat(updatedRows).isEqualTo(0);

            PaymentResponseDto found = paymentMapper.findByPaymentId("ORD_TEST_PAID_CANCEL");
            assertThat(found.getStatus()).isEqualTo("PAID");
        }
    }

    // 4. 취소 이력 저장 테스트
    @Test
    @DisplayName("insertCancelPayment - payment.id(PK)를 참조하는 취소 이력이 저장된다")
    void insertCancelPayment_Success() {
        // given
        PaymentRequestDto saved = saveReadyPayment("ORD_TEST_HISTORY", 5000);
        LocalDateTime cancelledAt = LocalDateTime.of(2026, 9, 28, 13, 5, 0);

        // when
        int insertedRows = paymentMapper.insertCancelPayment(saved.getId(), "cancel_test", "pg_cancel_test",
                "SUCCEEDED", 5000, "위변조 자동 취소", "https://receipt.example.com/cancel", cancelledAt);

        // then
        assertThat(insertedRows).isEqualTo(1);

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM cancel_payment WHERE payment_id = ?", Integer.class, saved.getId());
        assertThat(count).isEqualTo(1);
    }
}
