package dojo.supermarket.model;

class TwoForAmountDiscountCalculator implements OfferCalculator {

    private static final int TWO_ITEMS = 2;

    @Override
    public Discount calculate(Product product, Offer offer, double quantity, double unitPrice) {
        int quantityAsInt = (int) quantity;
        if (quantityAsInt < TWO_ITEMS) {
            return null;
        }

        double offerTotal = offer.argument() * (quantityAsInt / TWO_ITEMS)
                + quantityAsInt % TWO_ITEMS * unitPrice;
        double discountAmount = unitPrice * quantity - offerTotal;

        return new Discount(product, TWO_ITEMS + " for " + offer.argument(), -discountAmount);
    }
}