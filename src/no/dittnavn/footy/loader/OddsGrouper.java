package no.dittnavn.footy.loader;

import no.dittnavn.footy.model.MatchOdds;

import java.util.*;



public class OddsGrouper {

    public static List<MatchOdds> group(List<OddsRow> rows) {

        Map<String, MatchOdds> map = new HashMap<>();

        for (OddsRow r : rows) {

            // nøkkel for kamp
            String key = r.home + "|" + r.away;

            // opprett hvis ikke finnes
            map.putIfAbsent(key, new MatchOdds(r.home, r.away));

            MatchOdds m = map.get(key);

            String home = r.home.toLowerCase().trim();
            String away = r.away.toLowerCase().trim();
            String label = r.label.toLowerCase().trim();

            if (label.contains(home)) {
                m.homeOdds = Math.max(m.homeOdds, r.price);

            } else if (label.contains(away)) {
                m.awayOdds = Math.max(m.awayOdds, r.price);

            } else if (label.contains("draw")) {
                m.drawOdds = Math.max(m.drawOdds, r.price);
            }
        }

        return new ArrayList<>(map.values());
    }
}