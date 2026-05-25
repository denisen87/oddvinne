package no.dittnavn.footy.scanner;


public class MatchOdds {

    public String home;
    public String away;

    public double homeOdds;
    public double drawOdds;
    public double awayOdds;

    public String matchDate;

    public MatchOdds(
            String matchDate,
            String home,
            String away,
            double homeOdds,
            double drawOdds,
            double awayOdds
    ) {

        this.matchDate = matchDate;

        this.home = home;
        this.away = away;

        this.homeOdds = homeOdds;
        this.drawOdds = drawOdds;
        this.awayOdds = awayOdds;
    }

    public String getHome() {
        return home;
    }

    public String getAway() {
        return away;
    }

    public double getHomeOdds() {
        return homeOdds;
    }

    public double getDrawOdds() {
        return drawOdds;
    }

    public double getAwayOdds() {
        return awayOdds;
    }
}