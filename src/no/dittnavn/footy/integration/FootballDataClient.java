package no.dittnavn.footy.integration;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.IOException;


public class FootballDataClient {

    private static long lastApiCall = 0;

    private static void cooldown() throws InterruptedException {
        long now = System.currentTimeMillis();
        long diff = now - lastApiCall;

        if (diff < 2000) {
            Thread.sleep(2000 - diff);
        }

        lastApiCall = System.currentTimeMillis();
    }

    private static final String API_KEY = "2a9829b168384d079fbf5d043a7ec649";

    // 🔥 Felles metode for å hente kamper fra hvilken som helst liga
    private static String fetch(String endpoint) {

        try {
            cooldown();
            URL url = new URL(endpoint);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("X-Auth-Token", API_KEY);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream())
            );

            String inputLine;
            StringBuilder content = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }

            in.close();
            conn.disconnect();

            return content.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getPremierLeagueMatches() throws IOException {
        return fetchMatchesFromCompetition("PL");
    }

    public static String getLaLigaMatches() throws IOException {
        return fetchMatchesFromCompetition("PD");
    }

    public static String getBundesligaMatches() throws IOException {
        return fetchMatchesFromCompetition("BL1");
    }

    public static String getSerieAMatches() throws IOException {
        return fetchMatchesFromCompetition("SA");
    }

    public static String getLigue1Matches() throws IOException {
        return fetchMatchesFromCompetition("FL1");
    }

    public static String getPrimeiraLigaMatches() throws IOException {
        return fetchMatchesFromCompetition("PPL");
    }

    public static String getChampionshipMatches() throws IOException {
        return fetchMatchesFromCompetition("ELC");
    }

    public static String getEredivisieMatches() throws IOException {
        return fetchMatchesFromCompetition("DED");
    }


    // gammel – live sesong
    public static String fetchMatchesFromCompetition(String code) throws IOException {
        String url = "https://api.football-data.org/v4/competitions/"
                + code
                + "/matches?status=FINISHED";

        return fetch(url);
    }


    // ny – velg sesong selv
    public static String fetchMatchesFromCompetition(String code, int season) throws IOException {
        String url = "https://api.football-data.org/v4/competitions/"
                + code
                + "/matches?season="
                + season
                + "&status=FINISHED";

        return fetch(url);
    }

    public static String fetchMatchesFromCompetitionFromDate(String code, String date) throws IOException {

        String today = java.time.LocalDate.now().toString();

        String url = "https://api.football-data.org/v4/competitions/"
                + code
                + "/matches?dateFrom="
                + date
                + "&dateTo="
                + today
                + "&status=FINISHED";

        return fetch(url);
    }

    // 🔥 NY – hent kommende kamper (fixtures)
    public static String fetchUpcomingMatches2(String code) throws IOException {

        String today = java.time.LocalDate.now().toString();
        String tomorrow = java.time.LocalDate.now().plusDays(7).toString();

        String url = "https://api.football-data.org/v4/competitions/"
                + code
                + "/matches?dateFrom="
                + today
                + "&dateTo="
                + tomorrow
                + "&status=SCHEDULED";

        return fetch(url);
    }

    public static String fetchUpcomingMatches(String code) throws IOException {
        String url = "https://api.football-data.org/v4/competitions/"
                + code
                + "/matches?status=SCHEDULED";

        return fetch(url);
    }

}
