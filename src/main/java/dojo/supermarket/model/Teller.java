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

    public void addSpecialOffer(SpecialOfferType offerType, Product product, double offerValue) {
        if (offerType == SpecialOfferType.TEN_PERCENT_DISCOUNT) {
            if (offerValue < 0 || offerValue > 100 || Double.isNaN(offerValue)) {
                throw new IllegalArgumentException("Discount percentage must be between 0 and 100");
            }
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
            double unitPrice = catalog.getUnitPrice(product);
            double totalPrice = calculateTotalPrice(quantity, unitPrice);
            ReceiptItem item = new ReceiptItem(product, quantity, unitPrice, totalPrice);
            receipt.addProduct(item);
        }
        cart.handleOffers(receipt, offers, catalog);

        return receipt;
    }
}
