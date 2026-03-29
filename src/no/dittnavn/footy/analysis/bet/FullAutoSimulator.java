package no.dittnavn.footy.analysis.bet;

import no.dittnavn.footy.analysis.ProbabilityAnalysis;
import no.dittnavn.footy.analysis.OddsAnalyzer;
import no.dittnavn.footy.analysis.features.MatchFeatures;
import no.dittnavn.footy.analysis.NeuralModel;
import no.dittnavn.footy.model.MatchRecord;
import no.dittnavn.footy.stats.TeamStats;
import no.dittnavn.footy.model.Outcome;
import java.util.Random;

import no.dittnavn.footy.stats.StatsIndeks;


public class FullAutoSimulator {

    private NeuralModel model;
    private BankrollManager bankroll;
    private Random random = new Random();
    private double resultSignal;
    private double profit;
    private double totalBrier = 0.0;
    private int matchCount = 0;
    private int totalBets = 0;
    private int wins = 0;
    private double totalProfit = 0.0;

    public FullAutoSimulator(NeuralModel model, BankrollManager bankroll) {
        this.model = model;
        this.bankroll = bankroll;
    }

    public void simulateMatch(
            TeamStats home,
            TeamStats away,
            double oddsHome,
            double oddsDraw,
            double oddsAway) {

        // 1️⃣ FEATURES
        MatchFeatures features = new MatchFeatures(home, away, oddsHome, oddsDraw, oddsAway);

        // 2️⃣ AI prediction
        double[] probs = model.predictAll(features);
        double pHome = probs[0];
        double pDraw = probs[1];
        double pAway = probs[2];

        // realistisk draw basert på styrkeforskjell
// 1️⃣ FEATURES
// 1️⃣ FEATURES


        // 3️⃣ VALUE
        OddsAnalyzer analyzer = new OddsAnalyzer();

        double vHome = analyzer.calculateValue(pHome, oddsHome);
        double vDraw = analyzer.calculateValue(pDraw, oddsDraw);
        double vAway = analyzer.calculateValue(pAway, oddsAway);

        double best = Math.max(vHome, Math.max(vDraw, vAway));

        if (best <= 0) {
            System.out.println("AI hopper over kamp");
            return;
        }

        // 4️⃣ velg spill
        double stake = bankroll.calculateStake(best);

        Outcome bet = Outcome.HOME;
        double odds = oddsHome;

        if (best == vDraw) {
            bet = Outcome.DRAW;
            odds = oddsDraw;
        }
        if (best == vAway) {
            bet = Outcome.AWAY;
            odds = oddsAway;
        }

        System.out.println("\n🤖 AI spiller: " + bet);
        System.out.println("Stake: " + stake);

        // 5️⃣ simuler faktisk resultat tilfeldig
        double r = random.nextDouble();

        Outcome actual;
        if (r < pHome) actual = Outcome.HOME;
        else if (r < pHome + pDraw) actual = Outcome.DRAW;
        else actual = Outcome.AWAY;

        boolean win = bet == actual;

        if (win) {
            bankroll.win(odds, stake);
        } else {
            bankroll.lose(stake);
        }

        // 6️⃣ AI lærer

// ------------------------
// 6️⃣ AI lærer (3-veis)
// ------------------------

        double actualHome = actual == Outcome.HOME ? 1.0 : 0.0;
        double actualDraw = actual == Outcome.DRAW ? 1.0 : 0.0;
        double actualAway = actual == Outcome.AWAY ? 1.0 : 0.0;

        double profit = win ? stake * (odds - 1) : -stake;

// tren modellen
        model.train(
                features,
                actualHome,
                profit,
                0.0
        );

        bankroll.print();
    }


    public void run2(StatsIndeks indeks) {

        System.out.println("\n=== FULL AUTO AI SIMULATOR ===");

        // her kunne vi senere hentet kamper automatisk
        // foreløpig bare demo-melding
        System.out.println("Simulator klar. Ingen kamper lastet enda.");
    }

    public void run(StatsIndeks indeks) {

        System.out.println("\n=== FULL AUTO SIMULERING START ===");

        for(int i = 0; i < 10000; i++){

            // 1) hent tilfeldige lag
            TeamStats home = indeks.getRandomTeam();
            TeamStats away = indeks.getRandomTeam();


            if(home == null || away == null) continue;
            if(home.getName().equals(away.getName())) continue;

            System.out.println("Simulerer: " + home.getName() + " vs " + away.getName());

            // 2) lag features
// generer realistiske bookmaker-odds
            double oddsHome = 1.8 + Math.random();
            double oddsDraw = 2.8 + Math.random();
            double oddsAway = 2.2 + Math.random();

// lag features med ekte odds
            MatchFeatures features = new MatchFeatures(home, away, oddsHome, oddsDraw, oddsAway);


            // 3) AI prediction
// 3) AI prediction (ekte 3-veis)
            double[] probs = model.predictAll(features);

            double pHome = probs[0];
            double pDraw = probs[1];
            double pAway = probs[2];


            // 5) velg bet
            String bet = chooseBet(pHome, pDraw, pAway, oddsHome, oddsDraw, oddsAway);
            if(bet == null) continue;


            double stake = bankroll.getBankroll() * 0.02;

            // 6) simuler kampresultat
            Outcome actual = simulateOutcome(pHome, pDraw, pAway);

            // ---- BRIER ----
            double oHome = actual == Outcome.HOME ? 1.0 : 0.0;
            double oDraw = actual == Outcome.DRAW ? 1.0 : 0.0;
            double oAway = actual == Outcome.AWAY ? 1.0 : 0.0;

            double brier =
                    Math.pow(pHome - oHome, 2) +
                            Math.pow(pDraw - oDraw, 2) +
                            Math.pow(pAway - oAway, 2);

            totalBrier += brier;
            matchCount++;

            // 7) profit
            double profit = calculateProfit(bet, actual, stake, oddsHome, oddsDraw, oddsAway);

            totalProfit += profit;
            totalBets++;

            if(profit > 0) wins++;

            boolean correct =
                    (bet.equals("HOME") && actual == Outcome.HOME) ||
                            (bet.equals("AWAY") && actual == Outcome.AWAY) ||
                            (bet.equals("DRAW") && actual == Outcome.DRAW);

            System.out.println("Riktig tippet: " + correct);

            System.out.println("Bet: " + bet);
            System.out.println("Simulerer: " + home.getName() + " vs " + away.getName());
            System.out.println("Bet valgt: " + bet);
            System.out.println("Resultat: " + actual);
            System.out.println("Profit: " + profit);
            System.out.println("Bankroll nå: " + bankroll.getBankroll());
            System.out.println("--------------------------------");


            if(profit > 0) bankroll.win(profit);
            else bankroll.lose(stake);

            // 8) læring
            double signal = profit / stake;
            model.train(features, signal, pHome, pAway);


        }

        System.out.println("\n=== EVALUERING ===");

        System.out.println("Totale bets: " + totalBets);
        System.out.println("Total profit: " + totalProfit);
        System.out.println("ROI: " + (totalProfit / totalBets));

        System.out.println("Hit rate: " + ((double) wins / totalBets));
        System.out.println("Avg Brier: " + (totalBrier / matchCount));

        // ---- BRIER ----

        System.out.println("Hit rate: " + ((double) wins / totalBets));
        System.out.println("Avg Brier: " + (totalBrier / matchCount));


        bankroll.print();
    }

    private String chooseBet(
            double pHome, double pDraw, double pAway,
            double oddsHome, double oddsDraw, double oddsAway){

        if(model.shouldBet(pHome, oddsHome)) return "HOME";
        if(model.shouldBet(pAway, oddsAway)) return "AWAY";
        if(model.shouldBet(pDraw, oddsDraw)) return "DRAW";

        return null;
    }

    private Outcome simulateOutcome(double pHome, double pDraw, double pAway){

        double r = Math.random();

        if(r < pHome) return Outcome.HOME;
        else if(r < pHome + pDraw) return Outcome.DRAW;
        else return Outcome.AWAY;
    }

    private double calculateProfit(
            String bet,
            Outcome actual,
            double stake,
            double oddsHome,
            double oddsDraw,
            double oddsAway){

        if(bet.equals("HOME") && actual == Outcome.HOME)
            return stake * (oddsHome - 1);

        if(bet.equals("DRAW") && actual == Outcome.DRAW)
            return stake * (oddsDraw - 1);

        if(bet.equals("AWAY") && actual == Outcome.AWAY)
            return stake * (oddsAway - 1);

        return -stake;
    }


}


