
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

    public void addItemQuantity(Product product, double quantity) {

        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }

        if (!Double.isFinite(quantity) || quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be a finite number greater than zero"
            );
        }

        items.add(new ProductQuantity(product, quantity));
    }

    void handleOffers(Receipt receipt, Map<Product, Offer> offers,
                      SupermarketCatalog catalog) {

        Map<Product, Double> quantities = new HashMap<>();

        // Calculate the total quantity of each product
        for (ProductQuantity item : items) {
            quantities.merge(item.product(), item.quantity(), Double::sum);
        }

        // Calculate discounts using the combined quantities
        for (Product product : quantities.keySet()) {
            if (offers.containsKey(product)) {
                Offer offer = offers.get(product);
                double quantity = quantities.get(product);
                double unitPrice = catalog.getUnitPrice(product);

                Discount discount = calculateDiscount(
                        product, offer, quantity, unitPrice
                );

                if (discount != null) {
                    receipt.addDiscount(discount);
                }
            }
        }
    }

    private Discount calculateDiscount(Product product, Offer offer,
                                        double quantity, double unitPrice) {
        return CALCULATORS.get(offer.offerType())
                .calculate(product, offer, quantity, unitPrice);
    }
}
