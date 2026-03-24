package no.dittnavn.footy.loader;

import no.dittnavn.footy.model.Match;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class OddsCsvLoader {

    public static List<Match> load(String path) {

        List<Match> matches = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {

            String line = br.readLine(); // skip header

            while ((line = br.readLine()) != null) {

                String[] cols = line.split(",");

                if (cols.length < 5) continue;

                String home = cols[0].trim();
                String away = cols[1].trim();

                double homeOdds = Double.parseDouble(cols[2]);
                double drawOdds = Double.parseDouble(cols[3]);
                double awayOdds = Double.parseDouble(cols[4]);

                Match m = new Match(
                        LocalDate.now().toString(),
                        home,
                        away,
                        0,
                        0
                );

                m.setHomeOdds(homeOdds);
                m.setDrawOdds(drawOdds);
                m.setAwayOdds(awayOdds);

                matches.add(m);
            }

        } catch (Exception e) {
            System.out.println("Odds CSV load error: " + e.getMessage());
        }

        return matches;
    }
}