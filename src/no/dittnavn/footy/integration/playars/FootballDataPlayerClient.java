package no.dittnavn.footy.integration.playars;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class FootballDataPlayerClient {

    private static final String API_KEY = "2a9829b168384d079fbf5d043a7ec649";

    public static String getTeamPlayers(int teamId) {

        try {

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.football-data.org/v4/teams/" + teamId))
                    .header("X-Auth-Token", API_KEY)
                    .GET()
                    .build();

            HttpClient client = HttpClient.newHttpClient();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            return response.body();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }



}
