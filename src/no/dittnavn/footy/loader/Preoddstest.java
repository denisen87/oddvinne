package no.dittnavn.footy.loader;

import java.util.ArrayList;
import java.util.List;

import no.dittnavn.footy.model.MatchOdds;
import java.util.List;

public class Preoddstest {

    public static void main(String[] args) {

        List<OddsRow> odds = new ArrayList<>();

        odds.addAll(Preoddsloader.load("data/I1pre.csv")); // Serie A
        odds.addAll(Preoddsloader.load("data/E0pre.csv")); // Premier League
        odds.addAll(Preoddsloader.load("data/E1pre.csv")); // Premier League
        odds.addAll(Preoddsloader.load("data/SP1pre.csv")); // Laliga
        odds.addAll(Preoddsloader.load("data/NORpre.csv")); // Eliteserien
        odds.addAll(Preoddsloader.load("data/F1pre.csv")); // Ligue1
        odds.addAll(Preoddsloader.load("data/D1pre.csv")); // Bundesliga
        odds.addAll(Preoddsloader.load("data/EL1pre.csv")); // League one
        odds.addAll(Preoddsloader.load("data/EL2pre.csv")); // League two
        odds.addAll(Preoddsloader.load("data/SP2pre.csv")); // segunda league
        odds.addAll(Preoddsloader.load("data/SC1pre.csv")); // skotsk premierleague
        odds.addAll(Preoddsloader.load("data/Belpre.csv")); // JupilerLeague
        odds.addAll(Preoddsloader.load("data/N1pre.csv")); // Eresdivisjon2
        odds.addAll(Preoddsloader.load("data/FINpre.csv")); // veikausligaen



        // ✅ LAG MATCHES HER
        List<MatchOdds> matches = OddsGrouper.group(odds);

        System.out.println("Using delimiter: ;");
        System.out.println("TOTAL ROWS: " + odds.size());
        System.out.println("TOTAL MATCHES: " + matches.size());

        for (MatchOdds m : matches) {
            System.out.printf(
                    "%s vs %s | H=%.2f D=%.2f A=%.2f%n",
                    m.home,
                    m.away,
                    m.homeOdds,
                    m.drawOdds,
                    m.awayOdds
            );
        }
    }

}