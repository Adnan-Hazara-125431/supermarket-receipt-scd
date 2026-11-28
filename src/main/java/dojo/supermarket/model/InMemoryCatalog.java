package dojo.supermarket.model;

import java.util.HashMap;
import java.util.Map;

/** Simple catalog kept in memory, so the packaged app can run without a database. */
public class InMemoryCatalog implements SupermarketCatalog {

    private final Map<String, Double> prices = new HashMap<>();

    @Override
    public void addProduct(Product product, double price) {
        prices.put(product.name(), price);
    }

    @Override
    public double getUnitPrice(Product product) {
        return prices.get(product.name());
    }
}