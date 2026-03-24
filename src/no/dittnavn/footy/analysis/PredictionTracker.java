package no.dittnavn.footy.analysis;

import java.util.List;
import java.util.ArrayList;

import no.dittnavn.footy.model.MatchRecord;
import no.dittnavn.footy.model.Outcome;
import no.dittnavn.footy.model.MatchRecord;

public class PredictionTracker {

    private List<MatchRecord> history = new ArrayList<>();

    public void addPrediction(MatchRecord record){
        history.add(record);
    }

    public List<MatchRecord> getHistory(){
        return history;
    }

    public void updateResult(String home, String away, Outcome result){

        for(MatchRecord r : history){

            if(r.homeTeam.equals(home) && r.awayTeam.equals(away) && r.actualOutcome == null){

                r.actualOutcome = result;

                double stake = r.stake; // hvis lagret

                if(result == Outcome.HOME && r.bet.equals("HOME")){
                    r.profit = stake * (r.oddsHome - 1);
                }
                else if(result == Outcome.DRAW && r.bet.equals("DRAW")){
                    r.profit = stake * (r.oddsDraw - 1);
                }
                else if(result == Outcome.AWAY && r.bet.equals("AWAY")){
                    r.profit = stake * (r.oddsAway - 1);
                }
                else{
                    r.profit = -stake;
                }

                break;
            }
        }
    }


    public MatchRecord findLastPrediction(String home, String away){

        for(int i = history.size() - 1; i >= 0; i--){
            MatchRecord r = history.get(i);

            if(r.homeTeam.equalsIgnoreCase(home)
                    && r.awayTeam.equalsIgnoreCase(away)
                    && r.actualOutcome == null){
                return r;
            }
        }

        return null;
    }


}
