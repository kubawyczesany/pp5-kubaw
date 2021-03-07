package pl.kubaw.voucherstore.sales;

import pl.kubaw.payment.payu.exceptions.PayUException;
import pl.kubaw.voucherstore.productcatalog.Product;
import pl.kubaw.voucherstore.productcatalog.ProductCatalogFacade;
import pl.kubaw.voucherstore.sales.basket.Basket;
import pl.kubaw.voucherstore.sales.basket.InMemoryBasketStorage;
import pl.kubaw.voucherstore.sales.offer.Offer;
import pl.kubaw.voucherstore.sales.offer.OfferMaker;
import pl.kubaw.voucherstore.sales.ordering.Reservation;
import pl.kubaw.voucherstore.sales.ordering.ReservationRepository;
import pl.kubaw.voucherstore.sales.payment.PaymentDetails;
import pl.kubaw.voucherstore.sales.payment.PaymentGateway;
import pl.kubaw.voucherstore.sales.payment.PaymentUpdateStatusRequest;
import pl.kubaw.voucherstore.sales.payment.UntrustedPaymentException;

public class SalesFacade {
    private final InMemoryBasketStorage basketStorage;
    private final ProductCatalogFacade productCatalogFacade;
    private final CurrentCustomerContext currentCustomerContext;
    private final Inventory inventory;
    private final OfferMaker offerMaker;
    private final PaymentGateway paymentGateway;
    private final ReservationRepository reservationRepository;

    public SalesFacade(InMemoryBasketStorage basketStorage, ProductCatalogFacade productCatalogFacade, CurrentCustomerContext currentCustomerContext, Inventory inventory, OfferMaker offerMaker, PaymentGateway paymentGateway, ReservationRepository reservationRepository) {
        this.basketStorage = basketStorage;
        this.productCatalogFacade = productCatalogFacade;
        this.currentCustomerContext = currentCustomerContext;
        this.inventory = inventory;
        this.offerMaker = offerMaker;
        this.paymentGateway = paymentGateway;
        this.reservationRepository = reservationRepository;
    }

    public void addToBasket(String productId1) {
        Basket basket = basketStorage.loadForCustomer(currentCustomerContext.getCustomerId())
                .orElseGet(Basket::empty);

        Product product = productCatalogFacade.getById(productId1);

        basket.add(product, inventory);

        basketStorage.addForCustomer(currentCustomerContext.getCustomerId(), basket);
    }

    public Offer getCurrentOffer() {
        Basket basket = basketStorage.loadForCustomer(currentCustomerContext.getCustomerId())
                .orElseGet(Basket::empty);

        return offerMaker.calculateOffer(basket.getBasketItems());
    }

    public PaymentDetails acceptOffer(ClientDetails clientDetails, Offer seenOffer) throws PayUException {
        Basket basket = basketStorage.loadForCustomer(currentCustomerContext.getCustomerId())
                .orElseGet(Basket::empty);

        Offer currentOffer = offerMaker.calculateOffer(basket.getBasketItems());

        if (!seenOffer.isSameTotal(currentOffer)) {
            throw new OfferChangedException();
        }

        Reservation reservation = Reservation.of(currentOffer, clientDetails);

        var paymentDetails = paymentGateway.registerFor(reservation);
        reservation.fillPaymentDetails(paymentDetails);

        reservationRepository.save(reservation);

        return paymentDetails;
    }

    public void handlePaymentStatusChange(PaymentUpdateStatusRequest updateStatusRequest) {
        if (!paymentGateway.isTrusted(updateStatusRequest)) {
            throw new UntrustedPaymentException();
        }
    }
}
