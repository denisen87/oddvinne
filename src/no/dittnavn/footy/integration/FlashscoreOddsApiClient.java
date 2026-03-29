package no.dittnavn.footy.integration;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class FlashscoreOddsApiClient {

    public static String[] getOdds(String matchId) {

        try {

            String urlStr =
                    "https://d.flashscore.com/x/feed/odds_1x2/" + matchId;

            URL url = new URL(urlStr);
            HttpURLConnection conn =
                    (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");

            // 🔥 KRITISKE HEADERS
            conn.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
            conn.setRequestProperty("Accept", "*/*");
            conn.setRequestProperty("X-Fsign", "SW9D1eZo");
            conn.setRequestProperty("Referer",
                    "https://www.flashscore.com/");

            BufferedReader in =
                    new BufferedReader(
                            new InputStreamReader(conn.getInputStream()));

            String inputLine;
            StringBuilder content = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }

            in.close();

            String response = content.toString();

            // DEBUG
            if(response.length() < 50){
                System.out.println("⚠️ API response tom/for kort");
                return new String[]{"","",""};
            }

            // Flashscore feed parsing
            String[] parts = response.split("\\|");

            String home = "";
            String draw = "";
            String away = "";

            int found = 0;

            for(String p : parts){

                if(p.matches("\\d+\\.\\d+")){

                    if(found == 0) home = p;
                    else if(found == 1) draw = p;
                    else if(found == 2){
                        away = p;
                        break;
                    }

                    found++;
                }
            }

            return new String[]{home, draw, away};

        } catch (Exception e) {
            System.out.println("⚠️ Odds API feil: " + e.getMessage());
            return new String[]{"","",""};
        }
    }
}