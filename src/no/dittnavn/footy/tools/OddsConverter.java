package no.dittnavn.footy.tools;

public class OddsConverter {

    public double[] fromOdds(double home, double draw, double away) {

        double pHome = 1.0 / home;
        double pDraw = 1.0 / draw;
        double pAway = 1.0 / away;

        double sum = pHome + pDraw + pAway;

        return new double[]{
                pHome / sum,
                pDraw / sum,
                pAway / sum
        };
    }
}