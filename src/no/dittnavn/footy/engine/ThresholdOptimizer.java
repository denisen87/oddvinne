package no.dittnavn.footy.engine;
import java.util.List;
import no.dittnavn.footy.model.Match;
import no.dittnavn.footy.model.BacktestResult;

public class ThresholdOptimizer {

    public static void runOptimization(List<Match> matches) {

        double[] maxProbVals = {0.50, 0.55, 0.60};
        double[] probVals = {0.48, 0.52, 0.56};
        double[] edgeVals = {0.15, 0.20, 0.25, 0.30};
        double[] homeBiasVals = {0.90, 1.0, 1.05};
        double[] confidenceVals = {0.45, 0.55, 0.65};

        double bestROI = -999;
        String bestConfig = "";

        for (double maxProb : maxProbVals) {
            for (double prob : probVals) {
                for (double edge : edgeVals) {
                    for (double homeBias : homeBiasVals) {
                        for (double conf : confidenceVals) {

                            BacktestResult result =
                                    BacktestRunner.run(matches,maxProb,prob,edge,homeBias,conf);

                            System.out.println(
                                    "maxProb=" + maxProb +
                                            " prob=" + prob +
                                            " edge=" + edge +
                                            " homeBias=" + homeBias +
                                            " conf=" + conf +
                                            " ROI=" + result.roi +
                                            " bets=" + result.bets);


                            if (result.roi > bestROI && result.bets > 5) {
                                bestROI = result.roi;
                                bestConfig =
                                        "maxProb=" + maxProb +
                                                " prob=" + prob +
                                                " edge=" + edge +
                                                " homeBias=" + homeBias +
                                                " conf=" + conf;
                            }
                        }
                    }
                }
            }
        }

        System.out.println("\n🔥 BEST CONFIG:");
        System.out.println(bestConfig + " ROI=" + bestROI);
        System.out.println("TEST RUN");
    }


}