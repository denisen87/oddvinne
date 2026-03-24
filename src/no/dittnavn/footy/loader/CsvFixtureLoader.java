package no.dittnavn.footy.loader;

import no.dittnavn.footy.model.Match;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


public class CsvFixtureLoader {

    public static List<Match> load(String path) {

        List<Match> matches = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {

            String line = br.readLine(); // skip header

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            while ((line = br.readLine()) != null) {

                String delimiter = line.contains(";") ? ";" : ",";
                String[] cols = line.split(delimiter, -1);

                if (cols.length < 4) continue;

                String div  = cols[0];
                String dateStr;
                String home;
                String away;

                if (cols[0].equalsIgnoreCase("Norway")) {
                    dateStr = cols[3].replace(".", "/");
                    home = cols[5];
                    away = cols[6];
                } else {
                    dateStr = cols[1];
                    home = cols[2];
                    away = cols[3];
                }

                if (dateStr == null || dateStr.isBlank() ||
                        home == null || home.isBlank() ||
                        away == null || away.isBlank()) {

                    continue; // 🔥 SKIPPER søppelrad
                }

                double homeOdds = 0;
                double drawOdds = 0;
                double awayOdds = 0;

                // hvis odds finnes i fila – bruk dem
                if (cols.length >= 7) {
                    try {
                        if (!cols[4].isBlank())
                            homeOdds = Double.parseDouble(cols[4].replace(",", "."));

                        if (!cols[5].isBlank())
                            drawOdds = Double.parseDouble(cols[5].replace(",", "."));

                        if (!cols[6].isBlank())
                            awayOdds = Double.parseDouble(cols[6].replace(",", "."));
                    } catch (Exception ignored) {}
                }

                LocalDate matchDate = LocalDate.parse(dateStr, formatter);


                String id =
                        dateStr + "_" +
                                home.trim().toLowerCase() + "_" +
                                away.trim().toLowerCase();


                Match m = new Match(id, home, away, 0, 0);

                // legg odds hvis de finnes
                m.setHomeOdds(homeOdds);
                m.setDrawOdds(drawOdds);
                m.setAwayOdds(awayOdds);

                matches.add(m);
            }

        } catch (Exception e) {
            System.out.println("CSV FIXTURE load error: " + path + " (" + e.getMessage() + ")");
        }

        return matches;
    }

    private static double parseOdds(String s) {
        try {
            if (s == null || s.trim().isEmpty()) return 0;
            return Double.parseDouble(s.replace(",", "."));
        } catch (Exception e) {
            return 0;
        }
    }

}