package no.dittnavn.footy.analysis.simulation;

import no.dittnavn.footy.db.DatabaseManager;
import no.dittnavn.footy.model.Match;
import no.dittnavn.footy.model.MatchRecord;
import java.util.Map;
import java.util.HashMap;
import no.dittnavn.footy.stats.TeamStats;

import java.util.List;

public class BacktestEngine {

    public static void runBacktest() {

        List<Match> matches = DatabaseManager.getHistoricalMatchesOrdered();

        Map<String, TeamStats> stats = new HashMap<>();

        ModelWrapper model = new ModelWrapper(stats);

        Bankroll bankroll = new Bankroll(1000);

        int totalBets = 0;

        for (Match m : matches) {

            if (model.getMatchCount() < 50) {
                model.update(m, null); // eller bare skip hvis du vil
                continue;
            }

            MatchRecord p = model.predict(m);

            Bet bet = ValueFinder.findBet(p, m);

            if (bet != null) {

                totalBets++;

                double stake = bankroll.calculateStake(bet.getEdge());
                double odds = bet.getOdds();

                boolean win = isWin(bet, m);

                if (win) {
                    double profit = stake * (odds - 1);
                    bankroll.addProfit(profit);
                } else {
                    bankroll.addProfit(-stake);
                }
            }

            model.update(m, p);

            updateStats(stats, m);
        }

        System.out.println("\n=== BACKTEST RESULT ===");
        System.out.println("Total bets: " + totalBets);
        System.out.printf("Final bankroll: %.2f\n", bankroll.getBalance());
        System.out.printf("ROI: %.2f%%\n", bankroll.getROI());
    }

    private static boolean isWin(Bet bet, Match m) {

        String result;

        if (m.getHomeGoals() > m.getAwayGoals()) result = "HOME";
        else if (m.getHomeGoals() < m.getAwayGoals()) result = "AWAY";
        else result = "DRAW";

        return bet.getType().equals(result);
    }

    private static void updateStats(Map<String, TeamStats> stats, Match m) {

        String homeName = m.getHomeTeam();
        String awayName = m.getAwayTeam();

        TeamStats home = stats.computeIfAbsent(homeName, TeamStats::new);
        TeamStats away = stats.computeIfAbsent(awayName, TeamStats::new);

        home.updateFromMatch(m);
        away.updateFromMatch(m);

        double homeScore = m.getHomeGoals() > m.getAwayGoals() ? 1 :
                m.getHomeGoals() == m.getAwayGoals() ? 0.5 : 0;

        double awayScore = 1 - homeScore;

        home.updateElo(away.getElo(), homeScore);
        away.updateElo(home.getElo(), awayScore);
    }
}