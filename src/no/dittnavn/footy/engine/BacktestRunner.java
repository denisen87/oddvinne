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
    ) {

        StatsIndeks stats = new StatsIndeks();
        PoissonPredictor predictor = new PoissonPredictor();

        double totalProfit = 0.0;
        int bets = 0;

        double wModel = 0.7;
        double wMarket = 0.3;

        for (Match m : matches) {

            System.out.println(
                    m.getHomeTeam() + " vs " + m.getAwayTeam() +
                            " | HS=" + m.getHomeShots() +
                            " AS=" + m.getAwayShots() +
                            " | HST=" + m.getHomeShotsTarget() +
                            " AST=" + m.getAwayShotsTarget()
            );

            TeamStats homeStats = stats.getTeam(m.getHomeTeam());
            TeamStats awayStats = stats.getTeam(m.getAwayTeam());


            if (homeStats == null || awayStats == null) {
                stats.update(m);
                continue;
            }

            boolean hasData =
                    homeStats.getShotsOnTargetPerMatch() > 0 &&
                            awayStats.getShotsOnTargetPerMatch() > 0;

            if (homeStats.getGames() < 5 || awayStats.getGames() < 5) {
                stats.update(m);
                continue;
            }

            // 🔥 SOT FILTER (HER!)
            if (hasData &&
                    (
                            (homeStats.getShotsOnTargetPerMatch() < 2 &&
                                    awayStats.getShotsOnTargetPerMatch() < 2)

                                    ||

                                    (homeStats.getLastMatchSOT() < 2 &&
                                            awayStats.getLastMatchSOT() < 2)
                    )
            ) {
                stats.update(m);
                continue;
            }

            if (m.getHomeOdds() <= 1.01 ||
                    m.getDrawOdds() <= 1.01 ||
                    m.getAwayOdds() <= 1.01) {
                stats.update(m);
                continue;
            }

            // 🔮 prediction
            double[] poisson = predictor.predict(homeStats, awayStats);
            double[] market = marketProbs(m);

            double pHome = poisson[0] * wModel + market[0] * wMarket;
            double pDraw = poisson[1] * wModel + market[1] * wMarket;
            double pAway = poisson[2] * wModel + market[2] * wMarket;

            // normalize
            pHome *= homeBias;
            double sum = pHome + pDraw + pAway;
            pHome /= sum;
            pDraw /= sum;
            pAway /= sum;

            double maxProbValue = Math.max(pHome, Math.max(pDraw, pAway));

            // 🎯 EDGE + BET SELECTION
            double bestEdge = 0.0;
            String bet = null;


            if (m.getHomeOdds() > 0) {
                double edgeHome = (pHome * 0.95) - (1.0 / m.getHomeOdds());
                if (edgeHome > bestEdge) {
                    bestEdge = edgeHome;
                    bet = "HOME";
                }
            }

            if (m.getAwayOdds() > 0) {
                double edgeAway = (pAway * 0.95) - (1.0 / m.getAwayOdds());
                if (edgeAway > bestEdge) {
                    bestEdge = edgeAway;
                    bet = "AWAY";
                }
            }

            System.out.println("EDGE DEBUG: " + bestEdge);
            System.out.println("MAXPROB DEBUG: " + maxProbValue);

// 🔥 DATA-AWARE EDGE FILTER
            if (hasData) {

                if (bestEdge < 0.02) {
                    stats.update(m);
                    continue;
                }

            } else {

                if (bestEdge < 0.035) {
                    stats.update(m);
                    continue;
                }
            }


            // 🔥 MARKET FILTERS (KORREKT PLASSERING)

            double lineMoveHome = m.getPSCH() - m.getAvgCH();
            double lineMoveAway = m.getPSCA() - m.getAvgCA();

// ❌ odds går feil vei (sharp money imot deg)
            if (bet != null && bet.equals("HOME") && lineMoveHome < -0.02) {
                stats.update(m);
                continue;
            }

            if (bet != null && bet.equals("AWAY") && lineMoveAway < -0.02) {
                stats.update(m);
                continue;
            }

// 🔥 NYTT: spread (market disagreement)
            double spreadHome = m.getMaxCH() - m.getAvgCH();
            double spreadAway = m.getMaxCA() - m.getAvgCA();

// ❌ marked uenig → skip
            if (bet != null && bet.equals("HOME") && spreadHome > 0.3) {
                stats.update(m);
                continue;
            }

            if (bet != null && bet.equals("AWAY") && spreadAway > 0.3) {
                stats.update(m);
                continue;
            }


            System.out.println("EDGE DEBUG: " + bestEdge);
            System.out.println("MAXPROB DEBUG: " + maxProbValue);


            // 🔥 FILTERS

            if (maxProbValue < maxProb) {
                stats.update(m);
                continue;
            }

            if (Math.abs(pHome - pAway) < 0.05) {
                stats.update(m);
                continue;
            }

            if (bet == null) {
                stats.update(m);
                continue;
            }


            double probValue =
                    bet.equals("HOME") ? pHome : pAway;

// 🔥 KALIBRERING
            double adjustedProb = 0.5 + (probValue - 0.5) * 0.85;

// 🔥 FILTER (fortsatt på raw prob!)
            if (probValue < 0.58) {
                stats.update(m);
                continue;
            }

            // (midlertidig fjernet streng prob-filter)

            double odds =
                    bet.equals("HOME") ? m.getHomeOdds() : m.getAwayOdds();

            // 🔥 ODDS FILTER (HER!)
            if (hasData) {
                if (odds < 1.5 || odds > 3.2) continue;
            } else {
                if (odds < 1.7 || odds > 2.8) continue;
            }

            if (odds > 3.5) {
                stats.update(m);
                continue;
            }

            double confidenceScore = Math.abs(pHome - pAway);

            if (confidenceScore < confidence) {
                stats.update(m);
                continue;
            }

            // 🧾 RESULT
            String actual =
                    m.getHomeGoals() > m.getAwayGoals() ? "HOME" :
                            m.getHomeGoals() < m.getAwayGoals() ? "AWAY" : "DRAW";

            boolean win =
                    (bet.equals("HOME") && actual.equals("HOME")) ||
                            (bet.equals("AWAY") && actual.equals("AWAY"));

            // 💰 KELLY
            double implied = 1.0 / odds;
            double kelly = (adjustedProb - implied) / (odds - 1.0);
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

        double roi = bets > 0 ? (totalProfit / bets) * 100.0 : 0;

        System.out.println("Bets: " + bets);
        System.out.println("Total profit: " + totalProfit);
        System.out.println("ROI: " + roi);

        return new BacktestResult(roi, bets);
    }
}