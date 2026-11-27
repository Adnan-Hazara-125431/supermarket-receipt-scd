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
        for (Product product : productQuantities().keySet()) {
            if (offers.containsKey(product)) {
                Offer offer = offers.get(product);
                double quantity = productQuantities.get(product);
                double unitPrice = catalog.getUnitPrice(product);

                Discount discount = calculateDiscount(product, offer, quantity, unitPrice);

                if (discount != null) {
                    receipt.addDiscount(discount);
                }
            }
        }
    }

    private Discount calculateDiscount(Product product, Offer offer, double quantity, double unitPrice) {
        return switch (offer.offerType()) {
            case THREE_FOR_TWO -> calculateThreeForTwoDiscount(product, quantity, unitPrice);
            case TWO_FOR_AMOUNT -> calculateTwoForAmountDiscount(product, offer, quantity, unitPrice);
            case FIVE_FOR_AMOUNT -> calculateFiveForAmountDiscount(product, offer, quantity, unitPrice);
            case TEN_PERCENT_DISCOUNT -> calculateTenPercentDiscount(product, offer, quantity, unitPrice);
        };
    }

    private Discount calculateThreeForTwoDiscount(Product product, double quantity, double unitPrice) {
        int quantityAsInt = (int) quantity;
        if (quantityAsInt < THREE_ITEMS) {
            return null;
        }

        int numberOfOfferGroups = quantityAsInt / THREE_ITEMS;
        double discountAmount =
                quantity * unitPrice
                        - ((numberOfOfferGroups * TWO_ITEMS * unitPrice)
                        + quantityAsInt % THREE_ITEMS * unitPrice);

        return new Discount(product, "3 for 2", -discountAmount);
    }

    private Discount calculateTwoForAmountDiscount(Product product, Offer offer, double quantity, double unitPrice) {
        int quantityAsInt = (int) quantity;
        if (quantityAsInt < TWO_ITEMS) {
            return null;
        }

        double total =
                offer.argument() * (quantityAsInt / TWO_ITEMS)
                        + quantityAsInt % TWO_ITEMS * unitPrice;
        double discountAmount = unitPrice * quantity - total;

        return new Discount(product, "2 for " + offer.argument(), -discountAmount);
    }

    private Discount calculateFiveForAmountDiscount(Product product, Offer offer, double quantity, double unitPrice) {
        int quantityAsInt = (int) quantity;
        if (quantityAsInt < FIVE_ITEMS) {
            return null;
        }

        int numberOfOfferGroups = quantityAsInt / FIVE_ITEMS;
        double discountAmount =
                unitPrice * quantity
                        - (offer.argument() * numberOfOfferGroups
                        + quantityAsInt % FIVE_ITEMS * unitPrice);

        return new Discount(product, FIVE_ITEMS + " for " + offer.argument(), -discountAmount);
    }

    private Discount calculateTenPercentDiscount(Product product, Offer offer, double quantity, double unitPrice) {
        return new Discount(
                product,
                offer.argument() + "% off",
                -quantity * unitPrice * offer.argument() / PERCENT_DIVISOR);
    }
}