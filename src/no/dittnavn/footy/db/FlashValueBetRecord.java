package no.dittnavn.footy.db;

public class FlashValueBetRecord {

    public String homeTeam;
    public String awayTeam;

    public double homeOdds;
    public double drawOdds;
    public double awayOdds;

    public String betType;
    public double value;

    public String matchDate;

    public FlashValueBetRecord(
            String homeTeam,
            String awayTeam,
            double homeOdds,
            double drawOdds,
            double awayOdds,
            String betType,
            double value,
            String matchDate
    ) {

        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;

        this.homeOdds = homeOdds;
        this.drawOdds = drawOdds;
        this.awayOdds = awayOdds;

        this.betType = betType;
        this.value = value;

        this.matchDate = matchDate;
    }
}