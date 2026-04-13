package no.dittnavn.footy.engine;

import no.dittnavn.footy.analysis.poisson.PoissonPredictor;
import no.dittnavn.footy.db.DatabaseManager;
import no.dittnavn.footy.model.Match;
import no.dittnavn.footy.stats.StatsIndeks;
import no.dittnavn.footy.stats.TeamStats;
import no.dittnavn.footy.model.MatchRecord;
import no.dittnavn.footy.loader.CsvMatchLoader;
import java.io.File;
import java.util.ArrayList;
import no.dittnavn.footy.loader.CsvHistoricalLoader;
import no.dittnavn.footy.model.BacktestResult;



import java.util.List;

public class BacktestEngine {

    private static double[] marketProbs(Match m) {


        double h = m.getHomeOdds();
        double d = m.getDrawOdds();
        double a = m.getAwayOdds();

        if (h <= 1.01 || d <= 1.01 || a <= 1.01 ||
                Double.isNaN(h) || Double.isNaN(d) || Double.isNaN(a)) {

            return new double[]{0.33, 0.33, 0.34}; // fallback
        }

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

    public static void run() {

        System.out.println("🔥 USING DB BACKTEST 🔥");

        List<Match> matches =
                DatabaseManager.getHistoricalMatchesOrdered();

        FeatureConfig config = new FeatureConfig(); // default

        run(matches, config); // ✅ riktig metode
    }

    public static BacktestResult run(List<Match> matches, FeatureConfig config) {
        System.out.println("🔥 USING CSV BACKTEST 🔥");

        int debugCounter = 0;

        System.out.println("=== BACKTEST START ===");

        StatsIndeks stats = new StatsIndeks();
        PoissonPredictor predictor = new PoissonPredictor();

        double totalProfit = 0.0;
        int bets = 0;

        double totalBrier = 0.0;
        int matchCount = 0;

        int homeWins = 0;
        int drawWins = 0;
        int awayWins = 0;

        int predHome = 0;
        int predDraw = 0;
        int predAway = 0;

        // 🔥 hybrid (start konservativt)
        double wModel = 0.7;
        double wMarket = 0.3;
        double learningRate = 0.01;


        double homeBias = 0.89;
        double drawBias = 1.00;
        double awayBias = 1.03;

        double biasLearningRate = 0.01; // (kan brukes senere)

        for (Match m : matches) {

            System.out.println("MATCH: " + m.getHomeTeam() + " vs " + m.getAwayTeam());

            TeamStats homeStats = stats.getTeam(m.getHomeTeam());
            TeamStats awayStats = stats.getTeam(m.getAwayTeam());

            // ❗ Første gang lag dukker opp
            if (homeStats == null || awayStats == null) {
                stats.update(m);
            }

            // ❗ Ikke nok historikk enda
            if (homeStats.getGames() < 5 || awayStats.getGames() < 5) {
                stats.update(m);
                continue;
            }

            if (m.getHomeOdds() <= 1.01 ||
                    m.getDrawOdds() <= 1.01 ||
                    m.getAwayOdds() <= 1.01) {

                System.out.println("⚠️ SKIPPER DÅRLIGE ODDS: "
                        + m.getHomeTeam() + " vs " + m.getAwayTeam());

                stats.update(m);
                continue;
            }

            // ✅ PREDICT
            double[] poisson = predictor.predict(homeStats, awayStats);
            double[] market = marketProbs(m);


            // ✅ PREDICT (uten å vite resultatet)
            double pHome = poisson[0] * wModel + market[0] * wMarket;
            double pDraw = poisson[1] * wModel + market[1] * wMarket;
            double pAway = poisson[2] * wModel + market[2] * wMarket;

            System.out.println("BEFORE: " + poisson[0]);
            System.out.println("AFTER: " + pHome);
/*
            double shotDiff =
                    homeStats.getShotsOnTargetPerMatch() -
                            awayStats.getShotsOnTargetPerMatch();

 */

            double shotNetHome =
                    homeStats.getShotsOnTargetPerMatch() -
                            homeStats.getShotsOnTargetAgainstPerMatch();

            double shotNetAway =
                    awayStats.getShotsOnTargetPerMatch() -
                            awayStats.getShotsOnTargetAgainstPerMatch();


            double homeShotsOnTarget = homeStats.getShotsOnTargetPerMatch();
            double awayShotsOnTarget = awayStats.getShotsOnTargetPerMatch();

            boolean missingSot =
                    homeShotsOnTarget <= 0 || awayShotsOnTarget <= 0;

            double sotWeight = missingSot ? 0.0 : 1.0;

            double homeGoals = homeStats.getGoalsScoredPerMatch();
            double awayGoals = awayStats.getGoalsScoredPerMatch();

            // 🔥 Shot efficiency
            double homeEff = (homeShotsOnTarget == 0) ? 0 : homeGoals / homeShotsOnTarget;
            double awayEff = (awayShotsOnTarget == 0) ? 0 : awayGoals / awayShotsOnTarget;

            double diff = shotNetHome - shotNetAway;

            pHome += diff * 0.008 * sotWeight;
            pAway -= diff * 0.008 * sotWeight;

            double effDiff = homeEff - awayEff;

// clamp (veldig viktig)
            effDiff = Math.max(-0.25, Math.min(0.25, effDiff));

// legg til i probs
            pHome += effDiff * 0.01 * sotWeight;
            pAway -= effDiff * 0.01 * sotWeight;

            double defDiff =
                    awayStats.getShotsOnTargetAgainstPerMatch() -
                            homeStats.getShotsOnTargetAgainstPerMatch();

            // clamp
            defDiff = Math.max(-2.0, Math.min(2.0, defDiff));

            pHome += defDiff * 0.01 * sotWeight;
            pAway -= defDiff * 0.01 * sotWeight;

            // 🔥 Form
            double homeForm = homeStats.getFormScore();
            double awayForm = awayStats.getFormScore();

            double formDiff = homeForm - awayForm;

// clamp (viktig)
            formDiff = Math.max(-0.3, Math.min(0.3, formDiff));

// legg til

            pHome += formDiff * 0.01;
            pAway -= formDiff * 0.01;


            // bias først
            pHome *= homeBias;
            pDraw *= drawBias;
            pAway *= awayBias;


            double alpha = 0.88;

            pHome = 0.33 + (pHome - 0.33) * alpha;
            pDraw = 0.33 + (pDraw - 0.33) * alpha;
            pAway = 0.33 + (pAway - 0.33) * alpha;


// så draw fix

// 🔥 global draw correction (viktig!)
            pHome *= 0.96;
            pDraw *= 0.94;
            pAway *= 1.04;


            // 🔒 CLAMP (HER!)
            pHome = Math.max(0.05, Math.min(0.85, pHome));
            pDraw = Math.max(0.10, Math.min(0.60, pDraw));
            pAway = Math.max(0.05, Math.min(0.85, pAway));

// 🔥 optional edge fix
            if (pDraw > pHome && pDraw > pAway &&
                    (pDraw - Math.max(pHome, pAway)) < 0.03) {

                pDraw *= 0.9;
            }


// 🔥 normaliser
            double sum = pHome + pDraw + pAway;
            pHome /= sum;
            pDraw /= sum;
            pAway /= sum;

            double maxProb = Math.max(pHome, Math.max(pDraw, pAway));

            if (maxProb < 0.54) {
                stats.update(m);
                continue;
            }

            if (Math.abs(pHome - pAway) < 0.18) {
                stats.update(m);
                continue;
            }

            if (pAway > 0.75) {
                stats.update(m);
                continue;
            }

            double[] probs = new double[]{pHome, pDraw, pAway};

            MatchRecord p = new MatchRecord();
            p.predictedHome = probs[0];
            p.predictedDraw = probs[1];
            p.predictedAway = probs[2];

            stats.update(m);

// fake confidence midlertidig
            p.setProfit(0); // valgfritt

            p.setClosingOdds(m.getHomeOdds(), m.getDrawOdds(), m.getAwayOdds());

            System.out.println("PREDICTION: " + (p == null ? "NULL" : "OK"));
/*
            // Hopp hvis vi ikke har nok historikk
            if (!stats.hasTeam(m.getHomeTeam()) ||
                    !stats.hasTeam(m.getAwayTeam())) {

                stats.update(m);
                continue;
            }

 */


// 🔥 Krev minimum total historikk
// DEBUG – la alt passere
            if (homeStats.getGames() < 1 ||
                    awayStats.getGames() < 1) {

                stats.update(m);
                continue;
            }

            System.out.println(
                    homeStats.getName() +
                            " HG=" + homeStats.getHomeGames() +
                            " AG=" + homeStats.getAwayGames() +
                            " GS=" + homeStats.getHomeGoalsScoredPerMatch()
            );


            System.out.println("DEBUG HIT");

            System.out.println(
                    "Probs: "
                            + String.format("%.4f", pHome) + " / "
                            + String.format("%.4f", pDraw) + " / "
                            + String.format("%.4f", pAway)
            );


// renormaliser

            if (pHome > 0.4) predHome++;
            if (pDraw > 0.25) predDraw++;
            if (pAway > 0.4) predAway++;

            // Faktisk resultat
            String actual =
                    m.getHomeGoals() > m.getAwayGoals() ? "HOME" :
                            m.getHomeGoals() < m.getAwayGoals() ? "AWAY" :
                                    "DRAW";


// ======================
// 🔥 LEARNING (NY KODE)
// ======================

// hva skjedde faktisk?
            double oHome = actual.equals("HOME") ? 1.0 : 0.0;
            double oDraw = actual.equals("DRAW") ? 1.0 : 0.0;
            double oAway = actual.equals("AWAY") ? 1.0 : 0.0;

// hvor bra var modell vs marked?
            double modelError =
                    Math.abs(pHome - oHome) +
                            Math.abs(pDraw - oDraw) +
                            Math.abs(pAway - oAway);

            double marketError =
                    Math.abs(market[0] - oHome) +
                            Math.abs(market[1] - oDraw) +
                            Math.abs(market[2] - oAway);

// oppdater vekter
            double momentum = 0.95;

// hvis modell var bedre → 1, ellers 0
            double target = (modelError < marketError) ? 1.0 : 0.0;

// smooth update
            wModel = momentum * wModel + (1 - momentum) * target;
            wModel = Math.max(0.55, Math.min(0.75, wModel));
            wMarket = 1.0 - wModel;

// debug
            System.out.println("Weights -> Model: " + wModel + " Market: " + wMarket);

// debug

            if (actual.equals("HOME")) homeWins++;
            if (actual.equals("DRAW")) drawWins++;
            if (actual.equals("AWAY")) awayWins++;

            // ======================
            // BRIER SCORE (ALLE kamper)
            // ======================


            // ======================
            // EDGE-BEREGNING
            // ======================



            double brier =
                    Math.pow(pHome - oHome, 2) +
                            Math.pow(pDraw - oDraw, 2) +
                            Math.pow(pAway - oAway, 2);

            totalBrier += brier;
            matchCount++;

            // ======================
            // EDGE-BEREGNING
            // ======================

            double bestEdge = 0.0;
            String bet = null;

            if (m.getHomeOdds() > 0) {
                double adjustedP = pHome * 0.95;
                double implied = (1.0 / m.getHomeOdds()) * 1.01;


                double edge = adjustedP - implied;
                if (edge > bestEdge) {
                    bestEdge = edge;
                    bet = "HOME";
                }
            }

            if (m.getDrawOdds() > 0) {
                double adjustedP = pDraw * 0.92;
                double implied = (1.0 / m.getDrawOdds()) * 1.02;

                double edge = adjustedP - implied;
                if (edge > bestEdge) {
                    bestEdge = edge;
                    bet = "DRAW";
                }
            }

            if (m.getAwayOdds() > 0) {
                double adjustedP = pAway * 0.95;
                double implied = (1.0 / m.getAwayOdds()) * 1.01;

                double edge = adjustedP - implied;
                if (edge > bestEdge) {
                    bestEdge = edge;
                    bet = "AWAY";
                }
            }

            if (bet == null || bestEdge < 0.22) {
                continue;
            }

            if (bet.equals("AWAY") && pAway < 0.45) {
                stats.update(m);
                continue;
            }

            double prob =
                    bet.equals("HOME") ? pHome :
                    bet.equals("DRAW") ? pDraw :
                    pAway;

            if (prob < 0.51) {
                stats.update(m);
                continue;
            }

            if (bet.equals("AWAY") && m.getAwayOdds() > 3.0) {
                stats.update(m);
                continue;
            }

            if (bet.equals("DRAW")) {
                continue;
            }



            // ======================
            // SPILL
            // ======================

            System.out.println(
                    "Edges: H=" + ((pHome * 0.95) - (1.0/m.getHomeOdds() * 1.01)) +
                            " D=" + ((pDraw * 0.95) - (1.0/m.getDrawOdds() * 1.01)) +
                            " A=" + ((pAway * 0.95) - (1.0/m.getAwayOdds() * 1.01))
            );

// ======================
// SPILL
// ======================

            if (bet != null && bestEdge > 0.07) {

                // 🔥 skip ekstrem odds (optional)
                double odds =
                        bet.equals("HOME") ? m.getHomeOdds() :
                                bet.equals("DRAW") ? m.getDrawOdds() :
                                        m.getAwayOdds();

                if (odds > 3.5) {
                    stats.update(m);
                    continue;
                }

                if (bet.equals("AWAY") && odds > 2.5) {
                    stats.update(m);
                    continue;
                }

                // ❌ dropp draw bets
                if (bet.equals("DRAW")) {
                    stats.update(m);
                    continue;
                }

                boolean win =
                        (bet.equals("HOME") && actual.equals("HOME")) ||
                                (bet.equals("DRAW") && actual.equals("DRAW")) ||
                                (bet.equals("AWAY") && actual.equals("AWAY"));

                // 🔥 DEBUG: lær av dårlige bets
                if (!win) {
                    System.out.println("❌ BAD BET: " + bet +
                            " | Edge=" + bestEdge +
                            " | " + m.getHomeTeam() + " vs " + m.getAwayTeam());
                }


                prob =
                        bet.equals("HOME") ? pHome :
                                bet.equals("DRAW") ? pDraw :
                                        pAway;

                double implied = 1.0 / odds;

                double kelly = (prob - implied) / (odds - 1.0);

                double stake = Math.max(0, kelly * 0.5);

                if (stake > 0) {
                    bets++;

                    if (win) {
                        totalProfit += stake * (odds - 1.0);
                    } else {
                        totalProfit -= stake;
                    }
                }
            }

            stats.update(m);
        }

        // ======================
        // RESULTATER
        // ======================

        System.out.println("Bets: " + bets);
        System.out.println("Total profit: " + totalProfit);

        if (bets > 0) {
            double roi = (totalProfit / bets) * 100.0;
            System.out.printf("ROI: %.2f%%\n", roi);
        } else {
            System.out.println("ROI: 0%");
        }

        if (matchCount > 0) {
            System.out.println("Avg Brier: " + (totalBrier / matchCount));
        }

        System.out.println("\n=== Outcome distribution ===");
        System.out.println("Actual HOME: " + homeWins);
        System.out.println("Actual DRAW: " + drawWins);
        System.out.println("Actual AWAY: " + awayWins);

        System.out.println("\nPredicted max HOME: " + predHome);
        System.out.println("Predicted max DRAW: " + predDraw);
        System.out.println("Predicted max AWAY: " + predAway);

        System.out.println("=== BACKTEST DONE ===");

// 🔥 RETURN HER (inne i metoden)
        double roi = bets > 0 ? totalProfit / bets : 0.0;

        return new BacktestResult(totalProfit, roi, bets);



    }

}