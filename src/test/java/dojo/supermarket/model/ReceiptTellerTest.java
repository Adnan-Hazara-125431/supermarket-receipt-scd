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

        // Register first offer
        teller.addSpecialOffer(SpecialOfferType.TEN_PERCENT_DISCOUNT, milk, 10.0);
        // Attempt to register second offer
        try {
            teller.addSpecialOffer(SpecialOfferType.TEN_PERCENT_DISCOUNT, milk, 20.0);
            fail("Duplicate offer should be rejected");
        }
        catch (IllegalArgumentException e) {assertEquals("An offer already exists for milk", e.getMessage());
        }

        ShoppingCart cart = new ShoppingCart();
        cart.addItemQuantity(milk, 1);

        // Act
        Receipt receipt = teller.checksOutArticlesFrom(cart);

        // Verify that the original offer remains unchanged
        assertEquals(90.0, receipt.getTotalPrice(), 0.01);
        assertEquals(1, receipt.getDiscounts().size());
    }

    @Test
    void invalidPercentageDiscount150() {
        // Arrange
        SupermarketCatalog catalog = new FakeCatalog();
        Product milk = new Product("milk", ProductUnit.EACH);
        catalog.addProduct(milk, 100.0);

        Teller teller = new Teller(catalog);

        // Act and Assert
        try {
            teller.addSpecialOffer(
                    SpecialOfferType.TEN_PERCENT_DISCOUNT, milk, 150.0);
            fail("Discount above 100 should be rejected");
        }
        catch (IllegalArgumentException e) {
            assertEquals("Discount percentage must be between 0 and 100",  e.getMessage());
        }
    }

    @Test
    void negativePercentageDiscount() {
        // Arrange
        SupermarketCatalog catalog = new FakeCatalog();
        Product milk = new Product("milk", ProductUnit.EACH);
        catalog.addProduct(milk, 100.0);

        Teller teller = new Teller(catalog);

        // Act and Assert
        try {
            teller.addSpecialOffer(SpecialOfferType.TEN_PERCENT_DISCOUNT, milk, -20.0);
            fail("Negative discount should be rejected");
        }
        catch (IllegalArgumentException e) {
            assertEquals("Discount percentage must be between 0 and 100",  e.getMessage());
        }
    }

    @Test
    void overpricedSpecialOffer() {
        // Arrange
        SupermarketCatalog catalog = new FakeCatalog();
        Product toothbrush = new Product("toothbrush", ProductUnit.EACH);
        catalog.addProduct(toothbrush, 2.0);

        Teller teller = new Teller(catalog);

        // Act and Assert
        try {
            teller.addSpecialOffer(SpecialOfferType.TWO_FOR_AMOUNT, toothbrush, 5.0);
            fail("Overpriced offer should be rejected");
        }
        catch (IllegalArgumentException e) {
            assertEquals("Invalid promotional price for two products",  e.getMessage());
        }
    }

    @Test
    void validTwoForAmountOffer() {
        // Arrange
        SupermarketCatalog catalog = new FakeCatalog();
        Product toothbrush = new Product("toothbrush", ProductUnit.EACH);
        catalog.addProduct(toothbrush, 2.0);

        Teller teller = new Teller(catalog);
        teller.addSpecialOffer(
                SpecialOfferType.TWO_FOR_AMOUNT, toothbrush, 3.0);

        ShoppingCart cart = new ShoppingCart();
        cart.addItemQuantity(toothbrush, 2);

        // Act
        Receipt receipt = teller.checksOutArticlesFrom(cart);

        // Assert
        assertEquals(3.0, receipt.getTotalPrice(), 0.01);
        assertEquals(1, receipt.getDiscounts().size());
    }

    @Test
    void overpricedFiveForAmountOffer() {
        // Arrange
        SupermarketCatalog catalog = new FakeCatalog();
        Product toothbrush = new Product("toothbrush", ProductUnit.EACH);
        catalog.addProduct(toothbrush, 2.0);

        Teller teller = new Teller(catalog);

        // Act and Assert
        try {
            teller.addSpecialOffer(SpecialOfferType.FIVE_FOR_AMOUNT, toothbrush, 12.0);
            fail("Overpriced offer should be rejected");
        }
        catch (IllegalArgumentException e) {
            assertEquals("Invalid promotional price for five products", e.getMessage());
        }
    }

    @Test
    void validFiveForAmountOffer() {
        // Arrange
        SupermarketCatalog catalog = new FakeCatalog();
        Product toothbrush = new Product("toothbrush", ProductUnit.EACH);
        catalog.addProduct(toothbrush, 2.0);

        Teller teller = new Teller(catalog);

        teller.addSpecialOffer(SpecialOfferType.FIVE_FOR_AMOUNT, toothbrush, 8.0);

        ShoppingCart cart = new ShoppingCart();
        cart.addItemQuantity(toothbrush, 5);

        // Act
        Receipt receipt = teller.checksOutArticlesFrom(cart);

        // Assert
        assertEquals(8.0, receipt.getTotalPrice(), 0.01);
        assertEquals(1, receipt.getDiscounts().size());
    }

    @Test
    void promotionalPriceEqualToNormalPrice() {
        // Arrange
        SupermarketCatalog catalog = new FakeCatalog();

        Product toothbrush = new Product("toothbrush", ProductUnit.EACH);
        Product soap = new Product("soap", ProductUnit.EACH);

        catalog.addProduct(toothbrush, 2.0);
        catalog.addProduct(soap, 2.0);

        Teller teller = new Teller(catalog);

        teller.addSpecialOffer(SpecialOfferType.TWO_FOR_AMOUNT, toothbrush, 4.0);
        teller.addSpecialOffer(SpecialOfferType.FIVE_FOR_AMOUNT, soap, 10.0);

        ShoppingCart cart = new ShoppingCart();
        cart.addItemQuantity(toothbrush, 2);
        cart.addItemQuantity(soap, 5);
        // Act
        Receipt receipt = teller.checksOutArticlesFrom(cart);
        // Assert
        assertEquals(14.0, receipt.getTotalPrice(), 0.01);
    }

    @Test
    void negativePromotionalPrice() {

        // Arrange
        SupermarketCatalog catalog = new FakeCatalog();
        Product toothbrush = new Product("toothbrush", ProductUnit.EACH);
        catalog.addProduct(toothbrush, 2.0);

        Teller teller = new Teller(catalog);

        // Test two-product offer
        try {
            teller.addSpecialOffer(SpecialOfferType.TWO_FOR_AMOUNT, toothbrush, -1.0);
            fail("Negative promotional price should be rejected");
        }
        catch (IllegalArgumentException e) {
            assertEquals("Invalid promotional price for two products", e.getMessage());
        }

        // Test five-product offer
        try {
            teller.addSpecialOffer(SpecialOfferType.FIVE_FOR_AMOUNT, toothbrush, -1.0);
            fail("Negative promotional price should be rejected");
        }
        catch (IllegalArgumentException e) {
            assertEquals("Invalid promotional price for five products", e.getMessage());
        }
    }

    @Test
    void invalidNaNPromotionalPrice() {

        // Arrange
        SupermarketCatalog catalog = new FakeCatalog();
        Product toothbrush = new Product("toothbrush", ProductUnit.EACH);
        catalog.addProduct(toothbrush, 2.0);

        Teller teller = new Teller(catalog);

        // Test two-product offer
        try {
            teller.addSpecialOffer(SpecialOfferType.TWO_FOR_AMOUNT, toothbrush, Double.NaN);
            fail("NaN promotional price should be rejected");
        }
        catch (IllegalArgumentException e) {
            assertEquals("Invalid promotional price for two products", e.getMessage());
        }

        // Test five-product offer
        try {
            teller.addSpecialOffer(SpecialOfferType.FIVE_FOR_AMOUNT, toothbrush, Double.NaN);
            fail("NaN promotional price should be rejected");
        }
        catch (IllegalArgumentException e) {
            assertEquals("Invalid promotional price for five products", e.getMessage());
        }
    }

    @Test
    void unknownProduct() {
        // Arrange
        SupermarketCatalog catalog = new FakeCatalog();
        Product chocolate = new Product("chocolate", ProductUnit.EACH);

        Teller teller = new Teller(catalog);
        ShoppingCart cart = new ShoppingCart();

        cart.addItemQuantity(chocolate, 1);

        // Act and Assert
        try {
            teller.checksOutArticlesFrom(cart);
            fail("Unknown product should be rejected");
        }
        catch (IllegalArgumentException e) {
            assertEquals(
                    "Product chocolate was not found in the catalog",
                    e.getMessage());
        }
    }

    @Test
    void unknownProductPromotionalOffer() {
        // Arrange
        SupermarketCatalog catalog = new FakeCatalog();
        Product chocolate = new Product("chocolate", ProductUnit.EACH);

        Teller teller = new Teller(catalog);

        // Act and Assert
        try {
            teller.addSpecialOffer(SpecialOfferType.TWO_FOR_AMOUNT, chocolate, 5.0);
            fail("Unknown product should be rejected");
        }
        catch (IllegalArgumentException e) {
            assertEquals("Product chocolate was not found in the catalog", e.getMessage());
        }
    }

    @Test
    void zeroPercentageDiscount() {
        // Arrange
        SupermarketCatalog catalog = new FakeCatalog();
        Product milk = new Product("milk", ProductUnit.EACH);
        catalog.addProduct(milk, 100.0);

        Teller teller = new Teller(catalog);

        teller.addSpecialOffer(
                SpecialOfferType.TEN_PERCENT_DISCOUNT, milk, 0.0);

        ShoppingCart cart = new ShoppingCart();
        cart.addItemQuantity(milk, 1);

        // Act
        Receipt receipt = teller.checksOutArticlesFrom(cart);

        // Assert
        assertEquals(100.0, receipt.getTotalPrice(), 0.01);
    }

    @Test
    void hundredPercentageDiscount() {
        // Arrange
        SupermarketCatalog catalog = new FakeCatalog();
        Product milk = new Product("milk", ProductUnit.EACH);
        catalog.addProduct(milk, 100.0);

        Teller teller = new Teller(catalog);

        teller.addSpecialOffer(SpecialOfferType.TEN_PERCENT_DISCOUNT, milk, 100.0);

        ShoppingCart cart = new ShoppingCart();
        cart.addItemQuantity(milk, 1);

        // Act
        Receipt receipt = teller.checksOutArticlesFrom(cart);

        // Assert
        assertEquals(0.0, receipt.getTotalPrice(), 0.01);
    }

    @Test
    void invalidBoundaryDiscounts() {
        // Arrange
        SupermarketCatalog catalog = new FakeCatalog();
        Product milk = new Product("milk", ProductUnit.EACH);
        catalog.addProduct(milk, 100.0);

        Teller teller = new Teller(catalog);

        double[] values = {-0.01, 100.01};

        // Test both invalid values
        for (double value : values) {
            try {
                teller.addSpecialOffer(SpecialOfferType.TEN_PERCENT_DISCOUNT, milk, value);
                fail("Invalid discount should be rejected");
            }
            catch (IllegalArgumentException e) {
                assertEquals("Discount percentage must be between 0 and 100", e.getMessage());
            }
        }
    }

    @Test
    void invalidNaNDiscount() {
        // Arrange
        SupermarketCatalog catalog = new FakeCatalog();
        Product milk = new Product("milk", ProductUnit.EACH);
        catalog.addProduct(milk, 100.0);

        Teller teller = new Teller(catalog);

        // Act and Assert
        try {
            teller.addSpecialOffer(SpecialOfferType.TEN_PERCENT_DISCOUNT,  milk, Double.NaN);
            fail("NaN discount should be rejected");
        }
        catch (IllegalArgumentException e) {
            assertEquals("Discount percentage must be between 0 and 100",  e.getMessage());
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

    @Test
    void unknownProductPercentageOffer() {
        // Arrange
        SupermarketCatalog catalog = new FakeCatalog();
        Product chocolate = new Product("chocolate", ProductUnit.EACH);

        Teller teller = new Teller(catalog);

        // Act and Assert
        try {
            teller.addSpecialOffer(SpecialOfferType.TEN_PERCENT_DISCOUNT, chocolate, 10.0);
            fail("Unknown product should be rejected");
        }
        catch (IllegalArgumentException e) {
            assertEquals("Product chocolate was not found in the catalog", e.getMessage());
        }
    }

}
