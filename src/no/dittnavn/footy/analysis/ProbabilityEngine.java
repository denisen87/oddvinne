package no.dittnavn.footy.analysis;

import no.dittnavn.footy.stats.TeamStats;

public class ProbabilityEngine {

    private static final double HOME_ADVANTAGE = 100;
    private static final double DRAW_BASE = 0.25;

    public static double[] matchProbabilities(TeamStats home, TeamStats away){

        double homeElo = home.getElo() + HOME_ADVANTAGE;
        double awayElo = away.getElo();

        double eloDiff = homeElo - awayElo;

        double winProb = 1.0 / (1.0 + Math.pow(10, -eloDiff / 400));

        double drawProb = DRAW_BASE * Math.exp(-Math.abs(eloDiff)/400);

        double homeProb = winProb * (1 - drawProb);
        double awayProb = (1 - winProb) * (1 - drawProb);

        return new double[]{
                homeProb,
                drawProb,
                awayProb
        };
    }

}