package no.dittnavn.footy.model; // ✅ riktig


public class MatchOdds {

    public String home;
    public String away;

    public double homeOdds = 0;
    public double drawOdds = 0;
    public double awayOdds = 0;
    public String matchDate;

    public MatchOdds(String home, String away) {
        this.home = home;
        this.away = away;
    }

}



