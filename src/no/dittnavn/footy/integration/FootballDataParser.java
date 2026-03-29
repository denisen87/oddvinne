package no.dittnavn.footy.integration;

import no.dittnavn.footy.model.Match;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FootballDataParser {

    public static List<Match> parseMatches(String json) {

        if (json == null || json.isBlank()) {
            System.out.println("⚠️ Ingen data fra API (rate limit / feil).");
            return new ArrayList<>();
        }

        List<Match> matches = new ArrayList<>();

        JSONObject root = new JSONObject(json);
        JSONArray arr = root.getJSONArray("matches");

        for (int i = 0; i < arr.length(); i++) {

            JSONObject m = arr.getJSONObject(i);

            if (!m.getString("status").equals("FINISHED")) {
                continue;
            }

            int matchId = m.getInt("id");

            String homeName = m.getJSONObject("homeTeam")
                    .getString("name");

            String awayName = m.getJSONObject("awayTeam")
                    .getString("name");

            Integer homeGoals = null;
            Integer awayGoals = null;

            if (m.has("score")) {
                JSONObject score = m.getJSONObject("score");

                if (score.has("fullTime")) {
                    JSONObject ft = score.getJSONObject("fullTime");

                    if (!ft.isNull("home")) homeGoals = ft.getInt("home");
                    if (!ft.isNull("away")) awayGoals = ft.getInt("away");
                }
            }

            String id = String.valueOf(matchId);

            matches.add(
                    new Match(id, homeName, awayName, homeGoals, awayGoals)
            );
        }

        return matches;
    }

    // 🔥 NY – kommende kamper uten resultat
    public static List<Match> parseUpcomingMatches(String json) {

        List<Match> matches = new ArrayList<>();

        if (json == null || json.isBlank()) {
            return matches;
        }

        JSONObject root = new JSONObject(json);
        JSONArray arr = root.getJSONArray("matches");

        for (int i = 0; i < arr.length(); i++) {

            JSONObject m = arr.getJSONObject(i);

            String status = m.getString("status");

            if (!status.equals("SCHEDULED") && !status.equals("TIMED")) {
                continue;
            }

            int matchId = m.getInt("id");

            String homeName = m.getJSONObject("homeTeam")
                    .getString("name");

            String awayName = m.getJSONObject("awayTeam")
                    .getString("name");

            // ingen mål enda
            matches.add(
                    new Match(String.valueOf(matchId), homeName, awayName, -1, -1)
            );
        }

        return matches;
    }
}