package no.dittnavn.footy.engine;

import no.dittnavn.footy.model.Match;
import no.dittnavn.footy.model.BacktestResult;

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

            System.out.println("BACKTEST MATCH -> "
                    + match.getHomeTeam()
                    + " vs " + match.getAwayTeam()
                    + " | odds=" + match.getHomeOdds());

            boolean betHome = match.getHomeShotsTarget() >= match.getAwayShotsTarget();

            double odds;
            boolean win;

// velg side basert på SOT
            if (betHome) {
                odds = match.getHomeOdds();
                win = match.getHomeGoals() > match.getAwayGoals();
            } else {
                odds = match.getAwayOdds();
                win = match.getAwayGoals() > match.getHomeGoals();
            }

            // 🔥 FIX: logg dårlige odds
            if (odds <= 1.01) {
                System.out.println("BAD ODDS -> "
                        + match.getHomeTeam() + " vs " + match.getAwayTeam()
                        + " | odds=" + odds);

                odds = 1.8; // 🔥 KRITISK: fallback så vi kan bette
            }

            double probValue;

            if (!config.useHomeBias) {
                homeBias = 1.0;
            }

// 🔥 FEATURE-BASED PROBABILITY
            if (config.useSOT) {
                probValue = estimateProbability(match, homeBias);
            } else {
                probValue = 0.5; // fallback (ingen signal)
            }

// juster for away bet
            if (!betHome) {
                probValue = 1.0 - probValue;
            }

            double implied = 1.0 / odds;
            double edge = probValue - implied;
            double confidence = calculateConfidence(match);

            // -------------------------
            // FILTERS
            // -------------------------
/*
            if (probValue < probThreshold) continue;

            // 🔥 FIX: ikke blokker umulig range
            if (maxProb > probThreshold && probValue > maxProb) continue;

            if (edge < edgeThreshold) continue;

            if (odds < minOdds || odds > maxOdds) continue;

            if (config.useConfidence) {
                if (confidence < minConfidence) continue;


            }

 */

            // -------------------------
            // BET
            // -------------------------

            bets++;

            double stake = 1.0;

            win = match.getHomeGoals() > match.getAwayGoals();

            if (win) {
                totalProfit += (odds - 1.0) * stake;
            } else {
                totalProfit -= stake;
            }
        }

        double roi = bets > 0 ? totalProfit / bets : 0.0;

        System.out.println("Bets: " + bets);
        System.out.println("Total profit: " + totalProfit);
        System.out.println("ROI: " + roi);

        return new BacktestResult(totalProfit, roi, bets);

    }

    private static double estimateProbability(Match m, double homeBias) {

        int h = m.getHomeShotsTarget();
        int a = m.getAwayShotsTarget();

        if (h <= 0 && a <= 0) return 0.5;

        double total = h + a;
        if (total == 0) {
            return 0.52; // 🔥 viktig!
        }

        double base = (double) h / total;

        return Math.min(0.90, Math.max(0.10, base * homeBias));
    }

    private static double calculateConfidence(Match m) {

        int total = m.getHomeShotsTarget() + m.getAwayShotsTarget();

        if (total <= 0) return 0.0;

        return Math.min(1.0, total / 8.0);
    }

}