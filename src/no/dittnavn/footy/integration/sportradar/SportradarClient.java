package no.dittnavn.footy.integration.sportradar;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class SportradarClient {

    private static final String BASE =
            "https://stats.fn.sportradar.com/norsktipping/no/Europe:Berlin/gismo/";

    private final HttpClient client =
            HttpClient.newHttpClient();

    public String fetch(String endpoint) {

        try {

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE + endpoint))
                    .GET()
                    .build();

            HttpResponse<String> response =
                    client.send(request,
                            HttpResponse.BodyHandlers.ofString());

            return response.body();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}