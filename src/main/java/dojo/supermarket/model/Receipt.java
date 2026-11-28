package dojo.supermarket.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Receipt {

    private final List<ReceiptItem> items = new ArrayList<>();
    private final List<Discount> discounts = new ArrayList<>();

    public double getTotalPrice() {
        return items.stream().mapToDouble(ReceiptItem::totalPrice).sum() //
                + discounts.stream().mapToDouble(Discount::discountAmount).sum();
    }

    public void addProduct(ReceiptItem item) {
        items.add(item);
    }

    public List<ReceiptItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public void addDiscount(Discount discount) {
        discounts.add(discount);
    }

    public List<Discount> getDiscounts() {
        return Collections.unmodifiableList(discounts);
    }
}
