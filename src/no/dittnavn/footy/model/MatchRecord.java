package no.dittnavn.footy.model;

import no.dittnavn.footy.analysis.features.MatchFeatures;

public class MatchRecord {

    public MatchFeatures features;

    public double predictedHome;
    public double predictedDraw;
    public double predictedAway;

    public double oddsHome;
    public double oddsDraw;
    public double oddsAway;

    public Outcome actualOutcome;
    public double profit;

    public String homeTeam;
    public String awayTeam;

    public String bet;   // 👈 kun denne brukes
    public double stake;

    public double closingOddsHome;
    public double closingOddsDraw;
    public double closingOddsAway;
    private double confidence;
    public int dbId;

    public double valueHome;
    public double valueDraw;
    public double valueAway;
    public String date;

    public int homeFouls;
    public int awayFouls;
    public MatchRecord() {
    }


    public MatchRecord(
            String homeTeam,
            String awayTeam,
            MatchFeatures features,
            double predictedHome,
            double predictedDraw,
            double predictedAway,
            double oddsHome,
            double oddsDraw,
            double oddsAway,
            double stake,
            String betPlaced,
            double confidence
    ) {

        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.features = features;
        this.predictedHome = predictedHome;
        this.predictedDraw = predictedDraw;
        this.predictedAway = predictedAway;
        this.oddsHome = oddsHome;
        this.oddsDraw = oddsDraw;
        this.oddsAway = oddsAway;
        this.bet = betPlaced;   // 🔥 HER lagres bet riktig
        this.stake = stake;
        this.confidence = confidence;

    }

    public void setClosingOdds(double h, double d, double a){
        this.closingOddsHome = h;
        this.closingOddsDraw = d;
        this.closingOddsAway = a;
    }

    public String getBet() {
        return bet;
    }

    public double getConfidence(){
        return confidence;
    }

    public double getProfit() {
        return profit;
    }

    public void setProfit(double profit) {
        this.profit = profit;
    }

    public void setBet(String bet) {
        this.bet = bet;
    }



}

