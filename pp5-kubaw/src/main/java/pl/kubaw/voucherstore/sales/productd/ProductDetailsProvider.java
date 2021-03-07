package pl.kubaw.voucherstore.sales.productd;

public interface ProductDetailsProvider {
    ProductDetails getByProductId(String productId);
}
