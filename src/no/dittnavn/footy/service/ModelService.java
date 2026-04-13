package no.dittnavn.footy.service;

public class ModelService {

    public ModelResult calculate(String home, String away) {

        // 🔥 Midlertidig (dummy)
        double homeProb = 0.45;
        double drawProb = 0.25;
        double awayProb = 0.30;

        return new ModelResult(homeProb, drawProb, awayProb);
    }
}