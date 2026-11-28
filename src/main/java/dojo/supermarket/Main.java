package dojo.supermarket;

import dojo.supermarket.model.*;

/** Entry point of the packaged app: checks out a sample basket and prints the receipt. */
public class Main {

    public static void main(String[] args) {
        SupermarketCatalog catalog = new InMemoryCatalog();
        Product toothbrush = new Product("toothbrush", ProductUnit.EACH);
        Product apples = new Product("apples", ProductUnit.KILO);
        catalog.addProduct(toothbrush, 0.99);
        catalog.addProduct(apples, 1.99);

        Teller teller = new Teller(catalog);
        teller.addSpecialOffer(SpecialOfferType.THREE_FOR_TWO, toothbrush, 0);
        teller.addSpecialOffer(SpecialOfferType.TEN_PERCENT_DISCOUNT, apples, 20);

        ShoppingCart cart = new ShoppingCart();
        cart.addItemQuantity(toothbrush, 3);
        cart.addItemQuantity(apples, 2.5);

        Receipt receipt = teller.checksOutArticlesFrom(cart);
        System.out.println(new ReceiptPrinter().printReceipt(receipt));
    }
}