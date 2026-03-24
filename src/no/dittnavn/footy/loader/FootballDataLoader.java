package no.dittnavn.footy.loader;

import no.dittnavn.footy.model.Match;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

public class FootballDataLoader {

    public static List<Match> load(String filePath, String league) {

        List<Match> matches = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {

            // Les header
            String header = br.readLine().replace("\uFEFF", "");
            String delimiter = header.contains(";") ? ";" : ",";
            String[] columns = header.split(delimiter);

            // map kolonnenavn -> indeks
            Map<String, Integer> index = new HashMap<>();

            for (int i = 0; i < columns.length; i++) {
                index.put(columns[i].trim(), i);
            }

            System.out.println("HEADERS (" + league + "): " + index.keySet());

            String line;

            while ((line = br.readLine()) != null) {

                if (line.trim().isEmpty()) continue;

                String[] row = line.split(delimiter, -1);

                Match m = new Match();

                m.setLeague(league);

                // basic info
                m.setDate(get(row, index, "Date"));
                m.setHomeTeam(get(row, index, "HomeTeam"));
                m.setAwayTeam(get(row, index, "AwayTeam"));

                if (m.getDate().isBlank() || m.getHomeTeam().isBlank() || m.getAwayTeam().isBlank()) {
                    continue;
                }

                // goals
                m.setHomeGoals(parseInt(get(row, index, "FTHG")));
                m.setAwayGoals(parseInt(get(row, index, "FTAG")));

                // shots
                m.setHomeShots(parseInt(get(row, index, "HS")));
                m.setAwayShots(parseInt(get(row, index, "AS")));

                // shots on target
                m.setHomeShotsTarget(parseInt(get(row, index, "HST")));
                m.setAwayShotsTarget(parseInt(get(row, index, "AST")));

                // corners
                m.setHomeCorners(parseInt(get(row, index, "HC")));
                m.setAwayCorners(parseInt(get(row, index, "AC")));

                // yellow cards
                m.setHomeYellow(parseInt(get(row, index, "HY")));
                m.setAwayYellow(parseInt(get(row, index, "AY")));

                // Bet365 odds
                m.setHomeOdds(parseDouble(get(row, index, "B365H")));
                m.setDrawOdds(parseDouble(get(row, index, "B365D")));
                m.setAwayOdds(parseDouble(get(row, index, "B365A")));

                // Pinnacle odds
                m.setPsHome(parseDouble(get(row, index, "PSH")));
                m.setPsDraw(parseDouble(get(row, index, "PSD")));
                m.setPsAway(parseDouble(get(row, index, "PSA")));

                // max odds
                m.setMaxHome(parseDouble(get(row, index, "MaxH")));
                m.setMaxDraw(parseDouble(get(row, index, "MaxD")));
                m.setMaxAway(parseDouble(get(row, index, "MaxA")));

                // average odds
                m.setAvgHome(parseDouble(get(row, index, "AvgH")));
                m.setAvgDraw(parseDouble(get(row, index, "AvgD")));
                m.setAvgAway(parseDouble(get(row, index, "AvgA")));

                matches.add(m);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return matches;
    }

    // trygg henting av kolonne
    private static String get(String[] row, Map<String, Integer> index, String key) {

        Integer i = index.get(key);

        // 🔥 fallback hvis key ikke finnes (NOR.csv problem)
        if (i == null) {
            for (String k : index.keySet()) {
                if (k.trim().equalsIgnoreCase(key)) {
                    i = index.get(k);
                    break;
                }
            }
        }

        if (i == null) return "";
        if (i >= row.length) return "";

        return row[i];
    }

    private static int parseInt(String v) {

        if (v == null || v.isEmpty()) return 0;

        try {
            return Integer.parseInt(v);
        } catch (Exception e) {
            return 0;
        }
    }

    private static double parseDouble(String v) {

        if (v == null || v.isEmpty()) return 0.0;

        try {
            return Double.parseDouble(v);
        } catch (Exception e) {
            return 0.0;
        }
    }
}