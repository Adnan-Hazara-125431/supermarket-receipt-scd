package dojo.supermarket.model;

class PercentDiscountCalculator implements OfferCalculator {

    private static final double PERCENT_DIVISOR = 100.0;

    @Override
    public Discount calculate(Product product, Offer offer, double quantity, double unitPrice) {
        double discountAmount = quantity * unitPrice * offer.argument() / PERCENT_DIVISOR;

        return new Discount(product, offer.argument() + "% off", -discountAmount);
    }
}