package no.dittnavn.footy.service;

import no.dittnavn.footy.model.Match;
import no.dittnavn.footy.model.Outcome;
import no.dittnavn.footy.stats.StatsIndeks;
import no.dittnavn.footy.stats.TeamStats;
import no.dittnavn.footy.model.TeamOutcome;


public class MatchService {

    private StatsIndeks indeks;

    public MatchService(StatsIndeks indeks){
        this.indeks = indeks;
    }

    // 🔥 Registrer kamp
    public void registerMatch(String homeName, String awayName, int homeGoals, int awayGoals){

        // 1️⃣ hent eller opprett lag
        TeamStats homeStats = indeks.getOrCreate(homeName);
        TeamStats awayStats = indeks.getOrCreate(awayName);

        // goals
        homeStats.goalsScored += homeGoals;
        homeStats.goalsConceded += awayGoals;

        awayStats.goalsScored += awayGoals;
        awayStats.goalsConceded += homeGoals;

        // home stats
        homeStats.homeGames++;

        if(homeGoals > awayGoals){
            homeStats.homeWins++;
            awayStats.awayLosses++;

            homeStats.addResult(TeamOutcome.WIN);
            awayStats.addResult(TeamOutcome.LOSS);

        }else if(homeGoals < awayGoals){
            homeStats.homeLosses++;
            awayStats.awayWins++;

            homeStats.addResult(TeamOutcome.LOSS);
            awayStats.addResult(TeamOutcome.WIN);

        }else{
            homeStats.homeDraws++;
            awayStats.awayDraws++;

            homeStats.addResult(TeamOutcome.DRAW);
            awayStats.addResult(TeamOutcome.DRAW);
        }


        // away games
        awayStats.awayGames++;

        // 2️⃣ opprett match
        String id = "manual_" + homeName + "_" + awayName;

        Match match = new Match(id, homeName, awayName, homeGoals, awayGoals);



        // 3️⃣ oppdater stats
        homeStats.updateFromMatch(match);
        awayStats.updateFromMatch(match);

        // 4️⃣ oppdater rating (ELO)
        updateElo(homeStats, awayStats, homeGoals, awayGoals);
    }

    // ⭐ Elo rating
    private void updateElo(TeamStats home, TeamStats away, int homeGoals, int awayGoals){

        double homeScore;
        double awayScore;

        if(homeGoals > awayGoals){
            homeScore = 1;
            awayScore = 0;
        }
        else if(homeGoals < awayGoals){
            homeScore = 0;
            awayScore = 1;
        }
        else{
            homeScore = 0.5;
            awayScore = 0.5;
        }

        double homeElo = home.getElo();
        double awayElo = away.getElo();

        home.updateElo(awayElo, homeScore);
        away.updateElo(homeElo, awayScore);
    }
}
