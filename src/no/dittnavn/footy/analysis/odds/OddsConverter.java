package no.dittnavn.footy.analysis.odds;

public class OddsConverter {

    // enkel implisert sannsynlighet
    public static double impliedProbability(double odds){
        return 1.0 / odds;
    }

    // konverterer 1X2 odds til normaliserte sannsynligheter
    public static double[] fromOdds(double homeOdds, double drawOdds, double awayOdds){

        double bHome = 1.0 / homeOdds;
        double bDraw = 1.0 / drawOdds;
        double bAway = 1.0 / awayOdds;

        double sumOdds = bHome + bDraw + bAway;

        bHome /= sumOdds;
        bDraw /= sumOdds;
        bAway /= sumOdds;

        return new double[]{
                bHome,
                bDraw,
                bAway
        };
    }
}