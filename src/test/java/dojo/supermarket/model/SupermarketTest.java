
package dojo.supermarket.model;

import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Characterization and robustness tests for the supermarket application.
 */
class SupermarketTest {

    private static final double DELTA = 0.001;

    private final Product toothbrush = new Product("toothbrush", ProductUnit.EACH);
    private final Product apples = new Product("apples", ProductUnit.KILO);

    private Teller newTeller() {
        SupermarketCatalog catalog = new FakeCatalog();
        catalog.addProduct(toothbrush, 0.99);
        catalog.addProduct(apples, 1.99);
        return new Teller(catalog);
    }

    /** ARRANGE + ACT helper: one product, one optional offer, one quantity. */
    private Receipt checkout(SpecialOfferType offerType, double argument,
                             Product product, double quantity) {
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

    private void assertSingleDiscount(Receipt receipt,
                                      String description, double amount) {
        assertEquals(1, receipt.getDiscounts().size());

        Discount discount = receipt.getDiscounts().get(0);

        assertEquals(description, discount.description());
        assertEquals(amount, discount.discountAmount(), DELTA);
    }

    // ---------- Original checkout test ----------

    @Test
    void offerOnAnotherProduct_doesNotChangeTheCart() {
        Teller teller = newTeller();

        teller.addSpecialOffer(
                SpecialOfferType.TEN_PERCENT_DISCOUNT, toothbrush, 10.0
        );

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
        Receipt receipt = checkout(
                SpecialOfferType.TEN_PERCENT_DISCOUNT, 10.0, apples, 2.5
        );

        assertEquals(4.4775, receipt.getTotalPrice(), DELTA);
        assertSingleDiscount(receipt, "10.0% off", -0.4975);
    }

    // ---------- 3 for 2 ----------

    @Test
    void threeForTwo_noDiscountBelowThreeItems() {
        Receipt receipt = checkout(
                SpecialOfferType.THREE_FOR_TWO, 0, toothbrush, 2
        );

        assertEquals(1.98, receipt.getTotalPrice(), DELTA);
        assertNoDiscount(receipt);
    }

    @Test
    void threeForTwo_threeItemsPayForTwo() {
        Receipt receipt = checkout(
                SpecialOfferType.THREE_FOR_TWO, 0, toothbrush, 3
        );

        assertEquals(1.98, receipt.getTotalPrice(), DELTA);
        assertSingleDiscount(receipt, "3 for 2", -0.99);
    }

    @Test
    void threeForTwo_fourItemsPayForThree() {
        Receipt receipt = checkout(
                SpecialOfferType.THREE_FOR_TWO, 0, toothbrush, 4
        );

        assertEquals(2.97, receipt.getTotalPrice(), DELTA);
        assertSingleDiscount(receipt, "3 for 2", -0.99);
    }

    @Test
    void threeForTwo_sixItemsPayForFour() {
        Receipt receipt = checkout(
                SpecialOfferType.THREE_FOR_TWO, 0, toothbrush, 6
        );

        assertEquals(3.96, receipt.getTotalPrice(), DELTA);
        assertSingleDiscount(receipt, "3 for 2", -1.98);
    }

    // ---------- 2 for amount ----------

    @Test
    void twoForAmount_noDiscountForOneItem() {
        Receipt receipt = checkout(
                SpecialOfferType.TWO_FOR_AMOUNT, 1.50, toothbrush, 1
        );

        assertEquals(0.99, receipt.getTotalPrice(), DELTA);
        assertNoDiscount(receipt);
    }

    @Test
    void twoForAmount_twoItemsCostTheOfferPrice() {
        Receipt receipt = checkout(
                SpecialOfferType.TWO_FOR_AMOUNT, 1.50, toothbrush, 2
        );

        assertEquals(1.50, receipt.getTotalPrice(), DELTA);
        assertSingleDiscount(receipt, "2 for 1.5", -0.48);
    }

    @Test
    void twoForAmount_threeItemsCostOfferPricePlusOneFullPrice() {
        Receipt receipt = checkout(
                SpecialOfferType.TWO_FOR_AMOUNT, 1.50, toothbrush, 3
        );

        assertEquals(2.49, receipt.getTotalPrice(), DELTA);
        assertSingleDiscount(receipt, "2 for 1.5", -0.48);
    }

    // ---------- 5 for amount ----------

    @Test
    void fiveForAmount_noDiscountForFourItems() {
        Receipt receipt = checkout(
                SpecialOfferType.FIVE_FOR_AMOUNT, 4.00, toothbrush, 4
        );

        assertEquals(3.96, receipt.getTotalPrice(), DELTA);
        assertNoDiscount(receipt);
    }

    @Test
    void fiveForAmount_fiveItemsCostTheOfferPrice() {
        Receipt receipt = checkout(
                SpecialOfferType.FIVE_FOR_AMOUNT, 4.00, toothbrush, 5
        );

        assertEquals(4.00, receipt.getTotalPrice(), DELTA);
        assertSingleDiscount(receipt, "5 for 4.0", -0.95);
    }

    @Test
    void fiveForAmount_sevenItemsCostOfferPricePlusTwoFullPrice() {
        Receipt receipt = checkout(
                SpecialOfferType.FIVE_FOR_AMOUNT, 4.00, toothbrush, 7
        );

        assertEquals(5.98, receipt.getTotalPrice(), DELTA);
        assertSingleDiscount(receipt, "5 for 4.0", -0.95);
    }

    // ---------- Input validation tests ----------

    @Test
    void negativeQuantity_isRejected() {
    ShoppingCart cart = new ShoppingCart();

    IllegalArgumentException exception =
            assertThrows(
                    IllegalArgumentException.class,
                    () -> cart.addItemQuantity(toothbrush, -3));

    assertEquals(
            "Quantity must be a finite number greater than zero",
            exception.getMessage());
   }

    @Test
void zeroQuantity_isRejected() {
    ShoppingCart cart = new ShoppingCart();

    IllegalArgumentException exception =
            assertThrows(
                    IllegalArgumentException.class,
                    () -> cart.addItemQuantity(toothbrush, 0));

    assertEquals(
            "Quantity must be a finite number greater than zero",
            exception.getMessage());
}

    @Test
void nanQuantity_isRejected() {
    ShoppingCart cart = new ShoppingCart();

    IllegalArgumentException exception =
            assertThrows(
                    IllegalArgumentException.class,
                    () -> cart.addItemQuantity(toothbrush, Double.NaN));

    assertEquals(
            "Quantity must be a finite number greater than zero",
            exception.getMessage());
}

   @Test
    void nullProduct_isRejected() {
    ShoppingCart cart = new ShoppingCart();

    IllegalArgumentException exception =
            assertThrows(
                    IllegalArgumentException.class,
                    () -> cart.addItemQuantity(null, 2));

    assertEquals(
            "Product cannot be null",
            exception.getMessage());
}
@Test
void positiveInfinity_isRejected() {
    ShoppingCart cart = new ShoppingCart();

    IllegalArgumentException exception =
            assertThrows(
                    IllegalArgumentException.class,
                    () -> cart.addItemQuantity(toothbrush, Double.POSITIVE_INFINITY));

    assertEquals(
            "Quantity must be a finite number greater than zero",
            exception.getMessage());
}

    @Test
void negativeInfinity_isRejected() {
    ShoppingCart cart = new ShoppingCart();

    IllegalArgumentException exception =
            assertThrows(
                    IllegalArgumentException.class,
                    () -> cart.addItemQuantity(toothbrush, Double.NEGATIVE_INFINITY));

    assertEquals(
            "Quantity must be a finite number greater than zero",
            exception.getMessage());
}
}
