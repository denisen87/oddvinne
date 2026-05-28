package no.dittnavn.footy.integration.sportradar;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


import no.dittnavn.footy.db.DatabaseManager;
import no.dittnavn.footy.model.Match;

import java.sql.Connection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class TeamCrawler {


    public static void main(String[] args)
            throws Exception {

        SportradarClient client =
                new SportradarClient();

        Connection conn =
                DatabaseManager.connect();

        // ===== BESØKTE KAMPER =====

        Set<Integer> visited =
                new HashSet<>();

        // ===== KØ =====

        Queue<Integer> queue =
                new LinkedList<>();

        // ===== STARTKAMP =====

        queue.add(61732292);

        // ===== TEAM ID =====

        int torreenseId = 25787;

        while (!queue.isEmpty()) {

            int matchId =
                    queue.poll();

            // ===== DUPLIKATER =====

            if (visited.contains(matchId)) {
                continue;
            }

            visited.add(matchId);

            System.out.println(
                    "HENTER KAMP: " + matchId
            );

            try {

                // ===== MATCH JSON =====

                String matchJson =
                        client.fetch(
                                "stats_match_get/" + matchId
                        );

                // ===== TIMELINE JSON =====

                String timelineJson =
                        client.fetch(
                                "stats_match_timeline/" + matchId
                        );

                String detailsJson =
                        client.fetch(
                                "match_details/" + matchId
                        );



                if (matchJson == null
                        || timelineJson == null
                        || detailsJson == null) {

                    continue;
                }

                // ===== TIMELINE PARSER =====

                TimelineParser parser =
                        new TimelineParser();

                TimelineStats stats =
                        parser.parse(timelineJson);

                // ===== COVERAGE =====

                boolean hasCoverage =
                        stats.getHomeCorners() > 0
                                || stats.getAwayCorners() > 0
                                || stats.getHomeShotsOnTarget() > 0
                                || stats.getAwayShotsOnTarget() > 0
                                || stats.getHomeKeeperSaves() > 0
                                || stats.getAwayKeeperSaves() > 0;

                if (!hasCoverage) {

                    System.out.println(
                            "INGEN COVERAGE"
                    );

                    continue;
                }

                // ===== PARSE MATCH JSON =====

                JsonObject root =
                        JsonParser.parseString(matchJson)
                                .getAsJsonObject();

                JsonObject matchData =
                        root.getAsJsonArray("doc")
                                .get(0)
                                .getAsJsonObject()
                                .getAsJsonObject("data");

                // ===== MATCH =====

                Match match =
                        new Match();

                MatchDetailsParser detailsParser =
                        new MatchDetailsParser();

                detailsParser.apply(match, detailsJson);

                // ===== LEAGUE =====

                String league =
                        matchData.getAsJsonObject("tournament")
                                .get("name")
                                .getAsString();

                match.setLeague(league);

                System.out.println(
                        "LEAGUE: " + league
                );

                // ===== TEAMS =====

                JsonObject teams =
                        matchData.getAsJsonObject("teams");

                String home =
                        teams.getAsJsonObject("home")
                                .get("name")
                                .getAsString();

                String away =
                        teams.getAsJsonObject("away")
                                .get("name")
                                .getAsString();

                match.setHomeTeam(home);
                match.setAwayTeam(away);

                // ===== RESULT =====

                JsonObject result =
                        matchData.getAsJsonObject("result");

                int homeGoals =
                        result.get("home")
                                .getAsInt();

                int awayGoals =
                        result.get("away")
                                .getAsInt();

                match.setHomeGoals(homeGoals);
                match.setAwayGoals(awayGoals);

                // ===== DATE =====

                String date =
                        matchData.getAsJsonObject("time")
                                .get("date")
                                .getAsString();

                String[] parts =
                        date.split("/");

                String formattedDate =
                        "20" + parts[2]
                                + "-"
                                + parts[1]
                                + "-"
                                + parts[0];

                match.setDate(formattedDate);

                // ===== STATS =====

                match.setHomeCorners(
                        stats.getHomeCorners()
                );

                match.setAwayCorners(
                        stats.getAwayCorners()
                );

                match.setHomeShotsTarget(
                        stats.getHomeShotsOnTarget()
                );

                match.setAwayShotsTarget(
                        stats.getAwayShotsOnTarget()
                );

                match.setHomeKeeperSaves(
                        stats.getHomeKeeperSaves()
                );

                match.setAwayKeeperSaves(
                        stats.getAwayKeeperSaves()
                );

                match.setHomeDangerous(
                        stats.getHomeDangerous()
                );

                match.setAwayDangerous(
                        stats.getAwayDangerous()
                );

                match.setHomeYellow(
                        stats.getHomeYellowCards()
                );

                match.setAwayYellow(
                        stats.getAwayYellowCards()
                );

                match.setHomeRed(
                        stats.getHomeRedCards()
                );

                match.setAwayRed(
                        stats.getAwayRedCards()
                );

                match.setHomeShots(
                        stats.getHomeShots()
                );

                match.setAwayShots(
                        stats.getAwayShots()
                );

                match.setHomeFouls(
                        stats.getHomeFouls()
                );

                match.setAwayFouls(
                        stats.getAwayFouls()
                );

                match.setHomeOffsides(
                        stats.getHomeOffsides()
                );

                match.setAwayOffsides(
                        stats.getAwayOffsides()
                );

                match.setHomeGoalKicks(
                        stats.getHomeGoalKicks()
                );

                match.setAwayGoalKicks(
                        stats.getAwayGoalKicks()
                );

                match.setHomeFreeKicks(
                        stats.getHomeFreeKicks()
                );

                match.setAwayFreeKicks(
                        stats.getAwayFreeKicks()
                );

                // ===== DUPLIKATFILTER =====

                boolean exists =
                        DatabaseManager.matchExists(
                                conn,
                                match.getHomeTeam(),
                                match.getAwayTeam(),
                                match.getDate()
                        );

                if (exists) {

                    System.out.println(
                            "FINNES ALLEREDE"
                    );

                    continue;
                }

                // ===== SAVE =====

                DatabaseManager.saveHistoricalMatch(
                        conn,
                        match
                );

                System.out.println(
                        "SAVED TO DATABASE"
                );

                // ===== PRINT =====

                System.out.println(
                        "HOME CORNERS: "
                                + stats.getHomeCorners()
                );

                System.out.println(
                        "AWAY CORNERS: "
                                + stats.getAwayCorners()
                );

                System.out.println(
                        "HOME SHOTS OT: "
                                + stats.getHomeShotsOnTarget()
                );

                System.out.println(
                        "AWAY SHOTS OT: "
                                + stats.getAwayShotsOnTarget()
                );

                System.out.println(
                        "HOME SAVES: "
                                + stats.getHomeKeeperSaves()
                );

                System.out.println(
                        "AWAY SAVES: "
                                + stats.getAwayKeeperSaves()
                );

                System.out.println(
                        home + " vs " + away
                );

                // ===== TEAM HISTORY =====

                JsonObject teamHistory =
                        matchData.getAsJsonObject("teamhistory");

                JsonObject torreenseHistory =
                        teamHistory.getAsJsonObject(
                                String.valueOf(torreenseId)
                        );

                // ===== FORRIGE =====

                JsonElement previousElement =
                        torreenseHistory.get("previous");

                if (previousElement != null
                        && !previousElement.isJsonNull()) {

                    int previous =
                            previousElement.getAsInt();

                    if (!visited.contains(previous)) {

                        queue.add(previous);

                        System.out.println(
                                "FORRIGE: " + previous
                        );
                    }
                }

                // ===== NESTE =====

                JsonElement nextElement =
                        torreenseHistory.get("next");

                if (nextElement != null
                        && !nextElement.isJsonNull()) {

                    int next =
                            nextElement.getAsInt();

                    if (!visited.contains(next)) {

                        queue.add(next);

                        System.out.println(
                                "NESTE: " + next
                        );
                    }
                }

                System.out.println(
                        "----------------------"
                );

            } catch (Exception e) {

                e.printStackTrace();
            }
        }

        System.out.println(
                "TOTALT FUNNET: "
                        + visited.size()
        );
    }


}
