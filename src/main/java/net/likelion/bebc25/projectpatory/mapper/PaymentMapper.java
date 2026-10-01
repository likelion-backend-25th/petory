package net.likelion.bebc25.projectpatory.mapper;

import net.likelion.bebc25.projectpatory.domain.Payment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface PaymentMapper {
    // 1단계: 결제 사전 등록 (READY 상태로 저장, 생성된 PK는 payment.id에 채워진다)
    void savePayment(Payment payment);

    // 3단계: 주문번호(payment_id) 기반 조회
    Payment findByPaymentId(@Param("paymentId") String paymentId);

    // 변경: 3단계 검증용 행 잠금 조회 (SELECT ... FOR UPDATE)
    // 같은 결제에 대한 동시 검증 요청(/complete 중복 호출, 웹훅)이 순서대로 처리되도록 트랜잭션 안에서만 사용
    Payment findByPaymentIdForUpdate(@Param("paymentId") String paymentId);

    // 3단계: 검증 성공 시 결제 완료 처리 (PAID)
    // 변경: PortOne 응답의 pgTxId, receiptUrl, paidAt도 저장하도록 파라미터 추가
    int updatePaymentSuccess(@Param("paymentId") String paymentId,
                             @Param("transactionId") String transactionId,
                             @Param("pgTxId") String pgTxId,
                             @Param("receiptUrl") String receiptUrl,
                             @Param("paidAmount") Integer paidAmount,
                             @Param("paidAt") LocalDateTime paidAt);

    // 3단계: 위변조 감지/실패 시 상태 변경 (CANCELLED / FAILED)
    int updatePaymentFail(@Param("paymentId") String paymentId,
                          @Param("status") String status,
                          @Param("failCode") String failCode,
                          @Param("failMessage") String failMessage,
                          @Param("cancelAmount") Integer cancelAmount,
                          @Param("cancelReason") String cancelReason);

    // 변경: 취소 이력 저장 (cancel_payment.payment_id는 payment.id(PK)를 참조)
    int insertCancelPayment(@Param("paymentPk") Long paymentPk,
                            @Param("cancellationId") String cancellationId,
                            @Param("pgCancellationId") String pgCancellationId,
                            @Param("status") String status,
                            @Param("cancelAmount") Integer cancelAmount,
                            @Param("reason") String reason,
                            @Param("receiptUrl") String receiptUrl,
                            @Param("cancelledAt") LocalDateTime cancelledAt);

    // 결제 내역 조회
    List<Payment> findAllByMemberId(@Param("memberId") Long memberId);
}
