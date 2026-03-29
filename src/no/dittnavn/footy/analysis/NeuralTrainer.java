package no.dittnavn.footy.analysis;

public class NeuralTrainer {

    private static double neuralTrust = 1.0;

    public static void adjustTrust(boolean wasCorrect, double confidence){

        if(confidence > 0.75){

            if(wasCorrect){
                neuralTrust += 0.02;
            } else {
                neuralTrust -= 0.05;
            }
        }

        // klipp
        if(neuralTrust < 0.5) neuralTrust = 0.5;
        if(neuralTrust > 1.5) neuralTrust = 1.5;
    }

    public static double getTrust(){
        return neuralTrust;
    }
}