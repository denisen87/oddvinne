package no.dittnavn.footy.analysis.form;

import no.dittnavn.footy.stats.TeamStats;
import no.dittnavn.footy.analysis.form.FormCalculator;

public class FormCalculator {


    public double calculateFormScore(TeamStats team){

        int games = team.getGames();
        if(games < 5) return 0.5;

        double winRate = (double) team.getWins() / games;
        double drawRate = (double) team.getDraws() / games;

        return (winRate * 0.7) + (drawRate * 0.3);
    }

    public double attackStrength(TeamStats team){
        if(team.getGames() == 0) return 0.5;
        return (double) team.getGoalsScored() / team.getGames();
    }

    public double defenseStrength(TeamStats team){
        if(team.getGames() == 0) return 0.5;
        return 1.0 - ((double) team.getGoalsConceded() / team.getGames());
    }

    public double momentum(TeamStats team){
        return 0.5;
    }


}
