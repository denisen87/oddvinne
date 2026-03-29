package no.dittnavn.footy.model;

public class BacktestResult {
    public double roi;
    public int bets;

    public BacktestResult(double roi, int bets) {
        this.roi = roi;
        this.bets = bets;
    }
}