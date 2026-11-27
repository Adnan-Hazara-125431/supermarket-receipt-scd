package dojo.supermarket.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShoppingCart {

    private static final Map<SpecialOfferType, OfferCalculator> CALCULATORS = createCalculators();

    private final List<ProductQuantity> items = new ArrayList<>();
    private final Map<Product, Double> productQuantities = new HashMap<>();

    private static Map<SpecialOfferType, OfferCalculator> createCalculators() {
        Map<SpecialOfferType, OfferCalculator> calculators = new EnumMap<>(SpecialOfferType.class);
        calculators.put(SpecialOfferType.THREE_FOR_TWO, new ThreeForTwoDiscountCalculator());
        calculators.put(SpecialOfferType.TWO_FOR_AMOUNT, new TwoForAmountDiscountCalculator());
        calculators.put(SpecialOfferType.FIVE_FOR_AMOUNT, new FiveForAmountDiscountCalculator());
        calculators.put(SpecialOfferType.TEN_PERCENT_DISCOUNT, new PercentDiscountCalculator());
        return calculators;
    }

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
        return CALCULATORS.get(offer.offerType()).calculate(product, offer, quantity, unitPrice);
    }
}