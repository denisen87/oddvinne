package no.dittnavn.footy.model;

import no.dittnavn.footy.db.DatabaseManager;
import no.dittnavn.footy.engine.ThresholdOptimizer;
import no.dittnavn.footy.model.Match;

import java.util.List;

public class OptimizerMain {

    public static void main(String[] args) {

        System.out.println("=== RUNNING OPTIMIZER ===");

        DatabaseManager.init();

        List<Match> matches =
                DatabaseManager.getHistoricalMatchesOrdered();

        System.out.println("Matches loaded: " + matches.size());

        ThresholdOptimizer.runOptimization(matches);

        System.out.println("=== DONE ===");
    }
}