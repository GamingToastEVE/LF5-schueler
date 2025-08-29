package de.obj;

import java.text.DecimalFormat;

/**
 * Article representing an item on a receipt.
 */
public class Artikel {
    private Produkt produkt;
    private double menge;

    public Artikel() {}

    public Artikel(Produkt produkt, double menge) {
        this.produkt = produkt;
        this.menge = menge;
    }

    public Produkt getProdukt() {
        return produkt;
    }

    public void setProdukt(Produkt produkt) {
        this.produkt = produkt;
    }

    public double getMenge() {
        return menge;
    }

    public void setMenge(double menge) {
        this.menge = menge;
    }

    public double getNettoGesamtpreis() {
        return menge * produkt.getNettoPreis();
    }

    public double getBruttoGesamtpreis() {
        return menge * produkt.getBruttoPreis();
    }

    public double getMwstBetrag() {
        return getNettoGesamtpreis() * produkt.getMwst();
    }

    @Override
    public String toString() {
        DecimalFormat df = new DecimalFormat("#.##");
        String unit = produkt.isWeightBased() ? " kg" : " Stück";
        return String.format("%s%s %s = %s EUR (netto) %s EUR (brutto)",
                df.format(menge), unit, produkt.getBezeichnung(),
                df.format(getNettoGesamtpreis()),
                df.format(getBruttoGesamtpreis()));
    }
}
