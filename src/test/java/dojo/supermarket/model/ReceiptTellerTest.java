package dojo.supermarket.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class ReceiptTellerTest {

    @Test
    void sameProductAddedTwice() {

        // Arrange
        SupermarketCatalog catalog = new FakeCatalog();
        Product milk = new Product("milk", ProductUnit.EACH);
        catalog.addProduct(milk, 100.0);

        Teller teller = new Teller(catalog);
        ShoppingCart cart = new ShoppingCart();

        cart.addItemQuantity(milk, 2);
        cart.addItemQuantity(milk, 3);

        // Act
        Receipt receipt = teller.checksOutArticlesFrom(cart);

        // Assert
        assertEquals(2, receipt.getItems().size());
        assertEquals(500.0, receipt.getTotalPrice(), 0.01);
        assertEquals(2.0, receipt.getItems().get(0).quantity());
        assertEquals(3.0, receipt.getItems().get(1).quantity());

    }

    @Test
    void twoOffersOnSameProduct() {

        // Arrange
        SupermarketCatalog catalog = new FakeCatalog();
        Product milk = new Product("milk", ProductUnit.EACH);
        catalog.addProduct(milk, 100.0);

        Teller teller = new Teller(catalog);

        teller.addSpecialOffer(SpecialOfferType.TEN_PERCENT_DISCOUNT, milk, 10.0);
        teller.addSpecialOffer(SpecialOfferType.TEN_PERCENT_DISCOUNT, milk, 20.0);

        ShoppingCart cart = new ShoppingCart();
        cart.addItemQuantity(milk, 1);

        // Act
        Receipt receipt = teller.checksOutArticlesFrom(cart);

        // BUG: The second offer silently replaces the first.

        // Assert
        assertEquals(80.0, receipt.getTotalPrice(), 0.01);
        assertEquals(1, receipt.getDiscounts().size());
    }

    @Test
    void invalidPercentageDiscount150() {

        // Arrange
        SupermarketCatalog catalog = new FakeCatalog();
        Product milk = new Product("milk", ProductUnit.EACH);
        catalog.addProduct(milk, 100.0);

        Teller teller = new Teller(catalog);

        teller.addSpecialOffer(SpecialOfferType.TEN_PERCENT_DISCOUNT, milk, 150.0);

        ShoppingCart cart = new ShoppingCart();
        cart.addItemQuantity(milk, 1);

        // Act
        Receipt receipt = teller.checksOutArticlesFrom(cart);

        // BUG: A discount above 100% is accepted and produces a negative receipt total.

        // Assert
        assertEquals(-50.0, receipt.getTotalPrice(), 0.01);
        assertEquals(1, receipt.getDiscounts().size());
    }

    @Test
    void negativePercentageDiscount() {

        // Arrange
        SupermarketCatalog catalog = new FakeCatalog();
        Product milk = new Product("milk", ProductUnit.EACH);
        catalog.addProduct(milk, 100.0);

        Teller teller = new Teller(catalog);

        teller.addSpecialOffer(SpecialOfferType.TEN_PERCENT_DISCOUNT, milk, -20.0);

        ShoppingCart cart = new ShoppingCart();
        cart.addItemQuantity(milk, 1);

        // Act
        Receipt receipt = teller.checksOutArticlesFrom(cart);

        // BUG: A negative discount is accepted and increases the customer's total price.

        // Assert
        assertEquals(120.0, receipt.getTotalPrice(), 0.01);
        assertEquals(1, receipt.getDiscounts().size());
    }

    @Test
    void overpricedSpecialOffer() {

        // Arrange
        SupermarketCatalog catalog = new FakeCatalog();
        Product toothbrush = new Product("toothbrush", ProductUnit.EACH);
        catalog.addProduct(toothbrush, 2.0);

        Teller teller = new Teller(catalog);

        teller.addSpecialOffer(SpecialOfferType.TWO_FOR_AMOUNT, toothbrush, 5.0);

        ShoppingCart cart = new ShoppingCart();
        cart.addItemQuantity(toothbrush, 2);

        // Act
        Receipt receipt = teller.checksOutArticlesFrom(cart);

        // BUG: An overpriced offer is accepted and increases the customer's total price.

        // Assert
        assertEquals(5.0, receipt.getTotalPrice(), 0.01);
        assertEquals(1, receipt.getDiscounts().size());
    }

    @Test
    void unknownProduct() {

        // Arrange
        SupermarketCatalog catalog = new FakeCatalog();
        Product chocolate = new Product("chocolate", ProductUnit.EACH);

        Teller teller = new Teller(catalog);
        ShoppingCart cart = new ShoppingCart();

        cart.addItemQuantity(chocolate, 1);

        // Act
        try {
            teller.checksOutArticlesFrom(cart);
            fail("Expected an exception");
        }
        catch (NullPointerException e) {
            // BUG: Unknown product causes a NullPointerException.
            System.out.println("Unknown product exception confirmed");
        }
    }

    @Test
    void tamperWithDiscounts() {

        // Arrange
        SupermarketCatalog catalog = new FakeCatalog();
        Product milk = new Product("milk", ProductUnit.EACH);
        catalog.addProduct(milk, 100.0);

        Teller teller = new Teller(catalog);

        teller.addSpecialOffer(SpecialOfferType.TEN_PERCENT_DISCOUNT, milk, 10.0);

        ShoppingCart cart = new ShoppingCart();
        cart.addItemQuantity(milk, 1);

        // Act
        Receipt receipt = teller.checksOutArticlesFrom(cart);

        // Verify original total
        assertEquals(90.0, receipt.getTotalPrice(), 0.01);

        // Attempt to remove discounts externally
        try {
            receipt.getDiscounts().clear();
            fail("Discount list should not be modifiable");
        }
        catch (UnsupportedOperationException e) {
            System.out.println("Discount list is protected");
        }

        // Verify that the receipt remains unchanged
        assertEquals(90.0, receipt.getTotalPrice(), 0.01);
        assertEquals(1, receipt.getDiscounts().size());
    }
}
