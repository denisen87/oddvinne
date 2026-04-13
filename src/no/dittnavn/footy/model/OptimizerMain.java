package no.dittnavn.footy.model;

import no.dittnavn.footy.db.DatabaseManager;
import no.dittnavn.footy.engine.FullAutoOptimizer;
import no.dittnavn.footy.model.Match;
import no.dittnavn.footy.engine.OptimizerResult;

import java.util.List;

public class OptimizerMain {

    public static void main(String[] args) {


        System.out.println("=== RUNNING OPTIMIZER ===");

        DatabaseManager.init();

        List<Match> matches =
                DatabaseManager.getHistoricalMatchesOrdered();

        System.out.println("Matches loaded: " + matches.size());

        FullAutoOptimizer.run(matches);

        OptimizerResult result = FullAutoOptimizer.run(matches);

        System.out.println("=== DONE ===");

        System.out.println("BEST CONFIG: " + result.getBestConfig());
    }
}