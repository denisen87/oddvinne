package no.dittnavn.footy.integration.playars;

import java.util.*;
import no.dittnavn.footy.model.Player;

public class PlayerRepository {

    private Map<String, List<Player>> teamPlayers = new HashMap<>();

    public void addPlayer(String team, Player player){
        teamPlayers.computeIfAbsent(team, k -> new ArrayList<>()).add(player);
    }

    public List<Player> getPlayers(String team){
        return teamPlayers.getOrDefault(team, new ArrayList<>());
    }
}
