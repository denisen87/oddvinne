package no.dittnavn.footy.engine;

public class OptimizerResult {

    private FeatureConfig bestConfig;
    private double bestRoi;

    public OptimizerResult(FeatureConfig bestConfig, double bestRoi) {
        this.bestConfig = bestConfig;
        this.bestRoi = bestRoi;
    }

    public FeatureConfig getBestConfig() {
        return bestConfig;
    }

    public double getBestRoi() {
        return bestRoi;
    }
}