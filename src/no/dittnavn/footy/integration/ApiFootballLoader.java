package no.dittnavn.footy.integration;

import no.dittnavn.footy.model.Match;
import no.dittnavn.footy.util.TeamNameNormalizer;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ApiFootballLoader {

    public static List<Match> loadFromApi(String jsonResponse) {

        List<Match> matches = new ArrayList<>();

        try {

            JSONObject root = new JSONObject(jsonResponse);

            // ⚠️ Avhenger av API – kan være "matches", "response", "data"
            JSONArray games = root.getJSONArray("matches");

            for (int i = 0; i < games.length(); i++) {

                JSONObject game = games.getJSONObject(i);

                // ⚠️ Juster hvis feltene heter noe annet i ditt API
                String date = game.getString("utcDate");

                JSONObject homeObj = game.getJSONObject("homeTeam");
                JSONObject awayObj = game.getJSONObject("awayTeam");

                String home = homeObj.getString("name");
                String away = awayObj.getString("name");

                JSONObject score = game.getJSONObject("score")
                        .getJSONObject("fullTime");

                if (score.isNull("home") || score.isNull("away")) continue;

                int homeGoals = score.getInt("home");
                int awayGoals = score.getInt("away");

                // 🔥 STABIL MATCH-ID
                String id =
                        date + "_" +
                                TeamNameNormalizer.normalize(home).toLowerCase() + "_" +
                                TeamNameNormalizer.normalize(away).toLowerCase();

                matches.add(new Match(id, home, away, homeGoals, awayGoals));
            }

        } catch (Exception e) {
            System.out.println("API Football error: " + e.getMessage());
        }

        return matches;
    }
}