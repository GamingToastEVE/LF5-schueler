package de.obj;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Receipt/Bon entity representing a sales transaction.
 */
public class Bon {
    private int bonId;
    private Ort filiale = new Ort();
    private LocalDateTime datum;
    private User verkaufer;
    private List<Artikel> positionen = new ArrayList<>();
    private boolean isCancelled = false;

    public Bon() {
        this.datum = LocalDateTime.now();
    }

    public Bon(User verkaufer) {
        this();
        this.verkaufer = verkaufer;
    }

    // Getters and setters
    public int getBonId() {
        return bonId;
    }

    public void setBonId(int bonId) {
        this.bonId = bonId;
    }

    public Ort getFiliale() {
        return filiale;
    }

    public void setFiliale(Ort filiale) {
        this.filiale = filiale;
    }

    public LocalDateTime getDatum() {
        return datum;
    }

    public void setDatum(LocalDateTime datum) {
        this.datum = datum;
    }

    public User getVerkaufer() {
        return verkaufer;
    }

    public void setVerkaufer(User verkaufer) {
        this.verkaufer = verkaufer;
    }

    public List<Artikel> getPositionen() {
        return positionen;
    }

    public void setPositionen(List<Artikel> positionen) {
        this.positionen = positionen;
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    public void setCancelled(boolean cancelled) {
        isCancelled = cancelled;
    }

    // Business methods
    public void addArtikel(Artikel artikel) {
        positionen.add(artikel);
    }

    public void removeArtikel(int index) {
        if (index >= 0 && index < positionen.size()) {
            positionen.remove(index);
        }
    }

    public double getNettoGesamtbetrag() {
        return positionen.stream()
                .mapToDouble(Artikel::getNettoGesamtpreis)
                .sum();
    }

    public double getBruttoGesamtbetrag() {
        return positionen.stream()
                .mapToDouble(Artikel::getBruttoGesamtpreis)
                .sum();
    }

    public double getGesamtMwst() {
        return positionen.stream()
                .mapToDouble(Artikel::getMwstBetrag)
                .sum();
    }

    public String getFormattedDatum() {
        return datum.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
    }

    public String generateReceiptText() {
        StringBuilder sb = new StringBuilder();
        sb.append("=====================================\n");
        sb.append("           GoodFood GmbH            \n");
        sb.append("=====================================\n");
        sb.append("Datum: ").append(getFormattedDatum()).append("\n");
        if (verkaufer != null) {
            sb.append("Verkäufer: ").append(verkaufer.getFullName()).append("\n");
        }
        sb.append("Bon-Nr: ").append(bonId).append("\n");
        sb.append("-------------------------------------\n");

        for (int i = 0; i < positionen.size(); i++) {
            Artikel artikel = positionen.get(i);
            sb.append(String.format("%d. %s\n", i + 1, artikel.toString()));
        }

        sb.append("-------------------------------------\n");
        sb.append(String.format("Netto: %.2f EUR\n", getNettoGesamtbetrag()));
        sb.append(String.format("MwSt:  %.2f EUR\n", getGesamtMwst()));
        sb.append(String.format("TOTAL: %.2f EUR\n", getBruttoGesamtbetrag()));
        sb.append("=====================================\n");

        if (isCancelled) {
            sb.append("*** STORNIERT ***\n");
        }

        return sb.toString();
    }
}

