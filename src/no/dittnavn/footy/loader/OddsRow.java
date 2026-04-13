package no.dittnavn.footy.loader;


public class OddsRow {

    public String home;
    public String away;
    public String label;
    public double price;

    public OddsRow(String home, String away, String label, double price) {
        this.home = home;
        this.away = away;
        this.label = label;
        this.price = price;
    }
}