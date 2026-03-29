package no.dittnavn.footy.engine;

import java.util.List;

import no.dittnavn.footy.analysis.poisson.PoissonPredictor;
import no.dittnavn.footy.model.BacktestResult;
import no.dittnavn.footy.model.Match;
import no.dittnavn.footy.stats.StatsIndeks;
import no.dittnavn.footy.stats.TeamStats;

public class BacktestRunner {

    private static double[] marketProbs(Match m) {
        double h = m.getHomeOdds();
        double d = m.getDrawOdds();
        double a = m.getAwayOdds();

        double ih = 1.0 / h;
        double id = 1.0 / d;
        double ia = 1.0 / a;

        double sum = ih + id + ia;

        return new double[]{
                ih / sum,
                id / sum,
                ia / sum
        };
    }

    public static BacktestResult run(
            List<Match> matches,
            double maxProb,
            double prob,
            double edge,
            double homeBias,
            double confidence
    ){

        StatsIndeks stats = new StatsIndeks();
        PoissonPredictor predictor = new PoissonPredictor();

        double totalProfit = 0.0;
        int bets = 0;

        double wModel = 0.7;
        double wMarket = 0.3;

        for (Match m : matches) {

            TeamStats homeStats = stats.getTeam(m.getHomeTeam());
            TeamStats awayStats = stats.getTeam(m.getAwayTeam());

            if (homeStats == null || awayStats == null) {
                stats.update(m);
                continue;
            }

            if (homeStats.getGames() < 5 || awayStats.getGames() < 5) {
                stats.update(m);
                continue;
            }

            if (m.getHomeOdds() <= 1.01 ||
                    m.getDrawOdds() <= 1.01 ||
                    m.getAwayOdds() <= 1.01) {
                stats.update(m);
                continue;
            }

            // 🔮 predict
            double[] poisson = predictor.predict(homeStats, awayStats);
            double[] market = marketProbs(m);

            double pHome = poisson[0] * wModel + market[0] * wMarket;
            double pDraw = poisson[1] * wModel + market[1] * wMarket;
            double pAway = poisson[2] * wModel + market[2] * wMarket;

            // normalize

            pHome *= homeBias ;
            double sum = pHome + pDraw + pAway;
            pHome /= sum;
            pDraw /= sum;
            pAway /= sum;

            double maxProbValue = Math.max(pHome, Math.max(pDraw, pAway));

            // 🔥 FILTERS (fra optimizer)
            if (maxProbValue < maxProb) {
                stats.update(m);
                continue;
            }

            if (Math.abs(pHome - pAway) < 0.18) {
                stats.update(m);
                continue;
            }

            // 🎯 EDGE
            double bestEdge = 0.0;
            String bet = null;

            if (m.getHomeOdds() > 0) {
                edge = (pHome * 0.95) - (1.0 / m.getHomeOdds());
                if (edge > bestEdge) {
                    bestEdge = edge;
                    bet = "HOME";
                }
            }

            if (m.getAwayOdds() > 0) {
                edge = (pAway * 0.95) - (1.0 / m.getAwayOdds());
                if (edge > bestEdge) {
                    bestEdge = edge;
                    bet = "AWAY";
                }
            }

            if (bet == null || bestEdge < edge) {
                stats.update(m);
                continue;
            }

            double probValue =
                    bet.equals("HOME") ? pHome : pAway;

            if (probValue < prob) {
                stats.update(m);
                continue;
            }

            double odds =
                    bet.equals("HOME") ? m.getHomeOdds() : m.getAwayOdds();

            if (odds > 3.5) {
                stats.update(m);
                continue;
            }

            double confidenceScore = Math.abs(pHome - pAway);

            if (confidenceScore < confidence) {
                stats.update(m);
                continue;
            }

            // 🧾 resultat
            String actual =
                    m.getHomeGoals() > m.getAwayGoals() ? "HOME" :
                            m.getHomeGoals() < m.getAwayGoals() ? "AWAY" : "DRAW";

            boolean win =
                    (bet.equals("HOME") && actual.equals("HOME")) ||
                            (bet.equals("AWAY") && actual.equals("AWAY"));

            // 💰 Kelly
            double implied = 1.0 / odds;
            double kelly = (probValue - implied) / (odds - 1.0);
            double stake = Math.max(0, kelly * 0.5);

            if (stake > 0) {
                bets++;

                if (win) {
                    totalProfit += stake * (odds - 1.0);
                } else {
                    totalProfit -= stake;
                }
            }

            stats.update(m);
        }

        double roi = 0;
        if (bets > 0) {
            roi = (totalProfit / bets) * 100.0;
        }


        return new BacktestResult(roi, bets);
    }


}