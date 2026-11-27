package dojo.supermarket.model;

class FiveForAmountDiscountCalculator implements OfferCalculator {

    private static final int FIVE_ITEMS = 5;

    @Override
    public Discount calculate(Product product, Offer offer, double quantity, double unitPrice) {
        int quantityAsInt = (int) quantity;
        if (quantityAsInt < FIVE_ITEMS) {
            return null;
        }

        int numberOfOfferGroups = quantityAsInt / FIVE_ITEMS;
        double offerTotal = offer.argument() * numberOfOfferGroups
                + quantityAsInt % FIVE_ITEMS * unitPrice;
        double discountAmount = unitPrice * quantity - offerTotal;

        return new Discount(product, FIVE_ITEMS + " for " + offer.argument(), -discountAmount);
    }
}