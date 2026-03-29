package no.dittnavn.footy.analysis;

public class OddsAnalyzer {

    public double calculateValue(double probability, double odds) {
        return (odds * probability) - 1.0;
    }

}
