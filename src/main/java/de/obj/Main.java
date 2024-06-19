package de.obj;

public class Main {
    public static void main(String[] args) {
        Bon b = new Bon();
        b.filiale.strasse = "Hauptstrasse";
        b.filiale.hausnummer = 32;
        b.filiale.plz = "30167";
        b.filiale.ort = "Hannover";
        b.verkäufer.name = "Max Mustermann";
        b.am.tag = 17;
        b.am.monat = 3;
        b.am.jahr = 2024;

        Produkt p1 = new Produkt();
        p1.bezeichnung="Kartoffeln";
        p1.mwst=0.07f;
        p1.preis=1.20;
        Artikel a1 = new Artikel();
        a1.produkt=p1;
        a1.menge=3;
        b.position.add(a1);

        Produkt p2 = new Produkt();
        p2.bezeichnung="Wiesenhonig";
        p2.mwst=0.19f;
        p2.preis=5.99;
        Artikel a2 = new Artikel();
        a2.produkt=p2;
        a2.menge=1;
        b.position.add(a2);

        for (Artikel a : b.position) {
            System.out.println(a);
        }

    }
}
