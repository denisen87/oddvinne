package no.dittnavn.footy.loader;

import no.dittnavn.footy.model.MatchOdds;
import java.util.List;

public class Preoddstest {

    public static void main(String[] args) {

        List<OddsRow> odds = Preoddsloader.load("data/I1pre.csv");
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