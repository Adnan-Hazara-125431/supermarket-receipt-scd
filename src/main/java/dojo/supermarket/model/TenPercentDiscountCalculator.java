package dojo.supermarket.model;

class TenPercentDiscountCalculator implements OfferCalculator {

    private static final double PERCENT_DIVISOR = 100.0;

    @Override
    public Discount calculate(Product product, Offer offer, double quantity, double unitPrice) {
        return new Discount(
                product,
                offer.argument() + "% off",
                -quantity * unitPrice * offer.argument() / PERCENT_DIVISOR);
    }
}