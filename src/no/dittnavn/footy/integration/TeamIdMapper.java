package no.dittnavn.footy.integration;
import no.dittnavn.footy.util.TeamNameNormalizer;

import java.util.HashMap;
import java.util.Map;

public class TeamIdMapper {

    private static final Map<String, Integer> map = new HashMap<>();

    static {

        // La Liga
        map.put("real madrid", 86);
        map.put("barcelona", 81);
        map.put("mallorca", 399);
        map.put("elche", 278);
        map.put("real betis", 90);
        map.put("atletico madrid", 78);
        map.put("sevilla", 559);
        map.put("valencia", 95);

        // Premier League (eksempel)
        map.put("manchester city", 65);
        map.put("arsenal", 57);
        map.put("liverpool", 64);
        map.put("chelsea", 61);
        map.put("manchester united", 66);
    }


    public static int getId(String teamName){

        if(teamName == null) return -1;

        String key = TeamNameNormalizer.normalize(teamName)
                .toLowerCase()
                .replaceAll("\\s*\\(.*?\\)", "")
                .trim()
                .replace(".", "")
                .replace("-", " ")
                .replaceAll("\\s+", " ");

        return map.getOrDefault(key, -1);
    }

}
