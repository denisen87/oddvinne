package no.dittnavn.footy.analysis;

import no.dittnavn.footy.stats.TeamStats;
import no.dittnavn.footy.form.FormCalculator;
import no.dittnavn.footy.analysis.injury.InjuryImpactCalculator;
import no.dittnavn.footy.integration.playars.PlayerImpactCalculator;
import no.dittnavn.footy.integration.players.PlayerParser;

import no.dittnavn.footy.integration.playars.FootballDataPlayerClient;
import no.dittnavn.footy.integration.TeamIdMapper;
import no.dittnavn.footy.model.Player;
import no.dittnavn.footy.integration.playars.LineupPredictor;
import no.dittnavn.footy.integration.playars.StartingLineupSelector;
import no.dittnavn.footy.analysis.LeagueStrength;






import java.util.List;

public class ProbabilityCalculator {

    private static final FormCalculator form = new FormCalculator();
    private static final InjuryImpactCalculator injuryCalc = new InjuryImpactCalculator();
    private static final PlayerImpactCalculator playerCalc = new PlayerImpactCalculator();
    private static final LineupPredictor lineupPredictor = new LineupPredictor();



    // =========================
    // HOME WIN
    // =========================
    public static double homeWinProbability(TeamStats home, TeamStats away) {

        if (home.getGames() == 0 || away.getGames() == 0) {
            return 0.33;
        }

        // BASE
        double base =
                (home.getWins() / (double) home.getGames()
                        + away.getLosses() / (double) away.getGames()) / 2.0;

        // =========================
// TEAM-SPECIFIC HOME ADVANTAGE
// =========================
        double homeHomeRate = home.getHomeWins() / (double) Math.max(1, home.getHomeGames());
        double awayAwayLossRate = away.getAwayLosses() / (double) Math.max(1, away.getAwayGames());

        double teamHomeAdvantage = (homeHomeRate + awayAwayLossRate) / 2.0;

// juster base
        base = base * (0.7 + teamHomeAdvantage);

        // LEAGUE STRENGTH
        double homeStrength = LeagueStrength.getStrength(home.getName());
        double awayStrength = LeagueStrength.getStrength(away.getName());
        base = base * homeStrength / awayStrength;

        // FORM
        double homeForm = form.calculateFormScore(home);
        double awayForm = form.calculateFormScore(away);
        double formImpact = (homeForm - awayForm) * 0.25;

        // DNA
        double dnaImpact =
                (home.getDNA().getAttackIndex() - away.getDNA().getDefenseIndex()) * 0.2
                        + (home.getDNA().getTempoIndex() - away.getDNA().getTempoIndex()) * 0.1;

        // INJURIES
        double homeInjury = injuryCalc.calculateImpact(home.getProfile());
        double awayInjury = injuryCalc.calculateImpact(away.getProfile());
        double injuryImpact = awayInjury - homeInjury;

        // PLAYER IMPACT
        double playerImpact = calculatePlayerImpact(home, away);

        return clamp(base + formImpact + dnaImpact + injuryImpact + playerImpact);
    }




    // =========================
    // DRAW
    // =========================
    public static double drawProbability(TeamStats home, TeamStats away) {

        if (home.getGames() == 0 || away.getGames() == 0) {
            return 0.33;
        }

        double base =
                (home.getDraws() / (double) home.getGames()
                        + away.getDraws() / (double) away.getGames()) / 2.0;
        // =========================
// GOAL BALANCE IMPACT
// =========================
        double homeGoalsPerGame = home.getGoalsScored() / (double) home.getGames();
        double homeConcededPerGame = home.getGoalsConceded() / (double) home.getGames();

        double awayGoalsPerGame = away.getGoalsScored() / (double) away.getGames();
        double awayConcededPerGame = away.getGoalsConceded() / (double) away.getGames();

// expected goals (enkel modell)
        double expectedHomeGoals = (homeGoalsPerGame + awayConcededPerGame) / 2.0;
        double expectedAwayGoals = (awayGoalsPerGame + homeConcededPerGame) / 2.0;

// jo mer likt, jo høyere draw
        double goalDiff = Math.abs(expectedHomeGoals - expectedAwayGoals);

// juster draw basert på hvor jevnt det ser ut
        double goalBalanceImpact = (1.0 - goalDiff) * 0.25;

// begrens effekten
        if(goalBalanceImpact < -0.2) goalBalanceImpact = -0.2;
        if(goalBalanceImpact > 0.25) goalBalanceImpact = 0.25;

        base += goalBalanceImpact;

        double homeStrength = LeagueStrength.getStrength(home.getName());
        double awayStrength = LeagueStrength.getStrength(away.getName());

        double strengthGap = Math.abs(homeStrength - awayStrength);

// Jo større forskjell, jo mindre sannsynlig med draw
        double leagueImpact = -strengthGap * 0.15;

        base += leagueImpact;


        double homeForm = form.calculateFormScore(home);
        double awayForm = form.calculateFormScore(away);

        double formGap = Math.abs(homeForm - awayForm);
        double formImpact = -formGap * 0.15;

        double tempoGap =
                Math.abs(home.getDNA().getTempoIndex() - away.getDNA().getTempoIndex());

        double dnaImpact = -tempoGap * 0.2;

        double homeInjury = injuryCalc.calculateImpact(home.getProfile());
        double awayInjury = injuryCalc.calculateImpact(away.getProfile());
        double injuryDrawBoost = (homeInjury + awayInjury) * 0.25;

        return clamp(base + formImpact + dnaImpact + injuryDrawBoost);
    }


    // =========================
    // AWAY WIN
    // =========================
    public static double awayWinProbability(TeamStats home, TeamStats away) {

        if (home.getGames() == 0 || away.getGames() == 0) {
            return 0.33;
        }

        double base =
                (away.getWins() / (double) away.getGames()
                        + home.getLosses() / (double) home.getGames()) / 2.0;

        // TEAM-SPECIFIC AWAY STRENGTH
        double awayAwayRate = away.getAwayWins() / (double) Math.max(1, away.getAwayGames());
        double homeHomeLossRate = home.getHomeLosses() / (double) Math.max(1, home.getHomeGames());

        double teamAwayAdvantage = (awayAwayRate + homeHomeLossRate) / 2.0;

        base = base * (0.7 + teamAwayAdvantage);

            // LEAGUE STRENGTH
            double homeStrength = LeagueStrength.getStrength(home.getName());
            double awayStrength = LeagueStrength.getStrength(away.getName());

            base = base * homeStrength / awayStrength;


            double homeForm = form.calculateFormScore(home);
        double awayForm = form.calculateFormScore(away);
        double formImpact = (awayForm - homeForm) * 0.25;

        double dnaImpact =
                (away.getDNA().getAttackIndex() - home.getDNA().getDefenseIndex()) * 0.2
                        + (away.getDNA().getTempoIndex() - home.getDNA().getTempoIndex()) * 0.1;

        double homeInjury = injuryCalc.calculateImpact(home.getProfile());
        double awayInjury = injuryCalc.calculateImpact(away.getProfile());
        double injuryImpact = homeInjury - awayInjury;

        double playerImpact = calculatePlayerImpact(away, home);

        return clamp(base + formImpact + dnaImpact + injuryImpact + playerImpact);
    }


    // =========================
    // PLAYER IMPACT CORE
    // =========================
    private static double calculatePlayerImpact(TeamStats teamA, TeamStats teamB){

        try {

            int idA = TeamIdMapper.getId(teamA.getName());
            int idB = TeamIdMapper.getId(teamB.getName());

            String jsonA = FootballDataPlayerClient.getTeamPlayers(idA);
            String jsonB = FootballDataPlayerClient.getTeamPlayers(idB);

            List<Player> squadA = PlayerParser.parsePlayers(jsonA);
            List<Player> squadB = PlayerParser.parsePlayers(jsonB);

            StartingLineupSelector selector = new StartingLineupSelector();

            List<Player> xiA = selector.pickStartingXI(squadA);
            List<Player> xiB = selector.pickStartingXI(squadB);

            double impactA = playerCalc.calculateStartingXIImpact(xiA);
            double impactB = playerCalc.calculateStartingXIImpact(xiB);

            return (impactA - impactB) * 0.015;

        } catch (Exception e){
            return 0;
        }
    }



    // =========================
    // CLAMP
    // =========================
    private static double clamp(double v){
        if(v < 0.05) return 0.05;
        if(v > 0.9) return 0.9;
        return v;
    }
}
