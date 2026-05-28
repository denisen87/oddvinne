package no.dittnavn.footy.integration.sportradar;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import no.dittnavn.footy.model.Match;

public class MatchDetailsParser {

    public void apply(Match match, String detailsJson) {

        try {

            JsonObject root =
                    JsonParser.parseString(detailsJson)
                            .getAsJsonObject();

            JsonObject data =
                    root.getAsJsonArray("doc")
                            .get(0)
                            .getAsJsonObject()
                            .getAsJsonObject("data");

            JsonObject values =
                    data.getAsJsonObject("values");

            // ===== POSSESSION =====

            if (values.has("ballsafepercentage")) {

                JsonObject possession =
                        values.getAsJsonObject(
                                "ballsafepercentage"
                        );

                JsonObject value =
                        possession.getAsJsonObject("value");

                match.setHomePossession(
                        value.get("home").getAsInt()
                );

                match.setAwayPossession(
                        value.get("away").getAsInt()
                );
            }

            // ===== DANGEROUS ATTACKS =====

// ===== DANGEROUS ATTACKS =====

            JsonObject dangerous = null;

            if (values.has("1029")) {

                dangerous =
                        values.getAsJsonObject("1029");
            }

            else if (values.has("dangerousattackpercentage")) {

                dangerous =
                        values.getAsJsonObject(
                                "dangerousattackpercentage"
                        );
            }

            if (dangerous != null) {

                JsonObject value =
                        dangerous.getAsJsonObject("value");

                match.setHomeDangerous(
                        value.get("home").getAsInt()
                );

                match.setAwayDangerous(
                        value.get("away").getAsInt()
                );
            }

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}