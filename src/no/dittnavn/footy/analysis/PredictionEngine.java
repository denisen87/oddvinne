package no.dittnavn.footy.analysis;

import no.dittnavn.footy.analysis.features.MatchFeatures;
import no.dittnavn.footy.model.Match;
import no.dittnavn.footy.model.MatchRecord;
import no.dittnavn.footy.stats.TeamStats;

public class PredictionEngine {

    // ===============================
    // PUBLIC – PREDICT 1X2
    // ===============================
    public double[] calculateProbabilities(TeamStats home, TeamStats away) {

        double homeStrength = calculateTeamStrength(home, true);
        double awayStrength = calculateTeamStrength(away, false);

        double strengthDiff = homeStrength - awayStrength;

        double homeWin = logistic(strengthDiff);
        double awayWin = logistic(-strengthDiff);
        double draw = calculateDrawProbability(strengthDiff);

        // normalize
        double total = homeWin + draw + awayWin;
        homeWin /= total;
        draw /= total;
        awayWin /= total;

        // calibration
        homeWin = 0.5 + (homeWin - 0.5) * 0.7;
        draw = 0.5 + (draw - 0.5) * 0.7;
        awayWin = 0.5 + (awayWin - 0.5) * 0.7;

        // normalize again
        double newTotal = homeWin + draw + awayWin;
        homeWin /= newTotal;
        draw /= newTotal;
        awayWin /= newTotal;

        return new double[]{homeWin, draw, awayWin};
    }

    // ===============================
    // LOGISTIC
    // ===============================
    private double logistic(double x) {
        double scaling = 350.0;
        return 1.0 / (1.0 + Math.exp(-x / scaling));
    }

    // ===============================
    // DRAW MODEL
    // ===============================
    private double calculateDrawProbability(double strengthDiff) {
        double closeness = Math.exp(-Math.abs(strengthDiff) / 300.0);
        return 0.22 + (closeness * 0.18);
    }

    // ===============================
    // TEAM STRENGTH
    // ===============================
    private double calculateTeamStrength(TeamStats team, boolean isHome) {

        boolean hasShotData =
                team.getShotsOnTargetPerMatch() > 0 ||
                        team.getShotsPerMatch() > 0;

        double elo = team.getElo();
        double form;
        double winrate = 0;
        double goalDiffImpact = 0;

        if (team.getGames() > 0) {
            winrate = (double) team.getWins() / team.getGames();
            goalDiffImpact =
                    ((double) (team.getGoalsScored() - team.getGoalsConceded())
                            / team.getGames()) * 120;
        }

        if (isHome) {
            form = team.getHomeFormScore();
            elo *= 1.08;
        } else {
            form = team.getAwayFormScore();
            elo *= 0.97;
        }

        double strength = elo
                + (form * 150)
                + (winrate * 200)
                + goalDiffImpact;

        // ===== FEATURE IMPACT =====

        double shotImpact =
                (team.getShotsPerMatch() - team.getShotsAgainstPerMatch()) * 6;

        double possessionImpact =
                (team.getPossession() - 50) * 1.5;

        double cornerImpact =
                (team.getCornersFor() - team.getCornersAgainst()) * 2;

        double matchShotDiff =
                team.getLastMatchShots() - team.getLastMatchShotsAgainst();

        double matchSotDiff =
                team.getLastMatchSOT() - team.getLastMatchSOTAgainst();

        double matchCornerDiff =
                team.getLastMatchCorners() - team.getLastMatchCornersAgainst();

        double matchImpact =
                (matchShotDiff * 2.5)
                        + (matchSotDiff * 6)
                        + (matchCornerDiff * 1.5);

        matchImpact = Math.max(-40, Math.min(40, matchImpact));

        double sot = team.getShotsOnTargetPerMatch();
        double sotAgainst = team.getShotsOnTargetAgainstPerMatch();

        double sotImpact = 0;

        if (!(sot == 0 && sotAgainst == 0)) {
            double diff = sot - sotAgainst;
            sotImpact = diff * 35;

            if (sot > 5) sotImpact *= 1.2;
            if (sot > 6 && sotAgainst < 3) sotImpact += 15;
        }

        sotImpact = Math.max(-70, Math.min(70, sotImpact));

        if (hasShotData) {
            strength += sotImpact;
            strength += shotImpact;
            strength += possessionImpact;
            strength += cornerImpact;
            strength += matchImpact;
        } else {
            // fallback
            strength += goalDiffImpact * 0.5;
            strength += form * 80;
            strength += winrate * 100;
        }

        return strength;
    }

    // ===============================
    // 🔥 MAIN PREDICT METHOD
    // ===============================
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

        // ===============================
        // 🔥 FEATURES (DET VIKTIGE!)
        // ===============================
        MatchFeatures f = new MatchFeatures(
                home,
                away,
                m.getHomeOdds(),
                m.getDrawOdds(),
                m.getAwayOdds()
        );

        // 🔥 match-level data
        f.setMatchStats(
                m.getHomeShots(),
                m.getAwayShots(),
                m.getHomeShotsTarget(),
                m.getAwayShotsTarget(),
                m.getHomeCorners(),
                m.getAwayCorners()
        );

        // attach to record
        r.features = f;

        return r;
    }
}