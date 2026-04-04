package no.dittnavn.footy.engine;

public class FeatureConfig {

    public boolean useSOT;
    public boolean useHomeBias;
    public boolean useConfidence;

    @Override
    public String toString() {
        return "SOT=" + useSOT +
                " BIAS=" + useHomeBias +
                " CONF=" + useConfidence;
    }
}