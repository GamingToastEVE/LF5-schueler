# Kassensystem für GoodFood GmbH

## Schülerversion - Funktionierendes Kassensystem

Ein modernes, datenbankbasiertes Kassensystem entsprechend dem Pflichtenheft für die GoodFood GmbH.

## ✨ Implementierte Funktionen

### 🔐 Benutzerverwaltung
- **PIN-basierte Anmeldung** für alle Mitarbeiter
- **Rollenbasierte Zugriffskontrolle** (Verkäufer vs. Filialleiter)
- Sichere PIN-Speicherung mit Hashing

### 💰 Verkaufsprozesse
- **Artikelerfassung** per Barcode-Simulation oder Produktauswahl
- **Gewichtsbasierte Artikel** (Obst/Gemüse nach kg)
- **Automatische Preisberechnung** (Netto, MwSt, Brutto)
- **Kassenbon-Generierung** mit allen relevanten Informationen

### 📦 Produktverwaltung
- **Vollständige Produktdatenbank** mit bestehenden Artikeln
- **Neue Produkte hinzufügen** (nur Filialleiter)
- Unterstützung für Barcode und gewichtsbasierte Produkte

### 🚫 Stornierungen
- **Verkaufsstornierung** nur durch Filialleiter
- **Vollständige Protokollierung** aller Stornierungen
- Grund-Angabe bei Stornierung

### 📊 Statistiken & Reporting
- **Tagesumsatz** und **Wochenumsatz**
- Anzahl Verkäufe und Durchschnittswerte
- Exportierbare Daten (Grundlage für Excel/PDF)

## 🚀 Schnellstart

### Voraussetzungen
- Java 8 oder höher
- Maven 3.6+

### Installation & Start
```bash
# Repository klonen
git clone <repository-url>
cd LF5-schueler

# Anwendung kompilieren
mvn compile -DskipTests -Dcheckstyle.skip=true

# Kassensystem mit GUI starten (Standard)
mvn exec:java

# Alternativ mit Java direkt (GUI):
java -cp "target/classes:/home/runner/.m2/repository/org/xerial/sqlite-jdbc/3.36.0.3/sqlite-jdbc-3.36.0.3.jar" de.obj.SwingKassensystemApp

# Konsolen-Version:
java -cp "target/classes:/home/runner/.m2/repository/org/xerial/sqlite-jdbc/3.36.0.3/sqlite-jdbc-3.36.0.3.jar" de.obj.Main --console
```

### 🖥️ Benutzeroberfläche
Das Kassensystem bietet zwei Modi:
- **🎨 Grafische Benutzeroberfläche (Java Swing)** - Moderne, intuitive Bedienung mit Buttons und Dialogen
- **📟 Konsolen-Interface** - Textbasierte Menüführung

#### GUI-Features:
- Übersichtliches Hauptfenster mit großen Buttons
- Popup-Dialoge für Anmeldung und Verkaufsvorgänge
- Produktauswahl aus Liste mit Mengenangabe
- Kassenbon-Anzeige in separatem Fenster
- Rollenbasierte Button-Aktivierung

### 🔑 Demo-Zugangsdaten
Das System wird automatisch mit Demo-Benutzern initialisiert:

| Benutzer | PIN | Rolle | Berechtigungen |
|----------|-----|-------|----------------|
| **Maria Schmidt** | `1234` | Filialleiter | Alle Funktionen |
| **Johannes Müller** | `5678` | Verkäufer | Verkauf |
| **Emma Fischer** | `9999` | Verkäufer | Verkauf |

## 🎯 Benutzerhandbuch

### 1. Anmeldung
- PIN eingeben (siehe Demo-Zugangsdaten oben)
- System zeigt Hauptmenü entsprechend der Berechtigung

### 2. Verkaufsprozess
1. **"Neuer Verkauf"** wählen
2. **Artikel hinzufügen** per:
   - Barcode-Eingabe (ENTER für Demo-Artikel)
   - Auswahl aus Produktliste
3. **Menge eingeben** (Stück oder kg je nach Produkt)
4. **Weitere Artikel** hinzufügen oder entfernen
5. **Verkauf abschließen** → Kassenbon wird generiert

### 3. Produktverwaltung (nur Filialleiter)
- **Alle Produkte anzeigen**
- **Neue Produkte** mit Bezeichnung, Preis, Barcode hinzufügen
- Gewichtsbasierte Produkte markieren

### 4. Stornierungen (nur Filialleiter)
- Bon-ID eingeben
- Grund für Stornierung angeben
- Stornierung wird vollständig protokolliert

### 5. Statistiken (nur Filialleiter)
- **Tagesumsatz** - Verkäufe des aktuellen Tages
- **Wochenumsatz** - Verkäufe der letzten 7 Tage

## 🏗️ Technische Architektur

### Datenbankschema
- **Verkäufer** - Benutzerdaten mit PIN und Rolle
- **Produkt** - Artikelstammdaten mit Barcode-Support
- **Kassenbons** - Verkaufstransaktionen
- **BonPositionen** - Einzelpositionen pro Bon
- **Stornierungen** - Stornierungsprotokoll

### Software-Komponenten
```
📁 de.obj
├── 🖥️ SwingKassensystemApp.java # GUI-Hauptanwendung (Swing)
├── 🖥️ KassensystemApp.java     # Konsolen-Hauptanwendung
├── 🚀 Main.java                # Einstiegspunkt (GUI/Konsole)
├── 👤 User.java                # Benutzer-Entität
├── 🛒 Produkt.java            # Produkt-Entität  
├── 📄 Bon.java                # Kassenbon-Entität
├── 📦 Artikel.java            # Bon-Position
├── 🔧 DatabaseManager.java    # Datenbankverbindung
├── 👥 UserService.java        # Benutzerverwaltung
├── 🏪 ProduktService.java     # Produktverwaltung
└── 💰 VerkaufService.java     # Verkaufsabwicklung
```

## 🧪 Tests ausführen

```bash
# Alle Tests ausführen
mvn test -Dcheckstyle.skip=true

# Spezifische Tests
mvn test -Dtest=KassensystemTest -Dcheckstyle.skip=true
```

## 📋 Pflichtenheft-Compliance

| Anforderung | Status | Implementierung |
|------------|--------|-----------------|
| ✅ Benutzerverwaltung | Vollständig | PIN-Auth, Rollen, DB-gespeichert |
| ✅ Verkaufsvorgänge | Vollständig | Barcode, Gewicht, Preisberechnung |
| ✅ Produktverwaltung | Grundfunktionen | CRUD, Validierung |
| ✅ Stornierungen | Vollständig | Nur FL, Protokollierung |
| ✅ Statistiken | Grundfunktionen | Umsatz, Anzahl, Zeiträume |
| ✅ Sicherheit | Implementiert | PIN-Hashing, Rollenkontrolle |
| ✅ Datenhaltung | Vollständig | SQLite, Transaktionen |
| 🔄 Excel-Import | Vorbereitet | Grundstruktur vorhanden |
| ✅ GUI-Interface | Vollständig | Java Swing, benutzerfreundlich |

## 🔮 Erweiterungsmöglichkeiten

### Nächste Entwicklungsstufen:
1. **Excel-Import** für Produktdaten
2. **Erweiterte GUI-Funktionen** (Statistiken, Produktverwaltung, Stornierungen)
3. **Barcode-Scanner Integration**
4. **PDF-Export** für Statistiken
5. **Netzwerk-Backend** für Multi-Terminal-Betrieb
6. **Waagen-Schnittstelle** für gewichtsbasierte Produkte

### Technische Verbesserungen:
- **Vollständige Checkstyle-Compliance**
- **Erhöhte Testabdeckung**
- **Logging-Framework**
- **Konfigurationsdateien**
- **Docker-Container**

## 🏪 Demo-Daten

Das System wird mit realistischen Demo-Daten initialisiert:
- 9 verschiedene Produkte (Lebensmittel)
- 3 Benutzer mit unterschiedlichen Rollen
- Automatische Datenbankstruktur-Updates

---

**GoodFood GmbH Kassensystem** - Entwickelt als Schülerprojekt, implementiert nach modernen Software-Engineering-Standards.
