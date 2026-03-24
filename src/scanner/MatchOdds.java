package no.dittnavn.footy.scanner;

public class MatchOdds {

    private String home;
    private String away;

    private double homeOdds;
    private double drawOdds;
    private double awayOdds;

    public MatchOdds(String home, String away,
                     double homeOdds, double drawOdds, double awayOdds) {

        this.home = home;
        this.away = away;
        this.homeOdds = homeOdds;
        this.drawOdds = drawOdds;
        this.awayOdds = awayOdds;
    }

    public String getHome() { return home; }
    public String getAway() { return away; }

    public double getHomeOdds() { return homeOdds; }
    public double getDrawOdds() { return drawOdds; }
    public double getAwayOdds() { return awayOdds; }
}
