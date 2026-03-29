package no.dittnavn.footy.integration.playars;

import no.dittnavn.footy.model.Player;

import java.util.List;

public class PlayerImpactCalculator {

    public double calculateTeamImpact(List<Player> players){

        double total = 0;

        for(Player p : players){
            total += p.impactScore();
        }

        return total;
    }

    public double calculateStartingXIImpact(List<Player> startingXI){

        double total = 0;

        for(Player p : startingXI){
            total += p.impactScore();
        }

        // startellever skal ha mer vekt enn bredde
        return total * 1.25;
    }
}
