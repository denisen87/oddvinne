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



import java.util.List;

public class BacktestEngine {

    private static double[] marketProbs(Match m) {

        if (m.getHomeOdds() <= 0 ||
                m.getDrawOdds() <= 0 ||
                m.getAwayOdds() <= 0) {

            return new double[]{0.33, 0.33, 0.34}; // fallback
        }

        double h = 1.0 / m.getHomeOdds();
        double d = 1.0 / m.getDrawOdds();
        double a = 1.0 / m.getAwayOdds();

        double sum = h + d + a;

        return new double[]{
                h / sum,
                d / sum,
                a / sum
        };
    }

    public static void run() {
        List<Match> matches = new ArrayList<>();

        File dataFolder = new File("data");

        for (File file : dataFolder.listFiles()) {

            if (file.getName().endsWith(".csv")
                    && !file.getName().contains("odds")) {

                String league = file.getName().replace(".csv", "");

                matches.addAll(
                        CsvHistoricalLoader.load(file.getPath(), league)
                );
            }
        }

        System.out.println("MATCHES WITH ODDS: " + matches.size());
        run(matches); // test med én først
    }

    public static void run(List<Match> matches) {
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

        double homeBias = 1.0;
        double drawBias = 1.0;
        double awayBias = 1.0;

        double biasLearningRate = 0.01; // (kan brukes senere)

        for (Match m : matches) {

            System.out.println("MATCH: " + m.getHomeTeam() + " vs " + m.getAwayTeam());

            TeamStats homeStats = stats.getTeam(m.getHomeTeam());
            TeamStats awayStats = stats.getTeam(m.getAwayTeam());

            // ❗ Første gang lag dukker opp
            if (homeStats == null || awayStats == null) {
                stats.update(m);
                continue;
            }

            // ❗ Ikke nok historikk enda
            if (homeStats.getGames() < 5 || awayStats.getGames() < 5) {
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

            // bias først
            pHome *= homeBias;
            pDraw *= drawBias;
            pAway *= awayBias;

// så draw fix

// 🔥 global draw correction (viktig!)
            pDraw *= 0.92;

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
                double edge = pHome - (1.0 / m.getHomeOdds());
                if (edge > bestEdge) {
                    bestEdge = edge;
                    bet = "HOME";
                }
            }

            if (m.getDrawOdds() > 0) {
                double edge = pDraw - (1.0 / m.getDrawOdds());
                if (edge > bestEdge) {
                    bestEdge = edge;
                    bet = "DRAW";
                }
            }

            if (m.getAwayOdds() > 0) {
                double edge = pAway - (1.0 / m.getAwayOdds());
                if (edge > bestEdge) {
                    bestEdge = edge;
                    bet = "AWAY";
                }
            }

            // ======================
            // SPILL
            // ======================

            System.out.println(
                    "Edges: H=" + (pHome - 1.0/m.getHomeOdds()) +
                            " D=" + (pDraw - 1.0/m.getDrawOdds()) +
                            " A=" + (pAway - 1.0/m.getAwayOdds())
            );

// ======================
// SPILL
// ======================

            if (bet != null && bestEdge > 0.05) {

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

                double odds =
                        bet.equals("HOME") ? m.getHomeOdds() :
                                bet.equals("DRAW") ? m.getDrawOdds() :
                                        m.getAwayOdds();

                double prob =
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
    }

}