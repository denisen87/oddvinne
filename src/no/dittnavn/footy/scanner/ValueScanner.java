package no.dittnavn.footy.scanner;

import java.util.ArrayList;
import java.util.List;

import no.dittnavn.footy.analysis.OddsAnalyzer;
import no.dittnavn.footy.analysis.ProbabilityAnalysis;
import no.dittnavn.footy.stats.StatsIndeks;
import no.dittnavn.footy.stats.TeamStats;
import no.dittnavn.footy.model.MatchOdds;
import no.dittnavn.footy.analysis.learning.ModelWeights;

public class ValueScanner {

    private ModelWeights weights;

    public ValueScanner(ModelWeights weights){
        this.weights = weights;
    }


    private List<MatchOdds> kamper = new ArrayList<>();

    public void addMatch(MatchOdds m) {
        kamper.add(m);
    }

    public void scan(StatsIndeks indeks) {

        ProbabilityAnalysis prob = new ProbabilityAnalysis(weights);
        OddsAnalyzer odds = new OddsAnalyzer();

        System.out.println("\n=== VALUE SCANNER ===");

        for (MatchOdds m : kamper) {

            TeamStats home = indeks.getTeam(m.home);
            TeamStats away = indeks.getTeam(m.away);

            if (home == null || away == null) continue;

            double pHome = prob.homeWinProbability(home, away);
            double pDraw = prob.drawProbability(home, away);
            double pAway = prob.awayWinProbability(home, away);

            double vHome = odds.calculateValue(pHome, m.homeOdds);
            double vDraw = odds.calculateValue(pDraw, m.drawOdds);
            double vAway = odds.calculateValue(pAway, m.awayOdds);

            System.out.println("\n" + m.home + " vs " + m.away);

            if (vHome > 0.1)
                System.out.printf("🔥 VALUE HJEMME: %.3f%n", vHome);

            if (vDraw > 0.1)
                System.out.printf("🔥 VALUE UAVGJORT: %.3f%n", vDraw);

            if (vAway > 0.1)
                System.out.printf("🔥 VALUE BORTE: %.3f%n", vAway);
        }
    }
}
