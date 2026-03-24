package no.dittnavn.footy.analysis.learning;

import no.dittnavn.footy.model.Outcome;

public class BetResult {

    public double predictedProb;
    public double odds;
    public Outcome actual;

    public BetResult(double predictedProb, double odds, Outcome actual){
        this.predictedProb = predictedProb;
        this.odds = odds;
        this.actual = actual;
    }

    public double profit(){

        if(actual == Outcome.HOME){
            return odds - 1;
        }
        return -1;
    }

    public boolean wasValue(){
        return predictedProb * odds > 1.05;
    }
}
