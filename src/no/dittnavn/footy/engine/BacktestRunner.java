package no.dittnavn.footy.engine;

import no.dittnavn.footy.model.Match;
import no.dittnavn.footy.model.BacktestResult;
import no.dittnavn.footy.model.MatchRecord;
import no.dittnavn.footy.db.DatabaseManager;

import java.util.List;

public class BacktestRunner {

    public static BacktestResult run(
            List<Match> matches,
            double maxProb,
            double prob,
            double edgeThreshold,
            double homeBias,
            double minConfidence,
            double minOdds,
            double maxOdds,
            double probThreshold,
            FeatureConfig config
    ) {

        double totalProfit = 0.0;
        int bets = 0;

        System.out.println("TOTAL MATCHES: " + matches.size());

        for (Match match : matches) {

            double odds;
            boolean betHome;

            // 🔥 SOT-basert valg (din modell)

            double probHome = estimateProbability(match, homeBias);

            betHome = probHome > 0.5;

            String predicted = betHome ? "HOME" : "AWAY";

            double probValue = betHome ? probHome : (1.0 - probHome);

            if (betHome) {
                odds = match.getHomeOdds();
            } else {
                odds = match.getAwayOdds();
            }

            if (odds <= 1.01) {
                odds = 1.8;
            }

            if (!config.useHomeBias) {
                homeBias = 1.0;
            }

            // 🔥 probability
            probValue = estimateProbability(match, homeBias);


            if (probValue == 0.5) {
                System.out.println("⚠️ NO SIGNAL -> "
                        + match.getHomeTeam() + " vs " + match.getAwayTeam());
            }

            if (!betHome) {
                probValue = 1.0 - probValue;
            }

            // 🔥 lag pseudo probs for debug
            double pHome = betHome ? probValue : (1 - probValue);
            double pAway = betHome ? (1 - probValue) : probValue;
            double pDraw = 0.0; // du bruker ikke draw her

            predicted = betHome ? "HOME" : "AWAY";

            String actual =
                    match.getHomeGoals() > match.getAwayGoals() ? "HOME" :
                            match.getHomeGoals() < match.getAwayGoals() ? "AWAY" :
                                    "DRAW";

            double implied = 1.0 / odds;
            double edge = probValue - implied;
            double confidence = calculateConfidence(match);

            // 🔥 PRINT (Riktig!)
            System.out.println(
                    "BACKTEST MATCH -> "
                            + match.getHomeTeam() + " vs " + match.getAwayTeam()
                            + " | odds=" + odds
                            + " | pred=" + predicted
                            + " | prob=" + round(probValue)
                            + " | edge=" + round(edge)
                            + " | actual=" + actual
            );

            // -------------------------
            // BET
            // -------------------------

            bets++;
            double stake = 1.0;

            boolean win = betHome
                    ? match.getHomeGoals() > match.getAwayGoals()
                    : match.getAwayGoals() > match.getHomeGoals();

            if (win) {
                totalProfit += (odds - 1.0) * stake;
            } else {
                totalProfit -= stake;
            }
        }

        // -------------------------
        // RESULT SUMMARY
        // -------------------------

        double roi = bets > 0 ? totalProfit / bets : 0.0;

        System.out.println("Bets: " + bets);
        System.out.println("Total profit: " + totalProfit);
        System.out.println("ROI: " + roi);

        return new BacktestResult(totalProfit, roi, bets);
    }

    private static double estimateProbability(Match m, double homeBias) {

        int h = m.getHomeShotsTarget();
        int a = m.getAwayShotsTarget();

        if (h <= 0 && a <= 0) return -1;

        double total = h + a;
        if (total == 0) return -1;

        double base = (double) h / total;

        double prob = base * homeBias;

        // clamp
        if (prob < 0.10) prob = 0.10;
        if (prob > 0.90) prob = 0.90;

        return prob;
    }

    private static double calculateConfidence(Match m) {

        int total = m.getHomeShotsTarget() + m.getAwayShotsTarget();

        if (total <= 0) return 0.0;

        return Math.min(1.0, total / 8.0);
    }

    private static double round(double v) {
        return Math.round(v * 1000.0) / 1000.0;
    }
}