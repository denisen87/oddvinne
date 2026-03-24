package no.dittnavn.footy.analysis.learning;

import no.dittnavn.footy.analysis.features.MatchFeatures;
import no.dittnavn.footy.model.MatchRecord;
import no.dittnavn.footy.model.Outcome;

public class PredictionRecord {

    public String homeTeam;
    public String awayTeam;

    public MatchFeatures features;

    public double predictedHome;
    public double predictedDraw;
    public double predictedAway;

    public double oddsHome;
    public double oddsDraw;
    public double oddsAway;

    public double valueHome;
    public double valueDraw;
    public double valueAway;

    public String bet;
    public double stake;
    public double confidence;

    public int dbId;
    public double profit;

    public double[] neuralProbs;
    public double[] eloProbs;
    public double[] poissonProbs;
    public double[] oddsProbs;

    public Outcome actualOutcome;

    // 🔥 CONSTRUCTOR (matcher Main.java)
    public PredictionRecord(
            String homeTeam,
            String awayTeam,
            MatchFeatures features,
            double predictedHome,
            double predictedDraw,
            double predictedAway,
            double oddsHome,
            double oddsDraw,
            double oddsAway,
            String bet,
            double stake,
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

        this.bet = bet;
        this.stake = stake;
        this.confidence = confidence;
    }

    // 🔥 brukes av learning engine
    public MatchRecord toMatchRecord() {
        return new MatchRecord(
                homeTeam,
                awayTeam,
                features,
                predictedHome,
                predictedDraw,
                predictedAway,
                oddsHome,
                oddsDraw,
                oddsAway,
                confidence,   // 🔥 DENNE manglet
                bet,
                stake
        );

    }

    public double getConfidence(){
        return confidence;
    }

    public String getBet(){
        return bet;
    }
}