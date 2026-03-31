package no.dittnavn.footy.engine;
import java.util.List;
import no.dittnavn.footy.model.Match;
import no.dittnavn.footy.model.BacktestResult;

public class ThresholdOptimizer {

    public static void runOptimization(List<Match> matches) {

        double[] maxProbVals = {0.38, 0.40, 0.42};
        double[] probVals = {0.42, 0.45, 0.48, 0.50};
        double [] edgeVals = {0.03, 0.04, 0.05};
        double[] homeBiasVals = {0.90, 0.95, 1.0};
        double[] confidenceVals = {0.30, 0.35, 0.40, 0.45};

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


                            if (result.roi > bestROI && result.bets > 100 && result.bets < 500){
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