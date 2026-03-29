package no.dittnavn.footy.analysis.data;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class FootballApiClient {

    private static final String API_KEY = "2a9829b168384d079fbf5d043a7ec649";

    public String fetchMatches(){

        try{

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.football-data.org/v4/matches"))
                    .header("X-Auth-Token", API_KEY)
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            return response.body();

        } catch(Exception e){
            e.printStackTrace();
        }

        return null;
    }
}
