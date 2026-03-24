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

                String[] cols = line.split(",", -1); // viktig!

                if (cols.length < 7) continue;

                try {

                    String date = cols[1].trim();

                    String home = TeamNameNormalizer.normalize(cols[3]);
                    String away = TeamNameNormalizer.normalize(cols[4]);

                    String homeGoalsStr = cols[5].trim();
                    String awayGoalsStr = cols[6].trim();

                    if (homeGoalsStr.isEmpty() || awayGoalsStr.isEmpty()) continue;

                    int homeGoals = Integer.parseInt(homeGoalsStr);
                    int awayGoals = Integer.parseInt(awayGoalsStr);

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

                String[] cols = line.split(",", -1);

                if (cols.length < 7) continue;

                String date = cols[1].trim();

                String home = TeamNameNormalizer.normalize(cols[3]);
                String away = TeamNameNormalizer.normalize(cols[4]);

                // upcoming kamp = ingen mål
                if (!cols[5].isEmpty() || !cols[6].isEmpty()) continue;

                String id =
                        date + "_" +
                                home + "_" +
                                away;

                matches.add(new Match(id, home, away, 0, 0));
            }

        } catch (Exception e) {

            System.out.println("CSV upcoming load error: " + e.getMessage());

        }

        System.out.println("Totale upcoming fixtures: " + matches.size());

        return matches;
    }
}