package no.dittnavn.footy.analysis.learning;

import no.dittnavn.footy.model.Outcome;
import java.util.ArrayList;
import java.util.List;

public class PredictionTracker {

    private List<PredictionRecord> history = new ArrayList<>();

    // bankroll for Kelly etc
    private double bankroll = 1000.0;

    public void addPrediction(PredictionRecord record){
        history.add(record);
    }

    public List<PredictionRecord> getHistory(){
        return history;
    }

    public double getBankroll(){
        return bankroll;
    }

    public void setBankroll(double bankroll){
        this.bankroll = bankroll;
    }

    // 🔥 FINN SISTE PREDICTION FOR EN KAMP
    public PredictionRecord findLastPrediction(String home, String away){

        for(int i = history.size() - 1; i >= 0; i--){
            PredictionRecord r = history.get(i);

            if(r.homeTeam.equalsIgnoreCase(home)
                    && r.awayTeam.equalsIgnoreCase(away)){
                return r;
            }
        }

        return null;
    }

    // 🔥 OPPDATER RESULTAT ETTER KAMP
    public void updateResult(String home, String away, Outcome outcome){

        PredictionRecord r = findLastPrediction(home, away);

        if(r == null) return;

        r.actualOutcome = outcome;

        // enkel bankroll update
        if(r.bet != null){

            boolean win =
                    (outcome == Outcome.HOME && r.bet.equals("HOME")) ||
                            (outcome == Outcome.DRAW && r.bet.equals("DRAW")) ||
                            (outcome == Outcome.AWAY && r.bet.equals("AWAY"));

            if(win){
                bankroll += r.profit;
            } else {
                bankroll -= r.stake;
            }
        }
    }
}