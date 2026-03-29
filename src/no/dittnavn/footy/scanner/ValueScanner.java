package no.dittnavn.footy.scanner;

import java.util.ArrayList;
import java.util.List;

import no.dittnavn.footy.analysis.OddsAnalyzer;
import no.dittnavn.footy.analysis.ProbabilityAnalysis;
import no.dittnavn.footy.stats.StatsIndeks;
import no.dittnavn.footy.stats.TeamStats;
import no.dittnavn.footy.scanner.MatchOdds;
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

            TeamStats home = indeks.getTeam(m.getHome());
            TeamStats away = indeks.getTeam(m.getAway());

            if (home == null || away == null) continue;

            double pHome = prob.homeWinProbability(home, away);
            double pDraw = prob.drawProbability(home, away);
            double pAway = prob.awayWinProbability(home, away);

            double vHome = odds.calculateValue(pHome, m.getHomeOdds());
            double vDraw = odds.calculateValue(pDraw, m.getDrawOdds());
            double vAway = odds.calculateValue(pAway, m.getAwayOdds());

            System.out.println("\n" + m.getHome() + " vs " + m.getAway());

            if (vHome > 0.1)
                System.out.printf("🔥 VALUE HJEMME: %.3f%n", vHome);

            if (vDraw > 0.1)
                System.out.printf("🔥 VALUE UAVGJORT: %.3f%n", vDraw);

            if (vAway > 0.1)
                System.out.printf("🔥 VALUE BORTE: %.3f%n", vAway);
        }
    }
}
