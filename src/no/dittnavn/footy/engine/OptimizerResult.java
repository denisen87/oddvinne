package no.dittnavn.footy.engine;

public class OptimizerResult {

    private FeatureConfig bestConfig;
    private double bestRoi;

    private double prob;
    private double edge;
    private double confidence;
    private double minOdds;
    private double maxOdds;

    // 🔥 FULL constructor (bruk denne fremover)
    public OptimizerResult(
            FeatureConfig bestConfig,
            double bestRoi,
            double prob,
            double edge,
            double confidence,
            double minOdds,
            double maxOdds
    ) {
        this.bestConfig = bestConfig;
        this.bestRoi = bestRoi;
        this.prob = prob;
        this.edge = edge;
        this.confidence = confidence;
        this.minOdds = minOdds;
        this.maxOdds = maxOdds;
    }

    // 🔥 GAMMEL constructor (valgfri - kan beholdes)
    public OptimizerResult(FeatureConfig bestConfig, double bestRoi) {
        this.bestConfig = bestConfig;
        this.bestRoi = bestRoi;
    }

    public FeatureConfig getBestConfig() { return bestConfig; }
    public double getBestRoi() { return bestRoi; }

    public double getProb() { return prob; }
    public double getEdge() { return edge; }
    public double getConfidence() { return confidence; }
    public double getMinOdds() { return minOdds; }
    public double getMaxOdds() { return maxOdds; }

    public void setBestConfig(FeatureConfig bestConfig) {
        this.bestConfig = bestConfig;
    }
}