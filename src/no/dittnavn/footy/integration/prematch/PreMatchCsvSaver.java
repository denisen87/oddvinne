package no.dittnavn.footy.integration.prematch;

import no.dittnavn.footy.integration
        .FlashscoreFixtureFetcher.Fixture;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

public class PreMatchCsvSaver {

    public static void saveFixture(
            String leagueCode,
            Fixture fixture,
            String oddsHome,
            String oddsDraw,
            String oddsAway
    ) {

        try {

            String fileName =
                    "data/"
                            + leagueCode
                            + "preflash.csv";

            File file =
                    new File(fileName);

            boolean fileExists =
                    file.exists();

            PrintWriter pw =
                    new PrintWriter(
                            new FileWriter(
                                    file,
                                    true
                            )
                    );

            if(!fileExists){

                pw.println(
                        "date,home,away,odds_home,odds_draw,odds_away"
                );
            }

            pw.println(
                    fixture.date + "," +
                            fixture.homeTeam + "," +
                            fixture.awayTeam + "," +
                            oddsHome + "," +
                            oddsDraw + "," +
                            oddsAway
            );

            pw.close();

        } catch(Exception e){

            e.printStackTrace();
        }
    }
}