package pl.kubaw.voucherstore.sales.payment;

import pl.kubaw.voucherstore.sales.ordering.Reservation;

import java.util.UUID;

public class InMemoryPaymentGateway implements PaymentGateway {
    @Override
    public PaymentDetails registerFor(Reservation reservation) {
        return PaymentDetails.builder()
                .reservationId(reservation.getId())
                .paymentUrl("some_url")
                .paymentId(UUID.randomUUID().toString())
                .build();
    }

    @Override
    public boolean isTrusted(PaymentUpdateStatusRequest updateStatusRequest) {
        return true;
    }
}
