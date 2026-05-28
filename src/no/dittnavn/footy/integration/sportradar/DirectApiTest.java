package no.dittnavn.footy.integration.sportradar;

public class DirectApiTest {

    public static void main(String[] args) {

        SportradarClient client =
                new SportradarClient();

        String json =
                client.fetch(
                        "stats_match_get/61732292"
                );

        System.out.println(json);
    }
}