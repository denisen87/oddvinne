package no.dittnavn.footy.model;


import java.time.LocalDateTime;

public class ValueBetRecord {

    public String home;
    public String away;

    public double oddsHome;
    public double oddsDraw;
    public double oddsAway;

    public String betType; // H, D, A
    public double value;
    public String matchDate;

    public LocalDateTime timestamp;

    public ValueBetRecord(String home, String away,
                          double oddsHome, double oddsDraw, double oddsAway,
                          String betType, double value) {

        this.home = home;
        this.away = away;
        this.oddsHome = oddsHome;
        this.oddsDraw = oddsDraw;
        this.oddsAway = oddsAway;
        this.betType = betType;
        this.value = value;
        this.timestamp = LocalDateTime.now();
    }
}