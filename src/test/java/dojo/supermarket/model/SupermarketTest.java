package dojo.supermarket.model;

import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Characterization tests (Feathers): they record what the code does TODAY,
 * so we can refactor safely. Where today's behavior is wrong, the test says
 * "BUG" and keeps the current result. We fix those later (Robustness phase).
 */
class SupermarketTest {

    private static final double DELTA = 0.001;

    private final Product toothbrush = new Product("toothbrush", ProductUnit.EACH); // price 0.99
    private final Product apples = new Product("apples", ProductUnit.KILO);         // price 1.99

    private Teller newTeller() {
        SupermarketCatalog catalog = new FakeCatalog();
        catalog.addProduct(toothbrush, 0.99);
        catalog.addProduct(apples, 1.99);
        return new Teller(catalog);
    }

    /** ARRANGE + ACT helper: one product, one optional offer, one quantity. */
    private Receipt checkout(SpecialOfferType offerType, double argument, Product product, double quantity) {
        Teller teller = newTeller();
        if (offerType != null) {
            teller.addSpecialOffer(offerType, product, argument);
        }
        ShoppingCart cart = new ShoppingCart();
        cart.addItemQuantity(product, quantity);
        return teller.checksOutArticlesFrom(cart);
    }

    private void assertNoDiscount(Receipt receipt) {
        assertEquals(Collections.emptyList(), receipt.getDiscounts());
    }

    private void assertSingleDiscount(Receipt receipt, String description, double amount) {
        assertEquals(1, receipt.getDiscounts().size());
        Discount discount = receipt.getDiscounts().get(0);
        assertEquals(description, discount.description());
        assertEquals(amount, discount.discountAmount(), DELTA);
    }

    // ---------- the original test, renamed: it never tested an applied offer ----------

    @Test
    void offerOnAnotherProduct_doesNotChangeTheCart() {
        Teller teller = newTeller();
        teller.addSpecialOffer(SpecialOfferType.TEN_PERCENT_DISCOUNT, toothbrush, 10.0);
        ShoppingCart cart = new ShoppingCart();
        cart.addItemQuantity(apples, 2.5);

        Receipt receipt = teller.checksOutArticlesFrom(cart);

        assertEquals(4.975, receipt.getTotalPrice(), 0.01);
        assertNoDiscount(receipt);
        assertEquals(1, receipt.getItems().size());
        ReceiptItem item = receipt.getItems().get(0);
        assertEquals(apples, item.product());
        assertEquals(1.99, item.price());
        assertEquals(2.5 * 1.99, item.totalPrice());
        assertEquals(2.5, item.quantity());
    }

    // ---------- 10% discount ----------

    @Test
    void tenPercentDiscount_isAppliedToTheOfferedProduct() {
        Receipt receipt = checkout(SpecialOfferType.TEN_PERCENT_DISCOUNT, 10.0, apples, 2.5);

        assertEquals(4.4775, receipt.getTotalPrice(), DELTA);   // 4.975 - 10%
        assertSingleDiscount(receipt, "10.0% off", -0.4975);
    }

    // ---------- 3 for 2 ----------

    @Test
    void threeForTwo_noDiscountBelowThreeItems() {
        Receipt receipt = checkout(SpecialOfferType.THREE_FOR_TWO, 0, toothbrush, 2);

        assertEquals(1.98, receipt.getTotalPrice(), DELTA);
        assertNoDiscount(receipt);
    }

    @Test
    void threeForTwo_threeItemsPayForTwo() {
        Receipt receipt = checkout(SpecialOfferType.THREE_FOR_TWO, 0, toothbrush, 3);

        assertEquals(1.98, receipt.getTotalPrice(), DELTA);
        assertSingleDiscount(receipt, "3 for 2", -0.99);
    }

    @Test
    void threeForTwo_fourItemsPayForThree() {
        Receipt receipt = checkout(SpecialOfferType.THREE_FOR_TWO, 0, toothbrush, 4);

        assertEquals(2.97, receipt.getTotalPrice(), DELTA);
        assertSingleDiscount(receipt, "3 for 2", -0.99);
    }

    @Test
    void threeForTwo_sixItemsPayForFour() {
        Receipt receipt = checkout(SpecialOfferType.THREE_FOR_TWO, 0, toothbrush, 6);

        assertEquals(3.96, receipt.getTotalPrice(), DELTA);
        assertSingleDiscount(receipt, "3 for 2", -1.98);
    }

    // ---------- 2 for amount ----------

    @Test
    void twoForAmount_noDiscountForOneItem() {
        Receipt receipt = checkout(SpecialOfferType.TWO_FOR_AMOUNT, 1.50, toothbrush, 1);

        assertEquals(0.99, receipt.getTotalPrice(), DELTA);
        assertNoDiscount(receipt);
    }

    @Test
    void twoForAmount_twoItemsCostTheOfferPrice() {
        Receipt receipt = checkout(SpecialOfferType.TWO_FOR_AMOUNT, 1.50, toothbrush, 2);

        assertEquals(1.50, receipt.getTotalPrice(), DELTA);
        assertSingleDiscount(receipt, "2 for 1.5", -0.48);       // 1.98 - 1.50
    }

    @Test
    void twoForAmount_threeItemsCostOfferPricePlusOneFullPrice() {
        Receipt receipt = checkout(SpecialOfferType.TWO_FOR_AMOUNT, 1.50, toothbrush, 3);

        assertEquals(2.49, receipt.getTotalPrice(), DELTA);      // 1.50 + 0.99
        assertSingleDiscount(receipt, "2 for 1.5", -0.48);
    }

    // ---------- 5 for amount ----------

    @Test
    void fiveForAmount_noDiscountForFourItems() {
        Receipt receipt = checkout(SpecialOfferType.FIVE_FOR_AMOUNT, 4.00, toothbrush, 4);

        assertEquals(3.96, receipt.getTotalPrice(), DELTA);
        assertNoDiscount(receipt);
    }

    @Test
    void fiveForAmount_fiveItemsCostTheOfferPrice() {
        Receipt receipt = checkout(SpecialOfferType.FIVE_FOR_AMOUNT, 4.00, toothbrush, 5);

        assertEquals(4.00, receipt.getTotalPrice(), DELTA);
        assertSingleDiscount(receipt, "5 for 4.0", -0.95);       // 4.95 - 4.00
    }

    @Test
    void fiveForAmount_sevenItemsCostOfferPricePlusTwoFullPrice() {
        Receipt receipt = checkout(SpecialOfferType.FIVE_FOR_AMOUNT, 4.00, toothbrush, 7);

        assertEquals(5.98, receipt.getTotalPrice(), DELTA);      // 4.00 + 2 * 0.99
        assertSingleDiscount(receipt, "5 for 4.0", -0.95);
    }

    // ---------- bad input: records TODAY's (wrong) behavior; fixed later in Robustness ----------

    @Test
    void negativeQuantity_isAccepted_BUG() {
        Receipt receipt = checkout(null, 0, toothbrush, -3);

        assertEquals(-2.97, receipt.getTotalPrice(), DELTA);     // BUG: should be rejected
    }

    @Test
    void zeroQuantity_isAcceptedAndListed_BUG() {
        Receipt receipt = checkout(null, 0, toothbrush, 0);

        assertEquals(0.0, receipt.getTotalPrice(), DELTA);
        assertEquals(1, receipt.getItems().size());              // BUG: a "0 items" line appears
    }

    @Test
    void nanQuantity_givesNanTotal_BUG() {
        Receipt receipt = checkout(null, 0, toothbrush, Double.NaN);

        assertTrue(Double.isNaN(receipt.getTotalPrice()));       // BUG: should be rejected
    }

    @Test
    void nullProduct_crashesWithNullPointerException_BUG() {
        Teller teller = newTeller();
        ShoppingCart cart = new ShoppingCart();
        cart.addItem(null);

        assertThrows(NullPointerException.class,
                () -> teller.checksOutArticlesFrom(cart));       // BUG: should be a clear error
    }
}