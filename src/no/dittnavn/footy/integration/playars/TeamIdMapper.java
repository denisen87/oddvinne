package no.dittnavn.footy.integration.playars;

import java.util.HashMap;
import java.util.Map;

public class TeamIdMapper {

    private static final Map<String, Integer> map = new HashMap<>();

    static {

        // LA LIGA
        map.put("real madrid", 86);
        map.put("barcelona", 81);
        map.put("mallorca", 399);
        map.put("elche", 278);
        map.put("real betis", 90);
        map.put("atletico madrid", 78);
        map.put("sevilla", 559);
        map.put("valencia", 95);

        // PREMIER LEAGUE
        map.put("manchester city", 65);
        map.put("arsenal", 57);
        map.put("liverpool", 64);
        map.put("chelsea", 61);
        map.put("manchester united", 66);

        map.put("benfica", 1903);
        map.put("porto", 503);
        map.put("sporting", 498);
        map.put("braga", 5613);

        map.put("psg", 524);
        map.put("marseille", 516);
        map.put("monaco", 548);
        map.put("lyon", 523);
        map.put("lille", 521);
        map.put("nice", 522);
        map.put("lens", 546);
        map.put("rennes", 529);


    }

    public static int getId(String teamName){

        if(teamName == null) return -1;

        String key = teamName.toLowerCase().trim();

        // normalisering
        key = key.replace("cf", "")
                .replace("fc", "")
                .replace(".", "")
                .trim();

        return map.getOrDefault(key, -1);
    }
}
