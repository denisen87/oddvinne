package no.dittnavn.footy.analysis.simulation;

import no.dittnavn.footy.model.Match;
import no.dittnavn.footy.model.MatchRecord;

public class ValueFinder {

    public static Bet findBet(MatchRecord p, Match m) {

        if (p == null) return null;

        // 🔥 SUPER VIKTIG
        if (p.getConfidence() < 0.2) return null;

        double homeEdge = p.predictedHome - (1.0 / m.getHomeOdds());
        double drawEdge = p.predictedDraw - (1.0 / m.getDrawOdds());
        double awayEdge = p.predictedAway - (1.0 / m.getAwayOdds());

        // 🔥 DEBUG HER
        System.out.println(
                "Edges: H=" + homeEdge +
                        " D=" + drawEdge +
                        " A=" + awayEdge +
                        " | CONF=" + p.getConfidence()
        );


        if (m.getHomeOdds() > 5.0) homeEdge = -1;
        if (m.getDrawOdds() > 5.0) drawEdge = -1;
        if (m.getAwayOdds() > 5.0) awayEdge = -1;


        double threshold = 0.01;

        double bestEdge = threshold;
        String bestType = null;
        double bestOdds = 0;

// finn beste bet
        if (homeEdge > bestEdge) {
            bestEdge = homeEdge;
            bestType = "HOME";
            bestOdds = m.getHomeOdds();
        }

        if (drawEdge > bestEdge) {
            bestEdge = drawEdge;
            bestType = "DRAW";
            bestOdds = m.getDrawOdds();
        }

        if (awayEdge > bestEdge) {
            bestEdge = awayEdge;
            bestType = "AWAY";
            bestOdds = m.getAwayOdds();
        }

        if (bestType != null) {
            return new Bet(bestType, bestOdds, bestEdge);
        }


        return null;
    }

}