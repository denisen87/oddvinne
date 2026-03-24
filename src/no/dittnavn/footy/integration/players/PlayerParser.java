package no.dittnavn.footy.integration.players;

import no.dittnavn.footy.model.Player;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PlayerParser {

    public static List<Player> parsePlayers(String json) {

        List<Player> players = new ArrayList<>();

        JSONObject obj = new JSONObject(json);
/*
        if (!obj.has("squad")) {
            System.out.println("⚠️ Ingen spillerdata (API limit eller feil API-respons)");
            return players;
        }
        */


        JSONArray squad = obj.getJSONArray("squad");

        for (int i = 0; i < squad.length(); i++) {

            JSONObject p = squad.getJSONObject(i);

            String name = p.optString("name", "UNKNOWN");
            String position = p.optString("position", "UNKNOWN");

            Player player = new Player(
                    name,
                    position,
                    6.5,
                    0,
                    0,
                    900,
                    false
            );

            players.add(player);
        }

        return players;
    }
}
