package no.dittnavn.footy.model;

public class BacktestResult {

    public double profit;
    public double roi;
    public int bets;

    public BacktestResult(double profit, double roi, int bets) {
        this.profit = profit;
        this.roi = roi;
        this.bets = bets;
    }

    public double getProfit() {
        return profit;
    }

    public double getRoi() {
        return roi;
    }

    public int getBets() {
        return bets;
    }
}