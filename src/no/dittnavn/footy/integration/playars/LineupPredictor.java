package no.dittnavn.footy.analysis.players;

import no.dittnavn.footy.model.Player;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class LineupPredictor {

    public List<Player> predictStartingXI(List<Player> squad){

        return squad.stream()
                .sorted(Comparator.comparingDouble(Player::impactScore).reversed())
                .limit(11)
                .collect(Collectors.toList());
    }

    public double calculateLineupStrength(List<Player> squad){

        List<Player> xi = predictStartingXI(squad);

        double sum = 0;

        for(Player p : xi){
            sum += p.impactScore();
        }

        return sum / 11.0;
    }
}
