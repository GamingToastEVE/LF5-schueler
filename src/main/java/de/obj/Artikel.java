package de.obj;
import java.text.DecimalFormat;

public class Artikel {
    public Produkt produkt;
    public double menge;

    @Override
    public String toString() {
        DecimalFormat df = new DecimalFormat("#.##");        
        return menge+" kg bzw. Stück "+produkt.toString()+" = "+df.format((menge*produkt.preis))+" EUR (netto) "+df.format((menge*produkt.preis*(1+produkt.mwst)))+" EUR (brutto)";
    }
}
