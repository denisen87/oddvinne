package no.dittnavn.footy.analysis.learning;

public class ModelWeights {

    public double ratingWeight = 0.002;
    public double formWeight = 0.4;
    public double winrateWeight = 0.3;
    public double extraWeight = 0.1;

    public void adjust(double error){

        ratingWeight += 0.0001 * error;
        formWeight   += 0.01 * error;
        winrateWeight+= 0.01 * error;
        extraWeight  += 0.005 * error;

        normalize();
    }

    private void normalize(){
        double sum = ratingWeight + formWeight + winrateWeight + extraWeight;

        ratingWeight /= sum;
        formWeight   /= sum;
        winrateWeight/= sum;
        extraWeight  /= sum;
    }
}
