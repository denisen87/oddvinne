package no.dittnavn.footy.analysis;

import no.dittnavn.footy.stats.TeamStats;

import no.dittnavn.footy.analysis.learning.ModelWeights;

import no.dittnavn.footy.analysis.features.MatchFeatures;


import java.util.Random;


public class ProbabilityAnalysis {


    private ModelWeights weights;

    public ProbabilityAnalysis(ModelWeights weights) {
        this.weights = weights;
    }


    public double homeWinProbability(TeamStats home, TeamStats away) { // legger inn hjemmelag og bortelag som parametre,

        double homeStrength = teamStrength(home, true); //beregner hjemmelagets hjemme styrke basert på metoden teamstrength,
        double awayStrength = teamStrength(away, false); //beregner bortelagets borte styrke basert på metoden teamstrength,

        double diff = homeStrength - awayStrength; // regner differansen i styrke mellom disse to lagene

        double homeProb = 1.0 / (1 + Math.exp(-diff / 400)); // beregner hvor mye denne differansen påvirker hjemmelagets -
// sannsynlighet for å vinne
        return homeProb;
    }


    public double drawProbability(TeamStats home, TeamStats away) {

        double homeForm = home.getHomeFormScore();
        double awayForm = away.getAwayFormScore();

        double adjustedHome =
                homeWinProbabilityRaw(home, away, homeForm, awayForm);

        double adjustedAway =
                awayWinProbabilityRaw(home, away, homeForm, awayForm);

        double adjustedDraw =
                drawProbabilityRaw(homeForm, awayForm);

        double sum = adjustedHome + adjustedAway + adjustedDraw;
        return adjustedDraw / sum;
    }

    public double awayWinProbability(TeamStats home, TeamStats away) {

        double homeForm = home.getHomeFormScore();
        double awayForm = away.getAwayFormScore();

        double adjustedHome =
                homeWinProbabilityRaw(home, away, homeForm, awayForm);

        double adjustedAway =
                awayWinProbabilityRaw(home, away, homeForm, awayForm);

        double adjustedDraw =
                drawProbabilityRaw(homeForm, awayForm);

        double sum = adjustedHome + adjustedAway + adjustedDraw;
        return adjustedAway / sum;
    }

    // ---------------- PRIVATE HJELPEMETODER ----------------

    private double homeWinProbabilityRaw(
            TeamStats home, TeamStats away,
            double homeForm, double awayForm) {

        double base =
                (home.getWins() / (double) home.getGames() +
                        away.getLosses() / (double) away.getGames()) / 2.0;

        return base * (0.6 + 0.4 * homeForm);
    }

    private double awayWinProbabilityRaw(
            TeamStats home, TeamStats away,
            double homeForm, double awayForm) {

        double homeStrength = teamStrength(home, true);
        double awayStrength = teamStrength(away, false);

        double base = awayStrength / (homeStrength + awayStrength);

        return base * (0.6 + 0.4 * awayForm);
    }

    private double drawProbabilityRaw(double homeForm, double awayForm) {

        double base = 0.25; // baseline uavgjort
        double similarity = 1 - Math.abs(homeForm - awayForm);

        return base * (0.5 + similarity);
    }


    private double teamStrength(TeamStats team, boolean isHome) {

        // --- grunnstats ---
        double winrate = (double) team.getWins() / Math.max(1, team.getGames());
        double form;

        if (isHome) {
            form = team.getHomeFormScore();
        } else {
            form = team.getAwayFormScore();
        }

        double rating = team.getElo();

        // --- hent lagprofil ---
        var p = team.getProfile();

        double injuryPenalty = p.getInjuredPlayers() * 15;
        double suspensionPenalty = p.getSuspendedPlayers() * 20;
        double coachBoost = p.getCoachRating() * 50;
        double motivationBoost = p.getMotivation() * 40;
        double fatiguePenalty = p.getFatigue() * 60;

        double extra =
                coachBoost +
                        motivationBoost -
                        injuryPenalty -
                        suspensionPenalty -
                        fatiguePenalty;

        double strength =
                rating * weights.ratingWeight +
                        form * weights.formWeight * 200 +
                        winrate * weights.winrateWeight * 100 +
                        extra * weights.extraWeight;

        return strength;

    }

    public SimulationResult simulateMatch(
            double pHome,
            double pDraw,
            double pAway,
            int simulations) {

        Random random = new Random();

        int homeWins = 0;
        int draws = 0;
        int awayWins = 0;

        for (int i = 0; i < simulations; i++) {

            double r = random.nextDouble();

            if (r < pHome) {
                homeWins++;
            } else if (r < pHome + pDraw) {
                draws++;
            } else {
                awayWins++;
            }
        }

        return new SimulationResult(homeWins, draws, awayWins, simulations);
    }
}
