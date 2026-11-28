package dojo.supermarket.model;

interface OfferCalculator {

    Discount calculate(Product product, Offer offer, double quantity, double unitPrice);
}