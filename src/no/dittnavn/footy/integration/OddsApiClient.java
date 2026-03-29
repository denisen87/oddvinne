package no.dittnavn.footy.integration;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStreamReader;
import java.io.BufferedReader;

import org.json.JSONArray;
import org.json.JSONObject;

public class OddsApiClient {

    private static final String API_KEY = "9f793711c51611e0f4b6ab513bad1a83";

    public static double[] get1x2Odds(String home, String away) {

        double[] result = {0.0, 0.0, 0.0};

        try {

            String league = "soccer_efl_champ"; // Championship
            String urlStr =
                    "https://api.the-odds-api.com/v4/sports/"
                            + league
                            + "/odds/?apiKey=" + API_KEY
                            + "&regions=uk"
                            + "&markets=h2h"
                            + "&oddsFormat=decimal";

            URL url = new URL(urlStr);
            HttpURLConnection conn =
                    (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");

            BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(conn.getInputStream())
                    );

            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            reader.close();

            JSONArray games = new JSONArray(response.toString());

            for (int i = 0; i < games.length(); i++) {

                JSONObject game = games.getJSONObject(i);

                String homeTeam = game.getString("home_team");
                String awayTeam = game.getString("away_team");

                // DEBUG
                System.out.println("API kamp: " + homeTeam + " vs " + awayTeam);
                System.out.println("Matcher mot: " + home + " vs " + away);

                if (homeTeam.toLowerCase().contains(home.toLowerCase())
                        && awayTeam.toLowerCase().contains(away.toLowerCase())) {

                    JSONArray bookmakers =
                            game.getJSONArray("bookmakers");

                    JSONObject bookmaker =
                            bookmakers.getJSONObject(0);

                    JSONObject market =
                            bookmaker.getJSONArray("markets")
                                    .getJSONObject(0);

                    JSONArray outcomes =
                            market.getJSONArray("outcomes");

                    for (int j = 0; j < outcomes.length(); j++) {

                        JSONObject outcome =
                                outcomes.getJSONObject(j);

                        String name =
                                outcome.getString("name");

                        double price =
                                outcome.getDouble("price");

                        if (name.equalsIgnoreCase(home))
                            result[0] = price;
                        else if (name.equalsIgnoreCase("Draw"))
                            result[1] = price;
                        else if (name.equalsIgnoreCase(away))
                            result[2] = price;
                    }

                    break;
                }
            }

        } catch (Exception e) {
            System.out.println("⚠️ Odds API feil");
        }

        return result;
    }
}