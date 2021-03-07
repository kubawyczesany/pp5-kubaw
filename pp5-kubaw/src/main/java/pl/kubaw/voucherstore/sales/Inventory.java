package pl.kubaw.voucherstore.sales;

public interface Inventory {
    boolean isAvailable(String productId);
}
