package no.dittnavn.footy.engine;

import java.util.List;
import no.dittnavn.footy.model.Match;
import no.dittnavn.footy.model.BacktestResult;

public class ThresholdOptimizer {

    public static void runOptimization(List<Match> matches) {

        double[] maxProbVals = {0.52, 0.54, 0.56};
        double[] probVals = {0.51, 0.52, 0.53};
        double[] edgeVals = {0.20, 0.22, 0.24};

        double bestROI = -999;
        String bestConfig = "";

        for (double maxProb : maxProbVals) {
            for (double prob : probVals) {
                for (double edge : edgeVals) {

                    BacktestResult result =
                            BacktestRunner.run(matches, maxProb, prob, edge);

                    System.out.println(
                            "maxProb=" + maxProb +
                                    " prob=" + prob +
                                    " edge=" + edge +
                                    " ROI=" + result.roi +
                                    " bets=" + result.bets
                    );

                    if (result.roi > bestROI && result.bets > 5) {
                        bestROI = result.roi;
                        bestConfig = "maxProb=" + maxProb +
                                " prob=" + prob +
                                " edge=" + edge;
                    }
                }
            }
        }

        System.out.println("\n🔥 BEST CONFIG:");
        System.out.println(bestConfig + " ROI=" + bestROI);
        System.out.println("TEST RUN");
    }
}