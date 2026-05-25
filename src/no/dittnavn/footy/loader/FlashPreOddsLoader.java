package no.dittnavn.footy.loader;

import no.dittnavn.footy.scanner.MatchOdds;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class FlashPreOddsLoader {

    public static List<MatchOdds> load(String path) {

        List<MatchOdds> matches =
                new ArrayList<>();

        try(
                BufferedReader br =
                        new BufferedReader(
                                new FileReader(path)
                        )
        ) {

            String line;

            while((line = br.readLine()) != null){

                String[] parts =
                        line.split(",");

                if(parts.length < 6){
                    continue;
                }

                String matchDate = parts[0];
                String home = parts[1];
                String away = parts[2];

                double homeOdds =
                        Double.parseDouble(parts[3]);

                double drawOdds =
                        Double.parseDouble(parts[4]);

                double awayOdds =
                        Double.parseDouble(parts[5]);

                MatchOdds match =
                        new MatchOdds(
                                matchDate,
                                home,
                                away,
                                homeOdds,
                                drawOdds,
                                awayOdds
                        );

                matches.add(match);
            }

        } catch(Exception e){
            e.printStackTrace();
        }

        return matches;
    }
}