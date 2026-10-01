package net.likelion.bebc25.projectpatory.dto;

public record PortOneBillingKeyPaymentRequest(
        String billingKey,
        String orderName,
        Amount amount,
        String currency
) {
    public record Amount(int total) {}

    ;
}
