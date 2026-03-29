package no.dittnavn.footy.analysis;

public class ModelPerformance {

    private double brierSum = 0.0;
    private int count = 0;

    public void add(double predicted, double actual) {
        brierSum += Math.pow(predicted - actual, 2);
        count++;
    }

    public double getScore() {
        if (count == 0) return 1.0; // default score
        return brierSum / count;
    }

    public void reset() {
        brierSum = 0.0;
        count = 0;
    }

    public int getCount() {
        return count;
    }
}