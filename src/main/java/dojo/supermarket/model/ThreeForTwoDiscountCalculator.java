package dojo.supermarket.model;

class ThreeForTwoDiscountCalculator implements OfferCalculator {

    private static final int TWO_ITEMS = 2;
    private static final int THREE_ITEMS = 3;

    @Override
    public Discount calculate(Product product, Offer offer, double quantity, double unitPrice) {
        int quantityAsInt = (int) quantity;
        if (quantityAsInt < THREE_ITEMS) {
            return null;
        }

        int numberOfOfferGroups = quantityAsInt / THREE_ITEMS;
        double discountAmount =
                quantity * unitPrice
                        - ((numberOfOfferGroups * TWO_ITEMS * unitPrice)
                        + quantityAsInt % THREE_ITEMS * unitPrice);

        return new Discount(product, "3 for 2", -discountAmount);
    }
}