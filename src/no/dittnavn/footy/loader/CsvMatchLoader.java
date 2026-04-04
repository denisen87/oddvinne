package no.dittnavn.footy.loader;

import no.dittnavn.footy.model.Match;
import no.dittnavn.footy.util.TeamNameNormalizer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class CsvMatchLoader {

    public static List<Match> load(String path) {

        List<Match> matches = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {

            String line = br.readLine(); // skip header

            while ((line = br.readLine()) != null) {

                if (line.trim().isEmpty()) continue;

                String delimiter = line.contains(";") ? ";" : ",";
                String[] cols = line.split(delimiter, -1);

                if (cols.length < 9) {
                    System.out.println("FOR KORT RAD -> " + line);
                    continue;
                }

                try {

                    String date = cols[3].trim();

                    String home = TeamNameNormalizer.normalize(cols[5]);
                    String away = TeamNameNormalizer.normalize(cols[6]);

                    if (cols[7].isEmpty() || cols[8].isEmpty()) continue;

                    int homeGoals;
                    int awayGoals;

                    if (delimiter.equals(";")) { // Norway
                        if (cols.length < 9) continue;

                        if (cols[7].isEmpty() || cols[8].isEmpty()) continue;

                        homeGoals = safeInt(cols[7]);
                        awayGoals = safeInt(cols[8]);

                    } else { // EU
                        if (cols.length < 7) continue;

                        if (cols[5].isEmpty() || cols[6].isEmpty()) continue;

                        homeGoals = safeInt(cols[5]);
                        awayGoals = safeInt(cols[6]);
                    }

                    String id =
                            date + "_" +
                                    home + "_" +
                                    away;

                    matches.add(new Match(id, home, away, homeGoals, awayGoals));

                } catch (Exception parseError) {

                    System.out.println("⚠️ Kunne ikke parse linje:");
                    System.out.println(line);

                }

            }

        } catch (Exception e) {

            System.out.println("CSV load error: " + e.getMessage());

        }

        System.out.println("Totale kamper lest fra CSV: " + matches.size());

        return matches;
    }

    public static List<Match> loadUpcoming(String path) {

        List<Match> matches = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {

            String line = br.readLine(); // skip header

            while ((line = br.readLine()) != null) {

                if (line.trim().isEmpty()) continue;

                String delimiter = line.contains(";") ? ";" : ",";
                String[] cols = line.split(delimiter, -1);

                if (cols.length < 7) continue;

                try {

                    String date = cols[1].trim();

                    String home = TeamNameNormalizer.normalize(cols[3]);
                    String away = TeamNameNormalizer.normalize(cols[4]);

                    // upcoming = ingen mål
                    if (!cols[5].isEmpty() || !cols[6].isEmpty()) continue;

                    String id =
                            date + "_" +
                                    home + "_" +
                                    away;

                    matches.add(new Match(id, home, away, 0, 0));

                } catch (Exception e) {
                    System.out.println("Row parse error: " + e.getMessage() + " | " + line);
                }
            }

        } catch (Exception e) {
            System.out.println("CSV upcoming load error: " + e.getMessage());
        }

        return matches;
    }
    private static int safeInt(String s) {
        try {
            if (s == null || s.trim().isEmpty()) return 0;
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return 0;
        }
    }
}