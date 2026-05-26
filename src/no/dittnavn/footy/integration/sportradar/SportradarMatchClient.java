package no.dittnavn.footy.integration.sportradar;

public class SportradarMatchClient {

    private final SportradarClient client =
            new SportradarClient();

    public String fetchMatchDetails(int matchId) {

        return client.fetch(
                "match_details/" + matchId
        );
    }
}