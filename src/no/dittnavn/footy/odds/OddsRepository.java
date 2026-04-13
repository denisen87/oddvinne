package no.dittnavn.footy.odds;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import no.dittnavn.footy.util.TeamNameNormalizer;

public class OddsRepository {


    // Fil med odds (football-data format)
    private static final String FILE = "data/E0.csv";

    // key = "home-away"
    public static Map<String, double[]> loadOdds() {

        Map<String, double[]> oddsMap = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(FILE))) {

            String line = br.readLine(); // hopp over header

            while ((line = br.readLine()) != null) {

                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(";",-1);

                // football-data CSV trenger minst 26 kolonner
                if (parts.length < 26) continue;

                try {

                    // riktige kolonner i football-data CSV
                    String home = TeamNameNormalizer.normalize(parts[3].trim());
                    String away = TeamNameNormalizer.normalize(parts[4].trim());

                    String homeOddsStr = parts[23].trim();
                    String drawOddsStr = parts[24].trim();
                    String awayOddsStr = parts[25].trim();

                    // hopp over hvis odds mangler
                    if (homeOddsStr.isEmpty() || drawOddsStr.isEmpty() || awayOddsStr.isEmpty()) {
                        continue;
                    }

                    double homeOdds = Double.parseDouble(homeOddsStr);
                    double drawOdds = Double.parseDouble(drawOddsStr);
                    double awayOdds = Double.parseDouble(awayOddsStr);

                    String key = home + "-" + away;

                    oddsMap.put(key, new double[]{homeOdds, drawOdds, awayOdds});

                } catch (Exception parseError) {
                    System.out.println("⚠️ Kunne ikke parse linje: " + line);
                }
            }

        } catch (Exception e) {
            System.out.println("⚠️ Klarte ikke lese odds CSV");
            e.printStackTrace();
        }

        System.out.println("✅ Odds lastet: " + oddsMap.size() + " kamper");

        return oddsMap;
    }

}
