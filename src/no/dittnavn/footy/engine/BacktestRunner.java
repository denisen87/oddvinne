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

            // 🔥 SOT-basert valg (din modell)

            double probHome = estimateProbability(match, homeBias, probThreshold);

            if (probHome < 0) continue;

// -------------------------
// 🔥 DRAW + 3-WAY MODEL
// -------------------------

            int h = match.getHomeShotsTarget();
            int a = match.getAwayShotsTarget();

            double total = h + a;
            if (total == 0) continue;

            double diff = Math.abs(h - a);

// draw sannsynlighet
            double pDraw = Math.max(0.15, 1.0 - (diff / total));
            pDraw = Math.min(0.30, pDraw);

// 🔥 HER du lurte på
            double remaining = 1.0 - pDraw;

            double pHome = probHome * remaining;
            double pAway = (1.0 - probHome) * remaining;

            double oHome = match.getHomeOdds();
            double oDraw = match.getDrawOdds();
            double oAway = match.getAwayOdds();

            double edgeHome = pHome - (1.0 / oHome);
            double edgeDraw = pDraw - (1.0 / oDraw);
            double edgeAway = pAway - (1.0 / oAway);

            double maxEdge = Math.max(edgeHome, Math.max(edgeDraw, edgeAway));

// 🔥 la draw slippe gjennom

            String predicted;
            double probValue;

            if (maxEdge == edgeHome) {
                predicted = "HOME";
                probValue = pHome;
                odds = oHome;
            } else if (maxEdge == edgeDraw) {
                predicted = "DRAW";
                probValue = pDraw;
                odds = oDraw;
            } else {
                predicted = "AWAY";
                probValue = pAway;
                odds = oAway;
            }

            double confidence = calculateConfidence(match);

// 🔥 NY VALUE
            double value = (probValue * odds - 1.0) * confidence;

// 🔥 AWAY straff
            if (predicted.equals("AWAY")) {
                value *= 1;

            }

// 🔥 FILTER
            if (value < edgeThreshold) continue;

            if (odds <= 1.01) {
                odds = 1.8;
            }


            if (!config.useHomeBias) {
                homeBias = 1.0;
            }

            // 🔥 probability


            if (probValue == 0.5) {
                System.out.println("⚠️ NO SIGNAL -> "
                        + match.getHomeTeam() + " vs " + match.getAwayTeam());
            }


            // 🔥 lag pseudo probs for debug

            String actual =
                    match.getHomeGoals() > match.getAwayGoals() ? "HOME" :
                            match.getHomeGoals() < match.getAwayGoals() ? "AWAY" :
                                    "DRAW";

            double implied = 1.0 / odds;
            double edge = probValue - implied;


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

            boolean win =
                    (predicted.equals("HOME") && match.getHomeGoals() > match.getAwayGoals()) ||
                            (predicted.equals("AWAY") && match.getAwayGoals() > match.getHomeGoals()) ||
                            (predicted.equals("DRAW") && match.getHomeGoals() == match.getAwayGoals());

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

    private static double estimateProbability(Match m, double homeBias, double probThreshold) {


        int h = m.getHomeShotsTarget();
        int a = m.getAwayShotsTarget();

        if (h <= 0 && a <= 0) return -1;

        double total = h + a;

        double diff = Math.abs(h - a);

// jevn kamp → høy draw
        double pDraw = Math.max(0.15, 1.0 - (diff / total));
        pDraw = Math.min(0.30, pDraw); // litt lavere cap enn før

        if (total == 0) return -1;

        double base = (double) h / total;

        double prob = base * homeBias;

        prob = prob * probThreshold + 0.5 * (1 - probThreshold);

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