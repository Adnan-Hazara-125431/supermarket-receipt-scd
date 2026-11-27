package dojo.supermarket.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShoppingCart {
    private static final int TWO_ITEMS = 2;
    private static final int THREE_ITEMS = 3;
    private static final int FIVE_ITEMS = 5;
    private static final double PERCENT_DIVISOR = 100.0;

    private final List<ProductQuantity> items = new ArrayList<>();
    private final Map<Product, Double> productQuantities = new HashMap<>();

    List<ProductQuantity> getItems() {
        return Collections.unmodifiableList(items);
    }

    void addItem(Product product) {
        addItemQuantity(product, 1.0);
    }

    Map<Product, Double> productQuantities() {
        return Collections.unmodifiableMap(productQuantities);
    }

    public void addItemQuantity(Product product, double quantity) {
        items.add(new ProductQuantity(product, quantity));
        if (productQuantities.containsKey(product)) {
            productQuantities.put(product, productQuantities.get(product) + quantity);
        } else {
            productQuantities.put(product, quantity);
        }
    }

    void handleOffers(Receipt receipt, Map<Product, Offer> offers, SupermarketCatalog catalog) {
        for (Product product: productQuantities().keySet()) {
            double quantity = productQuantities.get(product);
            if (offers.containsKey(product)) {
                Offer offer = offers.get(product);
                double unitPrice = catalog.getUnitPrice(product);
                int quantityAsInt = (int) quantity;
                Discount discount = null;
                int offerQuantity = 1;
                if (offer.offerType() == SpecialOfferType.THREE_FOR_TWO) {
                    offerQuantity =  THREE_ITEMS;

                } else if (offer.offerType() == SpecialOfferType.TWO_FOR_AMOUNT) {
                    offerQuantity = TWO_ITEMS;
                    if (quantityAsInt >= TWO_ITEMS) {
                        double total = offer.argument() * (quantityAsInt / offerQuantity) + quantityAsInt % TWO_ITEMS * unitPrice;
                        double discountAmount = unitPrice * quantity - total;
                        discount = new Discount(product, "2 for " + offer.argument(), -discountAmount);
                    }

                } if (offer.offerType() == SpecialOfferType.FIVE_FOR_AMOUNT) {
                    offerQuantity = FIVE_ITEMS;
                }
                int numberOfOfferGroups = quantityAsInt / offerQuantity;
                if (offer.offerType() == SpecialOfferType.THREE_FOR_TWO && quantityAsInt > TWO_ITEMS) {
                    double discountAmount = quantity * unitPrice - ((numberOfOfferGroups * TWO_ITEMS * unitPrice) + quantityAsInt % THREE_ITEMS * unitPrice);
                    discount = new Discount(product, "3 for 2", -discountAmount);
                }
                if (offer.offerType() == SpecialOfferType.TEN_PERCENT_DISCOUNT) {
                    discount = new Discount(product, offer.argument() + "% off", -quantity * unitPrice * offer.argument() / PERCENT_DIVISOR);
                }
                if (offer.offerType() == SpecialOfferType.FIVE_FOR_AMOUNT && quantityAsInt >= FIVE_ITEMS) {
                    double discountTotal = unitPrice * quantity - (offer.argument() * numberOfOfferGroups + quantityAsInt % FIVE_ITEMS * unitPrice);
                    discount = new Discount(product, offerQuantity + " for " + offer.argument(), -discountTotal);
                }
                if (discount != null)
                    receipt.addDiscount(discount);
            }
        }
    }
}
