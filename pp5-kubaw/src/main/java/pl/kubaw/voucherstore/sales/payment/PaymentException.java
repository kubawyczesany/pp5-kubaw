package pl.kubaw.voucherstore.sales.payment;

import pl.kubaw.payment.payu.exceptions.PayUException;

public class PaymentException extends IllegalStateException {
    public PaymentException(PayUException e) {
        super(e);
    }
}
