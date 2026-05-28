package no.dittnavn.footy.integration.sportradar;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class TimelineParser {

    public TimelineStats parse(String timelineJson) {

        TimelineStats stats =
                new TimelineStats();

        try {

            JsonObject root =
                    JsonParser.parseString(timelineJson)
                            .getAsJsonObject();

            JsonObject data =
                    root.getAsJsonArray("doc")
                            .get(0)
                            .getAsJsonObject()
                            .getAsJsonObject("data");

            JsonArray events =
                    data.getAsJsonArray("events");

            // ===== COUNTERS =====

            int homeCorners = 0;
            int awayCorners = 0;

            int homeShots = 0;
            int awayShots = 0;

            int homeShotsOnTarget = 0;
            int awayShotsOnTarget = 0;

            int homeKeeperSaves = 0;
            int awayKeeperSaves = 0;

            int homeYellowCards = 0;
            int awayYellowCards = 0;

            int homeRedCards = 0;
            int awayRedCards = 0;

            int homeFouls = 0;
            int awayFouls = 0;

            int homeOffsides = 0;
            int awayOffsides = 0;

            int homeGoalKicks = 0;
            int awayGoalKicks = 0;

            int homeFreeKicks = 0;
            int awayFreeKicks = 0;

            // ===== LOOP EVENTS =====

            for (int i = 0; i < events.size(); i++) {

                JsonObject event =
                        events.get(i)
                                .getAsJsonObject();

                String docType =
                        event.get("_doctype")
                                .getAsString();

                System.out.println(
                        "DOCTYPE: " + docType
                );

                String team = "";

                if (event.has("team")) {

                    team =
                            event.get("team")
                                    .getAsString();
                }

                // ===== CORNERS =====

                if (docType.equals("corner")) {

                    if (team.equals("home")) {
                        homeCorners++;
                    }

                    if (team.equals("away")) {
                        awayCorners++;
                    }
                }

                // ===== TOTAL SHOTS =====

                if (docType.equals("shotontarget")
                        || docType.equals("shotofftarget")
                        || docType.equals("shotblocked")) {

                    if (team.equals("home")) {
                        homeShots++;
                    }

                    if (team.equals("away")) {
                        awayShots++;
                    }
                }

                // ===== SHOTS ON TARGET =====

                if (docType.equals("shotontarget")) {

                    if (team.equals("home")) {
                        homeShotsOnTarget++;
                    }

                    if (team.equals("away")) {
                        awayShotsOnTarget++;
                    }
                }

                // ===== GOALKEEPER SAVES =====

                if (docType.equals("goalkeepersave")) {

                    if (team.equals("home")) {
                        homeKeeperSaves++;
                    }

                    if (team.equals("away")) {
                        awayKeeperSaves++;
                    }
                }

                // ===== CARDS =====

                if (docType.equals("card")) {

                    System.out.println(
                            "CARD EVENT: " + event
                    );

                    String cardType = "";

                    if (event.has("card")) {

                        cardType =
                                event.get("card")
                                        .getAsString();
                    }

                    // ===== YELLOW =====

                    if (cardType.toLowerCase().contains("yellow")) {

                        if (team.equals("home")) {
                            homeYellowCards++;
                        }

                        if (team.equals("away")) {
                            awayYellowCards++;
                        }
                    }

                    // ===== RED =====

                    if (cardType.toLowerCase().contains("red")) {

                        if (team.equals("home")) {
                            homeRedCards++;
                        }

                        if (team.equals("away")) {
                            awayRedCards++;
                        }
                    }
                }

                // ===== FOULS =====
                // Sportradar virker å bruke freekick
                // som indirekte foul-event

                if (docType.equals("freekick")) {

                    if (team.equals("home")) {
                        homeFouls++;
                        homeFreeKicks++;
                    }

                    if (team.equals("away")) {
                        awayFouls++;
                        awayFreeKicks++;
                    }
                }

                // ===== OFFSIDES =====

                if (docType.equals("offside")) {

                    if (team.equals("home")) {
                        homeOffsides++;
                    }

                    if (team.equals("away")) {
                        awayOffsides++;
                    }
                }

                // ===== GOAL KICKS =====

                if (docType.equals("goalkick")) {

                    if (team.equals("home")) {
                        homeGoalKicks++;
                    }

                    if (team.equals("away")) {
                        awayGoalKicks++;
                    }
                }
            }

            // ===== SET STATS =====

            stats.setHomeCorners(homeCorners);
            stats.setAwayCorners(awayCorners);

            stats.setHomeShots(homeShots);
            stats.setAwayShots(awayShots);

            stats.setHomeShotsOnTarget(homeShotsOnTarget);
            stats.setAwayShotsOnTarget(awayShotsOnTarget);

            stats.setHomeKeeperSaves(homeKeeperSaves);
            stats.setAwayKeeperSaves(awayKeeperSaves);

            stats.setHomeYellowCards(homeYellowCards);
            stats.setAwayYellowCards(awayYellowCards);

            stats.setHomeRedCards(homeRedCards);
            stats.setAwayRedCards(awayRedCards);

            stats.setHomeFouls(homeFouls);
            stats.setAwayFouls(awayFouls);

            stats.setHomeOffsides(homeOffsides);
            stats.setAwayOffsides(awayOffsides);

            stats.setHomeGoalKicks(homeGoalKicks);
            stats.setAwayGoalKicks(awayGoalKicks);

            stats.setHomeFreeKicks(homeFreeKicks);
            stats.setAwayFreeKicks(awayFreeKicks);

        } catch (Exception e) {

            e.printStackTrace();
        }

        return stats;
    }
}