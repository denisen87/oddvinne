package no.dittnavn.footy.integration.sportradar;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import no.dittnavn.footy.model.Match;

public class SportradarMatchParser {

    public Match parse(String json) {

        try {

            ObjectMapper mapper =
                    new ObjectMapper();

            JsonNode root =
                    mapper.readTree(json);

            JsonNode data =
                    root.get("doc")
                            .get(0)
                            .get("data");

            Match match =
                    new Match();

            String homeTeam =
                    data.get("teams")
                            .get("home")
                            .asText();

            String awayTeam =
                    data.get("teams")
                            .get("away")
                            .asText();

            match.setHomeTeam(homeTeam);
            match.setAwayTeam(awayTeam);

            JsonNode values =
                    data.get("values");

            setStat(match, values, "110", "possession");
            setStat(match, values, "goalattempts", "shots");
            setStat(match, values, "125", "shots_target");
            setStat(match, values, "126", "shots_off");
            setStat(match, values, "171", "blocked");
            setStat(match, values, "124", "corners");
            setStat(match, values, "123", "offsides");
            setStat(match, values, "120", "fouls");
            setStat(match, values, "122", "throwins");
            setStat(match, values, "40", "yellow");
            setStat(match, values, "45", "yellowred");
            setStat(match, values, "50", "red");
            setStat(match, values, "60", "subs");
            setStat(match, values, "1126", "attacks");
            setStat(match, values, "1029", "dangerous");
            setStat(match, values, "161", "penalties");
            setStat(match, values, "127", "saves");
            setStat(match, values, "158", "injuries");

            return match;

        } catch (Exception e) {

            e.printStackTrace();
        }

        return null;
    }

    private void setStat(
            Match match,
            JsonNode values,
            String key,
            String type
    ) {

        JsonNode node =
                values.get(key);

        if (node == null) {
            return;
        }

        int home =
                node.get("value")
                        .get("home")
                        .asInt();

        int away =
                node.get("value")
                        .get("away")
                        .asInt();

        switch (type) {

            case "shots" -> {
                match.setHomeShots(home);
                match.setAwayShots(away);
            }

            case "shots_target" -> {
                match.setHomeShotsTarget(home);
                match.setAwayShotsTarget(away);
            }

            case "corners" -> {
                match.setHomeCorners(home);
                match.setAwayCorners(away);
            }

            case "yellow" -> {
                match.setHomeYellow(home);
                match.setAwayYellow(away);
            }

            case "fouls" -> {
                match.setHomeFouls(home);
                match.setAwayFouls(away);
            }

            case "possession" -> {
                match.setHomePossession(home);
                match.setAwayPossession(away);
            }

            case "dangerous" -> {
                match.setHomeDangerous(home);
                match.setAwayDangerous(away);
            }
        }
    }
}