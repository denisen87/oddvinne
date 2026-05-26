package no.dittnavn.footy.integration.sportradar;

import java.sql.Connection;

import no.dittnavn.footy.db.DatabaseManager;
import no.dittnavn.footy.model.Match;

public class SportradarMatchTest {

    public static void main(String[] args) throws Exception {

        SportradarMatchClient client =
                new SportradarMatchClient();

        String json =
                client.fetchMatchDetails(71297426);
                //client.fetchMatchDetails(71516508); // tolima

        SportradarMatchParser parser =
                new SportradarMatchParser();

        Match match =
                parser.parse(json);

        // test-data
        match.setDate("03/05/2026");

        match.setLeague("Colombia");

        System.out.println(match.getDate());

        System.out.println(match.getLeague());

        System.out.println(match.getHomeTeam());

        System.out.println(match.getAwayTeam());

        Connection conn =
                DatabaseManager.connect();

        // DEBUG
        System.out.println(
                "DATE: " +
                        match.getDate()
        );

        System.out.println(
                "LEAGUE: " +
                        match.getLeague()
        );

        System.out.println(
                "HOME: " +
                        match.getHomeTeam()
        );

        System.out.println(
                "AWAY: " +
                        match.getAwayTeam()
        );

        // SAVE TO DB
        DatabaseManager.saveHistoricalMatch(
                conn,
                match
        );

        // PRINT MATCH STATS
        System.out.println(
                "Shots: " +
                        match.getHomeShots() +
                        " - " +
                        match.getAwayShots()
        );

        System.out.println(
                "Corners: " +
                        match.getHomeCorners() +
                        " - " +
                        match.getAwayCorners()
        );

        System.out.println(
                "Possession: " +
                        match.getHomePossession() +
                        " - " +
                        match.getAwayPossession()
        );

        System.out.println(
                "Dangerous attacks: " +
                        match.getHomeDangerous() +
                        " - " +
                        match.getAwayDangerous()
        );

        System.out.println(
                "Saved to historical_matches"
        );

        conn.close();
    }
}