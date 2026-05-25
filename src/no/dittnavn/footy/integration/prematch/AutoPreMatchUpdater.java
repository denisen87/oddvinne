package no.dittnavn.footy.integration.prematch;

import no.dittnavn.footy.integration.FlashscoreFixtureFetcher;
import no.dittnavn.footy.integration.FlashscoreFixtureFetcher.Fixture;
import no.dittnavn.footy.integration.FlashscoreOddsApiClient;

import java.util.List;

public class AutoPreMatchUpdater {

    public static void updateLeague(
            String leagueCode,
            List<String> leagues
    ) {

        List<Fixture> fixtures =
                FlashscoreFixtureFetcher
                        .fetchFixtures(leagues);



        for (Fixture f : fixtures) {

            System.out.println(
                    "MATCH ID: " + f.matchId
            );

            try {

                String[] odds =
                        FlashscoreOddsApiClient
                                .getOddsHtml(f.matchId);

                if(odds == null || odds.length < 3){

                    System.out.println(
                            "Ingen odds funnet for: "
                                    + f.homeTeam
                                    + " vs "
                                    + f.awayTeam
                    );

                    continue;
                }

                if(odds[0].equals("-")
                        || odds[1].equals("-")
                        || odds[2].equals("-")){

                    continue;
                }

                f.oddsHome = odds[0];
                f.oddsDraw = odds[1];
                f.oddsAway = odds[2];

                String filePath =
                        "data/" + leagueCode + "preflash.csv";

                boolean exists =
                        AutoPreMatchUpdater.matchExists(
                                filePath,
                                f.date,
                                f.homeTeam,
                                f.awayTeam
                        );

                if (exists) {

                    System.out.println(
                            "Finnes allerede: "
                                    + f.homeTeam
                                    + " vs "
                                    + f.awayTeam
                    );

                    continue;
                }

                PreMatchCsvSaver.saveFixture(
                        leagueCode,
                        f,
                        f.oddsHome,
                        f.oddsDraw,
                        f.oddsAway
                );

                System.out.println(
                        "Lagret: "
                                + f.homeTeam
                                + " vs "
                                + f.awayTeam
                );

            } catch (Exception e) {

                e.printStackTrace();
            }
        }

    }

    public static boolean matchExists(
            String filePath,
            String date,
            String home,
            String away
    ) {

        try(

                java.io.BufferedReader br =
                        new java.io.BufferedReader(
                                new java.io.FileReader(filePath)
                        )

        ){

            String line;

            while((line = br.readLine()) != null){

                String key =
                        date + "," + home + "," + away;

                if(line.startsWith(key)){

                    return true;
                }
            }

        } catch(Exception e){

            e.printStackTrace();
        }

        return false;
    }

}