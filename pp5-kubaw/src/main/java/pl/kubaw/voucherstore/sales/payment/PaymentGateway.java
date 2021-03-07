package pl.kubaw.voucherstore.sales.payment;

import pl.kubaw.payment.payu.exceptions.PayUException;
import pl.kubaw.voucherstore.sales.ordering.Reservation;

public interface PaymentGateway {
    PaymentDetails registerFor(Reservation reservation) throws PayUException;

    boolean isTrusted(PaymentUpdateStatusRequest updateStatusRequest);
}
