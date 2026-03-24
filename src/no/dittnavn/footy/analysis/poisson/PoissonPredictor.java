package no.dittnavn.footy.analysis.poisson;

import no.dittnavn.footy.stats.TeamStats;

public class PoissonPredictor {

    private static final double LEAGUE_HOME_AVG = 1.55;
    private static final double LEAGUE_AWAY_AVG = 1.20;

    public double[] predict(TeamStats home, TeamStats away){

        double homeAttack =
                home.getHomeGoalsScoredPerMatch() / LEAGUE_HOME_AVG;

        double homeDefense =
                home.getHomeGoalsConcededPerMatch() / LEAGUE_AWAY_AVG;

        double awayAttack =
                away.getAwayGoalsScoredPerMatch() / LEAGUE_AWAY_AVG;

        double awayDefense =
                away.getAwayGoalsConcededPerMatch() / LEAGUE_HOME_AVG;

        double lambdaHome =
                LEAGUE_HOME_AVG * homeAttack * awayDefense;

        double lambdaAway =
                LEAGUE_AWAY_AVG * awayAttack * homeDefense;

        double pHome = 0.0;
        double pDraw = 0.0;
        double pAway = 0.0;

        for(int h = 0; h <= 7; h++){
            for(int a = 0; a <= 7; a++){

                double prob = poisson(h, lambdaHome) *
                        poisson(a, lambdaAway);

                if(h > a) pHome += prob;
                else if(h == a) pDraw += prob;
                else pAway += prob;
            }
        }

        double sum = pHome + pDraw + pAway;

        pHome /= sum;
        pDraw /= sum;
        pAway /= sum;

        return new double[]{pHome, pDraw, pAway};
    }

    private double poisson(int goals, double lambda){
        return Math.pow(lambda, goals) * Math.exp(-lambda) / factorial(goals);
    }

    private int factorial(int n){
        if(n <= 1) return 1;
        return n * factorial(n - 1);
    }

}