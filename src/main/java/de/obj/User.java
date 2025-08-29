package de.obj;

/**
 * User entity representing employees with authentication and role information.
 */
public class User {
    private int vid;
    private String vorname;
    private String nachname;
    private String pin;
    private Role rolle = Role.VERKAUFER;

    public enum Role {
        VERKAUFER, FILIALLEITER
    }

    /**
     * Default constructor.
     */
    public User() {}

    /**
     * Constructor with all fields.
     */
    public User(int vid, String vorname, String nachname, String pin, Role rolle) {
        this.vid = vid;
        this.vorname = vorname;
        this.nachname = nachname;
        this.pin = pin;
        this.rolle = rolle;
    }

    // Getters and setters
    public int getVid() {
        return vid;
    }

    public void setVid(int vid) {
        this.vid = vid;
    }

    public String getVorname() {
        return vorname;
    }

    public void setVorname(String vorname) {
        this.vorname = vorname;
    }

    public String getNachname() {
        return nachname;
    }

    public void setNachname(String nachname) {
        this.nachname = nachname;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public Role getRolle() {
        return rolle;
    }

    public void setRolle(Role rolle) {
        this.rolle = rolle;
    }

    public String getFullName() {
        return vorname + " " + nachname;
    }

    public boolean isFilialleiter() {
        return rolle == Role.FILIALLEITER;
    }

    @Override
    public String toString() {
        return "User{" +
                "vid=" + vid +
                ", vorname='" + vorname + '\'' +
                ", nachname='" + nachname + '\'' +
                ", rolle=" + rolle +
                '}';
    }
}