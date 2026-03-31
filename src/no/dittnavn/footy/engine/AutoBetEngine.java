package no.dittnavn.footy.engine;

import no.dittnavn.footy.stats.StatsIndeks;
import no.dittnavn.footy.analysis.NeuralModel;
import no.dittnavn.footy.model.Match;
import no.dittnavn.footy.db.DatabaseManager;
import no.dittnavn.footy.analysis.ProbabilityCalculator;
import no.dittnavn.footy.stats.TeamStats;
import no.dittnavn.footy.analysis.learning.PredictionTracker;
import no.dittnavn.footy.analysis.learning.PredictionRecord;
import no.dittnavn.footy.model.MatchRecord;
import no.dittnavn.footy.config.StrategyConfig;

import java.util.List;

public class AutoBetEngine {

    public static void run(StatsIndeks indeks,
                           NeuralModel neural,
                           PredictionTracker tracker,
                           List<Match> fixtures) {

        System.out.println("\n=== AUTO BET ENGINE START ===");

        ProbabilityCalculator calc = new ProbabilityCalculator();

        for (Match m : fixtures) {

            if (m.getHomeOdds() <= 0 ||
                    m.getDrawOdds() <= 0 ||
                    m.getAwayOdds() <= 0) continue;

            TeamStats homeStats = indeks.getTeam(m.getHomeTeam());
            TeamStats awayStats = indeks.getTeam(m.getAwayTeam());

            if (homeStats == null || awayStats == null) continue;

            // 🔮 Modell-probabilities
            double pHome = calc.homeWinProbability(homeStats, awayStats);
            double pDraw = calc.drawProbability(homeStats, awayStats);
            double pAway = calc.awayWinProbability(homeStats, awayStats);

// 🔧 Draw correction (Poisson undervurderer draw)
            double homeAvg = homeStats.getGoalsScored() / (double) homeStats.getGames();
            double awayAvg = awayStats.getGoalsScored() / (double) awayStats.getGames();

            double homeAdvantage = homeAvg - awayAvg;

// juster bias basert på lagstyrke
            double dynamicBias = StrategyConfig.HOME_BIAS;

            if (homeAdvantage < -0.3) {
                dynamicBias -= 0.10; // dårlig hjemmelag → mindre bias
            }
            else if (homeAdvantage > 0.5) {
                dynamicBias += 0.05; // sterkt hjemmelag → litt boost
            }

            pHome *= dynamicBias;
            double drawBoost = 1.6;
            pDraw *= drawBoost;

// 🔧 Normaliser
            double total = pHome + pDraw + pAway;
            if (total == 0) continue;

            pHome /= total;
            pDraw /= total;
            pAway /= total;

            // 📊 Bookmaker implied probability
            double bHome = 1.0 / m.getHomeOdds();
            double bDraw = 1.0 / m.getDrawOdds();
            double bAway = 1.0 / m.getAwayOdds();

            // 💰 Value
// 💰 EDGE (riktig betting edge)
            double edgeHome = (pHome * m.getHomeOdds()) - 1;
            double edgeDraw = (pDraw * m.getDrawOdds()) - 1;
            double edgeAway = (pAway * m.getAwayOdds()) - 1;

// finn beste edge
            double bestEdge = Math.max(edgeHome, Math.max(edgeDraw, edgeAway));

// filter
            if (bestEdge <= StrategyConfig.EDGE) continue;

            double maxProb = Math.max(pHome, Math.max(pDraw, pAway));
            if (maxProb < StrategyConfig.MAX_PROB) continue;

            String bet;
            double odds;
            double probability;



            if (edgeHome >= edgeDraw && edgeHome >= edgeAway) {
                bet = "HOME";
                odds = m.getHomeOdds();
                probability = pHome;
            }
            else if (edgeDraw >= edgeHome && edgeDraw >= edgeAway) {
                bet = "DRAW";
                odds = m.getDrawOdds();
                probability = pDraw;
            }
            else {
                bet = "AWAY";
                odds = m.getAwayOdds();
                probability = pAway;
            }

            if (odds < 1.5 || odds > 3.5) continue;

            if (probability < StrategyConfig.PROB) continue;

            double confidence = Math.abs(pHome - pAway);
            if (confidence < StrategyConfig.CONFIDENCE) continue;

            // 🧮 Kelly (flat bankroll = 100 units for now)
            double bankroll = 100;
            double kelly = ((probability * odds) - 1) / (odds - 1);
            double stake = Math.max(0, kelly) * bankroll;

            if (stake <= 0) continue;

            // 🧠 Learning record
            PredictionRecord pr = new PredictionRecord(
                    m.getHomeTeam(),
                    m.getAwayTeam(),
                    null,                  // features (kan være null her)
                    pHome,
                    pDraw,
                    pAway,
                    m.getHomeOdds(),
                    m.getDrawOdds(),
                    m.getAwayOdds(),
                    bet,
                    stake,
                    probability            // bruk modellens probability som confidence
            );

            pr.bet = bet;
            pr.stake = stake;
            pr.valueHome = edgeHome;
            pr.valueDraw = edgeDraw;
            pr.valueAway = edgeAway;

            tracker.addPrediction(pr);

            // 💾 Lagre i DB
            MatchRecord mr = pr.toMatchRecord();
            int id = DatabaseManager.savePrediction(mr);
            pr.dbId = id;

            System.out.println("AUTO BET: " +
                    m.getHomeTeam() + " vs " + m.getAwayTeam()
                    + " → " + bet
                    + " stake: " + String.format("%.2f", stake));
        }

        System.out.println("=== AUTO BET ENGINE DONE ===\n");
    }
}