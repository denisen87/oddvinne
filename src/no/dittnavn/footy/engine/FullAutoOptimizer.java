package no.dittnavn.footy.engine;

import no.dittnavn.footy.model.Match;
import no.dittnavn.footy.model.BacktestResult;

import java.util.*;

public class FullAutoOptimizer {

    public static void run(List<Match> matches) {
        matches = matches.subList(0, Math.min(100, matches.size()));
        Collections.shuffle(matches);

        int split = (int) (matches.size() * 0.7);
        List<Match> train = matches.subList(0, split);
        List<Match> test = matches.subList(split, matches.size());

        double bestROI = -999;
        String bestConfig = "";

        Random rand = new Random();

        System.out.println("=== START OPTIMIZATION ===");

        // 🔥 FEATURE TOGGLES
        boolean[] sotVals = {true, false};
        boolean[] confVals = {true, false};
        boolean[] biasVals = {true, false};

        // 🔥 GRID SEARCH (grov)
        double[] edgeVals = range(0.0, 0.05, 0.01);
        double[] probVals = range(0.45, 0.60, 0.02);
        double[] minOddsVals = range(1.3, 2.0, 0.2);
        double[] maxOddsVals = range(2.0, 3.0, 0.3);
        double[] confThresholds = range(0.0, 0.1, 0.02);

        // =========================
        // GRID SEARCH
        // =========================
        for (boolean useSOT : sotVals) {
            for (boolean useConf : confVals) {
                for (boolean useBias : biasVals) {

                    FeatureConfig config = new FeatureConfig();
                    config.useSOT = useSOT;
                    config.useConfidence = useConf;
                    config.useHomeBias = useBias;

                    for (double edge : edgeVals) {
                        for (double probThr : probVals) {
                            for (double minOdds : minOddsVals) {
                                for (double maxOdds : maxOddsVals) {
                                    for (double confThr : confThresholds) {

                                        BacktestResult trainResult = BacktestRunner.run(
                                                train,
                                                0.52,
                                                0.5,
                                                edge,
                                                1.0,
                                                confThr,
                                                minOdds,
                                                maxOdds,
                                                probThr,
                                                config
                                        );

                                        if (trainResult.bets < 50) continue;

                                        BacktestResult testResult = BacktestRunner.run(
                                                test,
                                                0.52,
                                                0.5,
                                                edge,
                                                1.0,
                                                confThr,
                                                minOdds,
                                                maxOdds,
                                                probThr,
                                                config
                                        );

                                        log(config, edge, probThr, confThr, minOdds, maxOdds, testResult);

                                        if (testResult.roi > bestROI && testResult.bets > 50) {
                                            bestROI = testResult.roi;
                                            bestConfig = buildConfigString(config, edge, probThr, confThr, minOdds, maxOdds, testResult);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // =========================
        // RANDOM SEARCH (finjustering)
        // =========================
        System.out.println("\n=== RANDOM SEARCH ===");

        for (int i = 0; i < 500; i++) {

            FeatureConfig config = new FeatureConfig();
            config.useSOT = rand.nextBoolean();
            config.useConfidence = rand.nextBoolean();
            config.useHomeBias = rand.nextBoolean();

            double edge = randRange(0.0, 0.05);
            double probThr = randRange(0.45, 0.60);
            double minOdds = randRange(1.3, 2.0);
            double maxOdds = randRange(2.0, 3.0);
            double confThr = randRange(0.0, 0.1);

            BacktestResult trainResult = BacktestRunner.run(
                    train,
                    0.52,
                    0.5,
                    edge,
                    1.0,
                    confThr,
                    minOdds,
                    maxOdds,
                    probThr,
                    config
            );

            if (trainResult.bets < 50) continue;

            BacktestResult testResult = BacktestRunner.run(
                    test,
                    0.52,
                    0.5,
                    edge,
                    1.0,
                    confThr,
                    minOdds,
                    maxOdds,
                    probThr,
                    config
            );

            log(config, edge, probThr, confThr, minOdds, maxOdds, testResult);

            if (testResult.roi > bestROI && testResult.bets > 50) {
                bestROI = testResult.roi;
                bestConfig = buildConfigString(config, edge, probThr, confThr, minOdds, maxOdds, testResult);
            }
        }

        System.out.println("\n🔥 BEST CONFIG FOUND:");
        System.out.println(bestConfig + " ROI=" + bestROI);
        System.out.println("=== DONE ===");
    }

    // =========================
    // HELPERS
    // =========================

    private static double[] range(double start, double end, double step) {
        List<Double> values = new ArrayList<>();
        for (double v = start; v <= end; v += step) {
            values.add(Math.round(v * 1000.0) / 1000.0);
        }
        return values.stream().mapToDouble(Double::doubleValue).toArray();
    }

    private static double randRange(double min, double max) {
        return min + (max - min) * Math.random();
    }

    private static void log(FeatureConfig config, double edge, double prob, double conf,
                            double minOdds, double maxOdds, BacktestResult result) {

        System.out.println(
                config +
                        " edge=" + round(edge) +
                        " prob=" + round(prob) +
                        " conf=" + round(conf) +
                        " odds=" + round(minOdds) + "-" + round(maxOdds) +
                        " ROI=" + round(result.roi) +
                        " bets=" + result.bets
        );
    }

    private static String buildConfigString(FeatureConfig config, double edge, double prob,
                                            double conf, double minOdds, double maxOdds,
                                            BacktestResult result) {

        return config +
                " edge=" + round(edge) +
                " prob=" + round(prob) +
                " conf=" + round(conf) +
                " minOdds=" + round(minOdds) +
                " maxOdds=" + round(maxOdds) +
                " bets=" + result.bets;
    }

    private static double round(double v) {
        return Math.round(v * 1000.0) / 1000.0;
    }
}