package no.dittnavn.footy.analysis;

import no.dittnavn.footy.stats.TeamStats;

import no.dittnavn.footy.model.Match;
import no.dittnavn.footy.model.MatchRecord;
import no.dittnavn.footy.stats.TeamStats;

public class PredictionEngine {

    // ===============================
    // OFFENTLIG METODE – 1X2 OUTPUT
    // ===============================

    public double[] calculateProbabilities(TeamStats home, TeamStats away) {

        double homeStrength = calculateTeamStrength(home, true);
        double awayStrength = calculateTeamStrength(away, false);

        double strengthDiff = homeStrength - awayStrength;

        // Logistisk modell for home/away
        double homeWin = logistic(strengthDiff);
        double awayWin = logistic(-strengthDiff);

        // Draw sannsynlighet basert på hvor jevn kampen er
        double draw = calculateDrawProbability(strengthDiff);

        // Normaliser slik at total = 1
        double total = homeWin + draw + awayWin;

        homeWin /= total;
        draw /= total;
        awayWin /= total;

        return new double[]{homeWin, draw, awayWin};
    }

    // ===============================
    // LOGISTISK FUNKSJON
    // ===============================

    private double logistic(double x) {
        double scaling = 350.0;  // Sensitivitet
        return 1.0 / (1.0 + Math.exp(-x / scaling));
    }

    // ===============================
    // DRAW MODELL
    // ===============================

    private double calculateDrawProbability(double strengthDiff) {

        double closeness = Math.exp(-Math.abs(strengthDiff) / 300.0);

        // Baseline draw rate i fotball ~25–30%
        return 0.22 + (closeness * 0.18);
    }

    // ===============================
    // TEAM STRENGTH (KJERNELOGIKK)
    // ===============================

    private double calculateTeamStrength(TeamStats team, boolean isHome) {

        double elo = team.getElo();
        double form;
        double winrate = 0;
        double goalDiffImpact = 0;

        if (team.getGames() > 0) {
            winrate = (double) team.getWins() / team.getGames();
            goalDiffImpact =
                    ((double) (team.getGoalsScored()
                            - team.getGoalsConceded())
                            / team.getGames()) * 120;
        }

        if (isHome) {
            form = team.getHomeFormScore();
            elo *= 1.08;   // moderat hjemmeboost
        } else {
            form = team.getAwayFormScore();
            elo *= 0.97;   // liten bortestraff
        }

        return elo
                + (form * 150)
                + (winrate * 200)
                + goalDiffImpact;
    }

    public static MatchRecord predict(Match m, TeamStats home, TeamStats away) {

        PredictionEngine engine = new PredictionEngine();

        double[] probs = engine.calculateProbabilities(home, away);

        MatchRecord r = new MatchRecord();

        r.homeTeam = m.getHomeTeam();
        r.awayTeam = m.getAwayTeam();

        r.predictedHome = probs[0];
        r.predictedDraw = probs[1];
        r.predictedAway = probs[2];

        r.oddsHome = m.getHomeOdds();
        r.oddsDraw = m.getDrawOdds();
        r.oddsAway = m.getAwayOdds();

        return r;
    }

}