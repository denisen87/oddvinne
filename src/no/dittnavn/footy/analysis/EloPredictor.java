
package no.dittnavn.footy.analysis.elo;


import no.dittnavn.footy.stats.TeamStats;

public class EloPredictor {

    public double[] predict(TeamStats home, TeamStats away) {

        double homeRating = home.getElo();
        double awayRating = away.getElo();

        double expectedHome =
                1.0 / (1.0 + Math.pow(10, (awayRating - homeRating) / 400));

        double expectedAway = 1 - expectedHome;

        double draw = 0.22;

        return new double[]{
                expectedHome * (1 - draw),
                draw,
                expectedAway * (1 - draw)
        };
    }
}

