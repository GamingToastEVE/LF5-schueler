package de.obj;

/**
 * Product entity with enhanced cash register functionality.
 */
public class Produkt {
    private int pid;
    private String bezeichnung;
    private double preis;
    private double mwst = 0.19; // Default 19% VAT
    private int kid; // Category ID
    private String barcode;
    private boolean isWeightBased;

    public Produkt() {}

    public Produkt(int pid, String bezeichnung, double preis, double mwst, int kid, String barcode, boolean isWeightBased) {
        this.pid = pid;
        this.bezeichnung = bezeichnung;
        this.preis = preis;
        this.mwst = mwst;
        this.kid = kid;
        this.barcode = barcode;
        this.isWeightBased = isWeightBased;
    }

    // Getters and setters
    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public String getBezeichnung() {
        return bezeichnung;
    }

    public void setBezeichnung(String bezeichnung) {
        this.bezeichnung = bezeichnung;
    }

    public double getPreis() {
        return preis;
    }

    public void setPreis(double preis) {
        this.preis = preis;
    }

    public double getMwst() {
        return mwst;
    }

    public void setMwst(double mwst) {
        this.mwst = mwst;
    }

    public int getKid() {
        return kid;
    }

    public void setKid(int kid) {
        this.kid = kid;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public boolean isWeightBased() {
        return isWeightBased;
    }

    public void setWeightBased(boolean weightBased) {
        isWeightBased = weightBased;
    }

    public double getNettoPreis() {
        return preis;
    }

    public double getBruttoPreis() {
        return preis * (1 + mwst);
    }

    @Override
    public String toString() {
        String unit = isWeightBased ? "pro kg" : "pro Stück";
        return String.format("%s - %.2f EUR %s (%.2f EUR brutto, %.1f%% MwSt)", 
                bezeichnung, preis, unit, getBruttoPreis(), mwst * 100);
    }
}

