package no.dittnavn.footy.analysis;

import no.dittnavn.footy.stats.TeamStats;

public class ModelTrainer {

    public static void adaptElo(
            TeamStats home,
            TeamStats away,
            double confidence,
            String result // HOME / DRAW / AWAY
    ){

        double homeScore;
        double awayScore;

        if(result.equals("HOME")){
            homeScore = 1.0;
            awayScore = 0.0;
        }
        else if(result.equals("AWAY")){
            homeScore = 0.0;
            awayScore = 1.0;
        }
        else{
            homeScore = 0.5;
            awayScore = 0.5;
        }

        // 🔥 juster K basert på confidence
        int baseK = 20;

        if(confidence > 0.8){
            baseK = 30;
        }

        // midlertidig juster rating med custom K
        double originalHomeRating = home.getElo();
        double originalAwayRating = away.getElo();

        double expectedHome =
                1.0 / (1.0 + Math.pow(10, (originalAwayRating-originalHomeRating)/400));

        double expectedAway = 1.0 - expectedHome;

        double newHome =
                originalHomeRating + baseK * (homeScore - expectedHome);

        double newAway =
                originalAwayRating + baseK * (awayScore - expectedAway);

        home.setElo(newHome);
        away.setElo(newAway);
    }
}