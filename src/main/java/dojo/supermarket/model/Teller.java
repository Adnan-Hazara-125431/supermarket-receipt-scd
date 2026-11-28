package dojo.supermarket.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Teller {

    private final SupermarketCatalog catalog;
    private final Map<Product, Offer> offers = new HashMap<>();

    public Teller(SupermarketCatalog catalog) {
        this.catalog = catalog;
    }

    private double getProductPrice(Product product) {
        try {
            return catalog.getUnitPrice(product);
        }
        catch (NullPointerException e) {
            throw new IllegalArgumentException("Product " + product.name() + " was not found in the catalog", e);
        }
    }

    private void validatePromotionalPrice(Product product, double offerValue, int quantity, String errorMessage) {
        double normalPrice = getProductPrice(product) * quantity;
        if (offerValue > normalPrice || offerValue < 0 || Double.isNaN(offerValue)) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    public void addSpecialOffer(SpecialOfferType offerType, Product product, double offerValue) {
        if (offers.containsKey(product)) {
            throw new IllegalArgumentException("An offer already exists for " + product.name());
        }
        if (offerType == SpecialOfferType.TEN_PERCENT_DISCOUNT) {
            getProductPrice(product);
            if (offerValue < 0 || offerValue > 100 || Double.isNaN(offerValue)) {
                throw new IllegalArgumentException("Discount percentage must be between 0 and 100");
            }
        }
        if (offerType == SpecialOfferType.FIVE_FOR_AMOUNT) {
            validatePromotionalPrice(product, offerValue, 5, "Invalid promotional price for five products");
        }
        if (offerType == SpecialOfferType.TWO_FOR_AMOUNT) {
            validatePromotionalPrice(product, offerValue, 2, "Invalid promotional price for two products");
        }
        offers.put(product, new Offer(offerType, product, offerValue));
    }

    private double calculateTotalPrice(double quantity, double unitPrice) {
        return quantity * unitPrice;
    }

    public Receipt checksOutArticlesFrom(ShoppingCart cart) {
        Receipt receipt = new Receipt();
        List<ProductQuantity> productQuantities = cart.getItems();
        for (ProductQuantity productQuantity: productQuantities) {
            Product product = productQuantity.product();
            double quantity = productQuantity.quantity();
            double unitPrice = getProductPrice(product);
            double totalPrice = calculateTotalPrice(quantity, unitPrice);
            ReceiptItem item = new ReceiptItem(product, quantity, unitPrice, totalPrice);
            receipt.addProduct(item);
        }
        cart.handleOffers(receipt, offers, catalog);

        return receipt;
    }
}
