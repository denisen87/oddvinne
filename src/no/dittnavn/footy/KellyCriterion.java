package no.dittnavn.footy;

public class KellyCriterion {

    public static double calculate(double probability, double odds){

        double edge = (probability * odds) - 1;

        double kelly = edge / (odds - 1);

        if(kelly < 0) return 0;

        return kelly;

    }

}