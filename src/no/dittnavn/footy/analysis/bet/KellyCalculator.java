package no.dittnavn.footy.analysis.bet;

public class KellyCalculator {

    public double calculateStake(double probability, double odds, double bankroll){

        double edge = (probability * odds) - 1;

        if(edge <= 0) return 0;

        double fraction = edge / (odds - 1);

        // safety
        fraction = Math.max(0, Math.min(fraction, 0.25));

        return bankroll * fraction;
    }
}
