package no.dittnavn.footy.integration.playars;

import no.dittnavn.footy.model.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class StartingLineupSelector {

    public List<Player> pickStartingXI(List<Player> squad){

        List<Player> available = new ArrayList<>();

        // fjern skadde
        for(Player p : squad){
            if(!p.isInjured()){
                available.add(p);
            }
        }

        // sorter på impactScore (beste først)
        available.sort(Comparator.comparingDouble(Player::impactScore).reversed());

        // velg topp 11
        List<Player> startingXI = new ArrayList<>();

        for(int i = 0; i < available.size() && i < 11; i++){
            startingXI.add(available.get(i));
        }

        return startingXI;
    }
}
