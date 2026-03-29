package no.dittnavn.footy.util;

import no.dittnavn.footy.model.Match;

import java.io.File;
import java.io.PrintWriter;
import java.util.List;

public class CsvMatchWriter {

    public static void save(String path, List<Match> matches) {

        try (PrintWriter pw = new PrintWriter(new File(path))) {

            // header – minimum nødvendig kolonner
            pw.println("Date,HomeTeam,AwayTeam,FTHG,FTAG");

            for (Match m : matches) {

                pw.println(
                        m.getDate() + "," +
                                m.getHomeTeam() + "," +
                                m.getAwayTeam() + "," +
                                m.getHomeGoals() + "," +
                                m.getAwayGoals()
                );
            }

        } catch (Exception e) {
            System.out.println("CSV write error: " + e.getMessage());
        }
    }
}