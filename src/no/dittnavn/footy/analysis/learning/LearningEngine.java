package no.dittnavn.footy.analysis.learning;
import no.dittnavn.footy.analysis.learning.PredictionRecord;
import no.dittnavn.footy.model.Outcome;
import no.dittnavn.footy.model.MatchRecord;
import no.dittnavn.footy.analysis.features.MatchFeatures;
import no.dittnavn.footy.analysis.NeuralModel;


import java.util.ArrayList;
import java.util.List;

public class LearningEngine {

    private double totalProfit = 0;
    private int bets = 0;

    private ModelWeights weights;

    private NeuralModel model;


    public LearningEngine(ModelWeights weights, NeuralModel model){
        this.weights = weights;
        this.model = model;
    }


    private List<MatchRecord> history = new ArrayList<>();


    public void addRecord(MatchRecord record){
        history.add(record);
    }



    public void analyzePerformance(){

        if(history.isEmpty()) return;

        int correct = 0;

        for(MatchRecord r : history){

            double max =
                    Math.max(r.predictedHome,
                            Math.max(r.predictedDraw, r.predictedAway));

            boolean predictedHome = r.predictedHome == max;
            boolean predictedDraw = r.predictedDraw == max;
            boolean predictedAway = r.predictedAway == max;

            boolean wasCorrect = false;

            if(predictedHome && r.actualOutcome == Outcome.HOME) wasCorrect = true;
            if(predictedDraw && r.actualOutcome == Outcome.DRAW) wasCorrect = true;
            if(predictedAway && r.actualOutcome == Outcome.AWAY) wasCorrect = true;

            if(wasCorrect) correct++;

            // 🔥 JUSTER MODELLEN
            adjustWeights(r,wasCorrect);
        }

        double accuracy = correct / (double) history.size();
        System.out.println("AI accuracy: " + (accuracy*100) + "%");


    }
    private void adjustWeights(MatchRecord r, boolean correct){

        double learningRate = 0.05;

        if(correct){
            weights.formWeight += learningRate;
            weights.ratingWeight += learningRate;
        }else{
            weights.formWeight -= learningRate;
            weights.ratingWeight -= learningRate;
        }

        clamp();
    }
    private void clamp(){
        weights.formWeight = Math.max(0, Math.min(2, weights.formWeight));
        weights.ratingWeight = Math.max(0, Math.min(2, weights.ratingWeight));
    }

    public void addBetResult(BetResult r){

        double p = r.profit();
        totalProfit += p;
        bets++;

        System.out.println("Bet profit: " + p);
        System.out.println("Total profit: " + totalProfit);

        if(bets > 20){
            double roi = totalProfit / bets;
            System.out.println("ROI: " + (roi*100) + "%");
        }
    }

    public void trainFromReality(NeuralModel model){

        for(MatchRecord r : history){

            if(r.actualOutcome == null) continue;

            MatchFeatures f = r.features;


            double actual =
                    r.actualOutcome == Outcome.HOME ? 1 :
                            r.actualOutcome == Outcome.DRAW ? 0.5 : 0;

            double profitSignal = r.profit;

            double probabilitySignal = r.predictedHome;

            model.train(f, actual, profitSignal, probabilitySignal);

        }
    }

    public void learnFromReality(MatchRecord r){

        if(r.actualOutcome == null) return;

        MatchFeatures f = r.features;

        double actual =
                r.actualOutcome == Outcome.HOME ? 1 :
                        r.actualOutcome == Outcome.DRAW ? 0.5 : 0;

        double profitSignal = r.profit;

        double probabilitySignal = r.predictedHome;

        double oddsSignal =
                r.actualOutcome == Outcome.HOME ? r.oddsHome :
                        r.actualOutcome == Outcome.DRAW ? r.oddsDraw :
                                r.oddsAway;


        double combinedSignal =
                profitSignal * 0.6 +
                        probabilitySignal * 0.2 +
                        oddsSignal * 0.2;

        model.train(f, actual, profitSignal, combinedSignal);

    }

}
