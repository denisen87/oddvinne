package no.dittnavn.footy.service;

public class ModelResult {

    public double homeProb;
    public double drawProb;
    public double awayProb;

    public ModelResult(double homeProb, double drawProb, double awayProb) {
        this.homeProb = homeProb;
        this.drawProb = drawProb;
        this.awayProb = awayProb;
    }
}