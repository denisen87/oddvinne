package no.dittnavn.footy.loader;

import no.dittnavn.footy.model.Match;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CsvHistoricalLoader {

    public static List<Match> load(String path, String league){

        List<Match> matches = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {

            String line = br.readLine(); // skip header

            while ((line = br.readLine()) != null) {

                if (line.trim().isEmpty()) continue;

                String delimiter = line.contains(";") ? ";" : ",";
                String[] parts = line.split(delimiter);

                if (parts.length < 8) {
                    System.out.println("Skipping row (wrong column count): " + line);
                    continue;
                }

                try {

                    String date;
                    String homeTeam;
                    String awayTeam;
                    int homeGoals;
                    int awayGoals;

                    // 🔥 Norsk format
                    if (parts[0].equalsIgnoreCase("Norway")) {

                        date = parts[3].trim().replace(".", "/");
                        homeTeam = parts[5].trim();
                        awayTeam = parts[6].trim();

                        homeGoals = Integer.parseInt(parts[7].trim());
                        awayGoals = Integer.parseInt(parts[8].trim());

                    } else {
                        // 🔵 Standard format

                        date = parts[1].trim();
                        homeTeam = parts[3].trim();
                        awayTeam = parts[4].trim();

                        homeGoals = Integer.parseInt(parts[5].trim());
                        awayGoals = Integer.parseInt(parts[6].trim());
                    }

                    // 🔥 Odds parsing (robust)
                    List<Double> oddsValues = new ArrayList<>();

                    for (int i = parts.length - 1; i >= 0; i--) {
                        if (isNumeric(parts[i])) {
                            oddsValues.add(0, Double.parseDouble(parts[i].trim().replace(",", ".")));
                        } else {
                            break;
                        }
                    }

                    if (oddsValues.size() < 3) {
                        System.out.println("Skipping row (not enough odds): " + line);
                        continue;
                    }

                    double homeOdds = oddsValues.get(0);
                    double drawOdds = oddsValues.get(1);
                    double awayOdds = oddsValues.get(2);

                    Match match = new Match(
                            date,
                            homeTeam,
                            awayTeam,
                            homeGoals,
                            awayGoals
                    );

                    match.setLeague(league);

                    match.setHomeOdds(homeOdds);
                    match.setDrawOdds(drawOdds);
                    match.setAwayOdds(awayOdds);

                    matches.add(match);

                } catch (Exception e) {
                    System.out.println("Row parse error: " + line);
                }
            }

        } catch (IOException e) {
            System.out.println("CSV HISTORICAL load error: " + e.getMessage());
        }


        return matches;
    }

    private static boolean isNumeric(String value) {
        if (value == null) return false;
        try {
            Double.parseDouble(value.trim().replace(",", "."));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static double safeParse(String value) {
        if (!isNumeric(value)) return 0.0;
        return Double.parseDouble(value.trim().replace(",", "."));
    }
}