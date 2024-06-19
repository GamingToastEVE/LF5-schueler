package de.obj;


public class Produkt {
    public String bezeichnung;
    public float mwst;
    public double preis;

    @Override
    public String toString() {
        return bezeichnung+" a "+preis+" EUR zzgl. "+(mwst*100)+"% MwSt ";
    }
}

