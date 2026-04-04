

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

                line = line.replace("\"", "");

                if (line.trim().isEmpty()) continue;

                String delimiter;


                if (line.contains("\t")) {
                    delimiter = "\t";
                } else if (line.contains(";")) {
                    delimiter = ";";
                } else {
                    delimiter = ",";
                }

                line = line.replace("\"", "");
                String[] parts = line.split(delimiter, -1);

                if (parts.length < 10) continue;

                String dateStr;
                String home;
                String away;

                // 🔥 Norway/Argentina/Brazil format
                if (parts[0].equalsIgnoreCase("Norway")) {
                    dateStr = parts[3].replace(".", "/");
                    home = parts[5];
                    away = parts[6];
                } else {
                    dateStr = parts[1];
                    home = parts[3];
                    away = parts[4];
                }

                if (dateStr == null || dateStr.isBlank() ||
                        home == null || home.isBlank() ||
                        away == null || away.isBlank()) {
                    continue;
                }

                double homeOdds = 0;
                double drawOdds = 0;
                double awayOdds = 0;

                // 🔥 odds ligger lengre ut i Norway-filer

                try {
                    // 🔥 sørg for at vi har nok kolonner
                    if (parts.length > 12) {

                        double h1 = parseOdds(parts[10]);
                        double d1 = parseOdds(parts[11]);
                        double a1 = parseOdds(parts[12]);

                        double h2 = parts.length > 15 ? parseOdds(parts[13]) : 0;
                        double d2 = parts.length > 15 ? parseOdds(parts[14]) : 0;
                        double a2 = parts.length > 15 ? parseOdds(parts[15]) : 0;

                        double h3 = parts.length > 18 ? parseOdds(parts[16]) : 0;
                        double d3 = parts.length > 18 ? parseOdds(parts[17]) : 0;
                        double a3 = parts.length > 18 ? parseOdds(parts[18]) : 0;

                        // 🔥 ta beste odds som faktisk er gyldige
                        homeOdds = Math.max(h1, Math.max(h2, h3));
                        drawOdds = Math.max(d1, Math.max(d2, d3));
                        awayOdds = Math.max(a1, Math.max(a2, a3));
                    }

                    // 🔥 debug hvis fortsatt feil
                    if (homeOdds <= 1.01 || drawOdds <= 1.01 || awayOdds <= 1.01) {
                        System.out.println("ODDS FAIL PARSE -> " + line);
                    }

                } catch (Exception ignored) {}


                LocalDate matchDate = LocalDate.parse(dateStr, formatter);

                String id =
                        dateStr + "_" +
                                home.trim().toLowerCase() + "_" +
                                away.trim().toLowerCase();

                Match m = new Match(id, home, away, 0, 0);

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