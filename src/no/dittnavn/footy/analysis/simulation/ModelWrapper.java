package no.dittnavn.footy.analysis.simulation;

import no.dittnavn.footy.model.Match;
import no.dittnavn.footy.analysis.learning.ModelWeights;
import no.dittnavn.footy.analysis.PredictionEngine;
import no.dittnavn.footy.model.MatchRecord;
import no.dittnavn.footy.stats.TeamStats;

import java.util.Map;

public class ModelWrapper {

    private int matchCount = 0;

    private ModelWeights weights = new ModelWeights();

    private Map<String, TeamStats> stats;

    // 🔥 Constructor
    public ModelWrapper(Map<String, TeamStats> stats) {
        this.stats = stats;
    }

    // 🔹 PREDICT
    public MatchRecord predict(Match m) {

        TeamStats home = getStats(m.getHomeTeam());
        TeamStats away = getStats(m.getAwayTeam());

        return PredictionEngine.predict(m, home, away);
    }

    // 🔹 UPDATE (learning)
    public void update(Match m, MatchRecord p) {

        matchCount++;

        if (p != null) {
            double error = calculateError(p, m);
            weights.adjust(error);
        }
    }

    public int getMatchCount() {
        return matchCount;
    }

    // 🔹 ERROR (enkelt foreløpig)
    private double calculateError(MatchRecord p, Match m) {

        double actualHome = m.getHomeGoals() > m.getAwayGoals() ? 1 : 0;
        double actualDraw = m.getHomeGoals() == m.getAwayGoals() ? 1 : 0;
        double actualAway = m.getHomeGoals() < m.getAwayGoals() ? 1 : 0;

        return (actualHome - p.predictedHome)
                + (actualDraw - p.predictedDraw)
                + (actualAway - p.predictedAway);
    }

    // 🔹 HENT STATS
    private TeamStats getStats(String team) {
        return stats.getOrDefault(team, new TeamStats(team));
    }
}