package no.dittnavn.footy.engine;

import no.dittnavn.footy.model.Match;
import no.dittnavn.footy.model.MatchRecord;
import no.dittnavn.footy.stats.TeamStats;
import no.dittnavn.footy.db.DatabaseManager;
import no.dittnavn.footy.util.TeamNameNormalizer;
import no.dittnavn.footy.analysis.features.MatchFeatures;
import no.dittnavn.footy.stats.StatsIndeks;
import no.dittnavn.footy.analysis.NeuralModel;
import no.dittnavn.footy.analysis.ProbabilityCalculator;
import no.dittnavn.footy.analysis.learning.PredictionTracker;
import no.dittnavn.footy.odds.OddsRepository;
import no.dittnavn.footy.analysis.poisson.PoissonPredictor;

import java.util.List;
import java.util.Map;

public class AutoPredictionEngine {

    public static void run(StatsIndeks indeks,
                           NeuralModel neural,
                           PredictionTracker tracker,
                           List<Match> fixtures) {

        System.out.println("\n=== AUTO PREDICTION ENGINE START ===");
        System.out.println("Totale fixtures mottatt: " + fixtures.size());

        // Last odds én gang
        Map<String, double[]> oddsMap = OddsRepository.loadOdds();

        for (Match m : fixtures) {

            String home = TeamNameNormalizer.normalize(m.getHomeTeam());
            String away = TeamNameNormalizer.normalize(m.getAwayTeam());

            // ----- HENT ODDS -----
            String key = home + "-" + away;

            if (!oddsMap.containsKey(key)) {
                System.out.println("⚠️ Mangler odds for " + home + " vs " + away);
                continue;
            }

            double[] o = oddsMap.get(key);
            m.setHomeOdds(o[0]);
            m.setDrawOdds(o[1]);
            m.setAwayOdds(o[2]);

            // ----- HENT STATS -----
            TeamStats homeStats = indeks.getTeam(home);
            TeamStats awayStats = indeks.getTeam(away);

            if (homeStats == null) {
                homeStats = indeks.getOrCreate(home);
            }

            if (awayStats == null) {
                awayStats = indeks.getOrCreate(away);
            }

            PoissonPredictor poisson = new PoissonPredictor();
            double[] poissonProbs = poisson.predict(homeStats, awayStats);

            double pHomePois = poissonProbs[0];
            double pDrawPois = poissonProbs[1];
            double pAwayPois = poissonProbs[2];

// 🔥 auto-opprett lag hvis de ikke finnes


            // ----- MODEL PROBABILITIES -----
            ProbabilityCalculator calc = new ProbabilityCalculator();

            double pHome = calc.homeWinProbability(homeStats, awayStats);
            double pDraw = calc.drawProbability(homeStats, awayStats);
            double pAway = calc.awayWinProbability(homeStats, awayStats);

            double weightPoisson = 0.6;
            double weightModel = 0.4;

            pHome = (pHomePois * weightPoisson) + (pHome * weightModel);
            pDraw = (pDrawPois * weightPoisson) + (pDraw * weightModel);
            pAway = (pAwayPois * weightPoisson) + (pAway * weightModel);

            double total = pHome + pDraw + pAway;
            if (total == 0) continue;

            pHome /= total;
            pDraw /= total;
            pAway /= total;

            double oddsHome = m.getHomeOdds();
            double oddsDraw = m.getDrawOdds();
            double oddsAway = m.getAwayOdds();

            // ----- IMPLIED PROBABILITIES -----
            double impHome = 1.0 / oddsHome;
            double impDraw = 1.0 / oddsDraw;
            double impAway = 1.0 / oddsAway;

            double impTotal = impHome + impDraw + impAway;

            impHome /= impTotal;
            impDraw /= impTotal;
            impAway /= impTotal;

            // ----- EDGE -----
            double edgeHome = pHome - impHome;
            double edgeDraw = pDraw - impDraw;
            double edgeAway = pAway - impAway;

            // ----- BET SELECTION -----
            String bet = "NO_BET";
            double confidence = 0.0;
            double edgeValue = 0.0;

            if (edgeHome > 0.05) {
                bet = "HOME";
                confidence = edgeHome;
                edgeValue = edgeHome;
            } else if (edgeDraw > 0.05) {
                bet = "DRAW";
                confidence = edgeDraw;
                edgeValue = edgeDraw;
            } else if (edgeAway > 0.05) {
                bet = "AWAY";
                confidence = edgeAway;
                edgeValue = edgeAway;
            }

            // ----- FEATURES -----
            MatchFeatures features = new MatchFeatures(
                    homeStats,
                    awayStats,
                    homeStats.getFormScore(),
                    awayStats.getFormScore(),
                    homeStats.getElo() - awayStats.getElo()
            );

            // ----- SAVE -----
            MatchRecord record = new MatchRecord(
                    home,
                    away,
                    features,
                    pHome,
                    pDraw,
                    pAway,
                    oddsHome,
                    oddsDraw,
                    oddsAway,
                    edgeValue,
                    bet,
                    confidence
            );

            DatabaseManager.savePrediction(record);

            // ----- OUTPUT -----
            System.out.println("\n=== PREDICTION ===");
            System.out.println(home + " vs " + away);

            System.out.println("Model probs:");
            System.out.println("Home: " + String.format("%.2f", pHome));
            System.out.println("Draw: " + String.format("%.2f", pDraw));
            System.out.println("Away: " + String.format("%.2f", pAway));

            System.out.println("Odds:");
            System.out.println("Home: " + oddsHome +
                    " Draw: " + oddsDraw +
                    " Away: " + oddsAway);

            System.out.println("Edge:");
            System.out.println("Home: " + String.format("%.3f", edgeHome));
            System.out.println("Draw: " + String.format("%.3f", edgeDraw));
            System.out.println("Away: " + String.format("%.3f", edgeAway));

            System.out.println("BET: " + bet +
                    " | confidence=" +
                    String.format("%.3f", confidence));
        }

        System.out.println("\n=== AUTO PREDICTION ENGINE DONE ===");
    }
}