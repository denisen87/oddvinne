package no.dittnavn.footy.analysis.simulation;

public class Bankroll {

    private double balance;
    private double initial;

    public Bankroll(double start) {
        this.balance = start;
        this.initial = start;
    }

    public double calculateStake(double edge) {
        // enkel Kelly-ish
        return balance * Math.min(edge, 0.02);
    }

    public void addProfit(double profit) {
        balance += profit;
    }

    public double getBalance() {
        return balance;
    }

    public double getROI() {
        return ((balance - initial) / initial) * 100;
    }
}