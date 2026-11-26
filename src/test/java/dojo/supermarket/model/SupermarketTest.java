package dojo.supermarket.model;

import dojo.supermarket.ReceiptPrinter;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SupermarketTest {

    // Todo: test all kinds of discounts are applied properly

    @Test
void tenPercentDiscount() {
    SupermarketCatalog catalog = new FakeCatalog();

    Product toothbrush = new Product("toothbrush", ProductUnit.EACH);
    catalog.addProduct(toothbrush, 0.99);

    Teller teller = new Teller(catalog);
    teller.addSpecialOffer(
            SpecialOfferType.TEN_PERCENT_DISCOUNT,
            toothbrush,
            10.0);

    ShoppingCart cart = new ShoppingCart();
    cart.addItemQuantity(toothbrush, 2);

    // ACT
    Receipt receipt = teller.checksOutArticlesFrom(cart);

    // ASSERT
    assertEquals(1.782, receipt.getTotalPrice(), 0.01);

    assertEquals(1, receipt.getDiscounts().size());

    assertEquals(1, receipt.getItems().size());

    ReceiptItem receiptItem = receipt.getItems().get(0);

    assertEquals(toothbrush, receiptItem.product());
    assertEquals(0.99, receiptItem.price());
    assertEquals(1.98, receiptItem.totalPrice(), 0.01);
    assertEquals(2, receiptItem.quantity(), 0.01);
}



}
