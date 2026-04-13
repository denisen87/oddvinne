package no.dittnavn.footy.service;

import no.dittnavn.footy.model.MatchOdds;
import no.dittnavn.footy.model.ValueBet;

import java.util.ArrayList;
import java.util.List;


public class ValueFinder {

    private final ProbabilityModel model = new ProbabilityModel();

    public List<ValueBet> find(MatchOdds m) {

        List<ValueBet> list = new ArrayList<>();

        check(list, m, "H", m.homeOdds, model.homeProb(m));
        check(list, m, "D", m.drawOdds, model.drawProb(m));
        check(list, m, "A", m.awayOdds, model.awayProb(m));

        return list;
    }

    private void check(List<ValueBet> list, MatchOdds m,
                       String type, double odds, double modelProb) {

        if (odds == 0) return;

        double implied = 1.0 / odds;
        double edge = modelProb - implied;

        if (edge > 0.05) {
            ValueBet v = new ValueBet();
            v.match = m.home + " vs " + m.away;
            v.type = type;
            v.odds = odds;
            v.impliedProb = implied;
            v.modelProb = modelProb;
            v.edge = edge;

            list.add(v);
        }
    }
}