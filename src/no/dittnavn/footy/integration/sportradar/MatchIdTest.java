package no.dittnavn.footy.integration.sportradar;

import no.dittnavn.footy.db.DatabaseManager;
import no.dittnavn.footy.model.Match;

import java.sql.Connection;
import java.util.Set;

public class MatchIdTest {

    public static void main(String[] args)
            throws Exception {

        String url =
                "https://www.norsk-tipping.no/sport/oddsen";

        // ===== EXTRACT IDS =====

        SportradarMatchIdExtractor extractor =
                new SportradarMatchIdExtractor();

        Set<Integer> ids =
                extractor.extractMatchIds(url);

        // ===== CLIENT + PARSER =====

        SportradarMatchClient client =
                new SportradarMatchClient();

        SportradarMatchParser parser =
                new SportradarMatchParser();

        Connection conn =
                DatabaseManager.connect();

        // ===== LOOP IDS =====

        for (Integer id : ids) {

            System.out.println(
                    "FETCHING: " + id
            );

            try {

                String json =
                        client.fetchMatchDetails(id);

                Match match =
                        parser.parse(json);

                if (match == null) {

                    System.out.println(
                            "Match null"
                    );

                    continue;
                }

                boolean exists =
                        DatabaseManager.matchExists(
                                conn,
                                match.getHomeTeam(),
                                match.getAwayTeam(),
                                match.getDate()
                        );

                if (exists) {

                    System.out.println(
                            "Finnes allerede: " +
                                    match.getHomeTeam() +
                                    " vs " +
                                    match.getAwayTeam()
                    );

                    continue;
                }

                DatabaseManager.saveHistoricalMatch(
                        conn,
                        match
                );

                System.out.println(
                        "Saved: " +
                                match.getHomeTeam() +
                                " vs " +
                                match.getAwayTeam()
                );

            } catch (Exception e) {

                System.out.println(
                        "Feil på id: " + id
                );

                e.printStackTrace();
            }
        }

        conn.close();

        System.out.println(
                "\nFERDIG!"
        );
    }
}