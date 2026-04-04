package no.dittnavn.footy.analysis.simulation;

import no.dittnavn.footy.analysis.NeuralModel;
import no.dittnavn.footy.analysis.features.MatchFeatures;
import no.dittnavn.footy.analysis.ProbabilityAnalysis;
import no.dittnavn.footy.model.Outcome;
import no.dittnavn.footy.stats.StatsIndeks;
import no.dittnavn.footy.stats.TeamStats;
import no.dittnavn.footy.analysis.bet.BankrollManager;
import no.dittnavn.footy.analysis.simulation.ManualBettingSimulator;
import no.dittnavn.footy.analysis.bet.KellyCalculator;
import no.dittnavn.footy.analysis.features.MatchFeatures;
import no.dittnavn.footy.analysis.NeuralModel;
import no.dittnavn.footy.model.TeamOutcome;

import java.util.Scanner;

public class ManualBettingSimulator {

    private NeuralModel neural;
    private ProbabilityAnalysis analysis;
    private BankrollManager bankroll;
    private Scanner scanner = new Scanner(System.in);
    private double totalBrier = 0.0;
    private int matchCount = 0;


    public ManualBettingSimulator(NeuralModel neural,
                                  ProbabilityAnalysis analysis, BankrollManager bankroll) {
        this.neural = neural;
        this.analysis = analysis;
        this.bankroll = bankroll;
    }


    public void run(StatsIndeks indeks) {

        Scanner scanner = new Scanner(System.in);

        System.out.print("Hjemmelag: ");
        String homeName = scanner.nextLine();

        System.out.print("Bortelag: ");
        String awayName = scanner.nextLine();

        TeamStats home = indeks.getTeam(homeName);
        TeamStats away = indeks.getTeam(awayName);

        if (home == null || away == null) {
            System.out.println("Lag finnes ikke.");
            return;
        }

// generer realistiske bookmaker-odds
        double oddsHome = 1.8 + Math.random();
        double oddsDraw = 2.8 + Math.random();
        double oddsAway = 2.2 + Math.random();

// lag features med ekte odds
        MatchFeatures features = new MatchFeatures(home, away, oddsHome, oddsDraw, oddsAway);


        double[] probs = neural.predictAll(features);

        double pHome = probs[0];
        double pDraw = probs[1];
        double pAway = probs[2];

        System.out.println("AI sannsynligheter:");
        System.out.println("Hjem: " + pHome);
        System.out.println("Uavgjort: " + pDraw);
        System.out.println("Borte: " + pAway);

// 🔥 SPØR OM ODDS FØRST
        System.out.print("Odds H: ");
        double oddsH = Double.parseDouble(scanner.nextLine());

        System.out.print("Odds U: ");
        double oddsU = Double.parseDouble(scanner.nextLine());

        System.out.print("Odds B: ");
        double oddsB = Double.parseDouble(scanner.nextLine());

// 🔥 SÅ REGN UT VALUE
        double valueHome = pHome * oddsH - 1;
        double valueDraw = pDraw * oddsU - 1;
        double valueAway = pAway * oddsB - 1;

        System.out.println("Value H: " + valueHome);
        System.out.println("Value U: " + valueDraw);
        System.out.println("Value B: " + valueAway);

        // enkel value
        double bestValue = Math.max(valueHome, Math.max(valueDraw, valueAway));

        if (bestValue <= 0) {
            System.out.println("Ingen value bet.");
            return;
        }

        Outcome bet;

        if (bestValue == valueHome) {
            bet = Outcome.HOME;
        } else if (bestValue == valueDraw) {
            bet = Outcome.DRAW;
        } else {
            bet = Outcome.AWAY;
        }

        System.out.println("AI anbefaler: " + bet);

        if ((bet == Outcome.HOME && (oddsH < 1.6 || oddsH > 3.0)) ||
                (bet == Outcome.AWAY && (oddsB < 1.6 || oddsB > 3.0)) ||
                (bet == Outcome.DRAW && (oddsU < 1.6 || oddsU > 3.0))) {

            System.out.println("Skipper (odds/filter)");
            return;
        }


            KellyCalculator kelly = new KellyCalculator();

            double fraction = kelly.calculateStake(2, 1, 1);

            double stake = bankroll.getBankroll() * fraction;


            System.out.println("AI spiller Hjemmeseier");
            System.out.println("Stake: " + stake);

            // resultat
            System.out.print("Resultat (H/U/B): ");
            String res = scanner.nextLine();

            double actual;

            if (res.equalsIgnoreCase("H")) actual = 1;
            else if (res.equalsIgnoreCase("B")) actual = 0;
            else actual = 0.5;

            // bankroll
            if (res.equalsIgnoreCase("H")) {
                bankroll.win(stake * (oddsH - 1));
            } else {
                bankroll.lose(stake);
            }

            double profit;

            if (res.equalsIgnoreCase("H")) {
                profit = stake * (oddsH - 1);
            } else {
                profit = -stake;
            }

            double signal = profit / stake;


            // læring
// læring
            Outcome actualOutcome;

            if (res.equalsIgnoreCase("H")) actualOutcome = Outcome.HOME;
            else if (res.equalsIgnoreCase("B")) actualOutcome = Outcome.AWAY;
            else actualOutcome = Outcome.DRAW;

            double actualHome = actualOutcome == Outcome.HOME ? 1.0 : 0.0;

            double profitSignal = profit / stake;

// tren modellen
            neural.train(
                    features,
                    actualHome,
                    profit,
                    0.0
            );


            System.out.println("Ny bankroll: " + bankroll);
        }


    public void simulateMatch(
            TeamStats home,
            TeamStats away,
            double oddsHome,
            double oddsDraw,
            double oddsAway) {

        // 1️⃣ features
        MatchFeatures features =
                new MatchFeatures(home, away, oddsHome, oddsDraw, oddsAway);

        // 2️⃣ prediksjon
        double[] probs = neural.predictAll(features);

        double pHome = probs[0];
        double pDraw = probs[1];
        double pAway = probs[2];

        // 3️⃣ velg bet (enkel edge-test)
        String bet;

        if (neural.shouldBet(pHome, oddsHome)) {
            bet = "HOME";
        } else if (neural.shouldBet(pAway, oddsAway)) {
            bet = "AWAY";
        } else if (neural.shouldBet(pDraw, oddsDraw)) {
            bet = "DRAW";
        } else {
            return; // ingen spill
        }

        // 4️⃣ stake
        double stake = bankroll.getBankroll() * 0.03;

        // 5️⃣ simuler faktisk resultat
        double r = Math.random();
        Outcome actual;

        if (r < pHome) actual = Outcome.HOME;
        else if (r < pHome + pDraw) actual = Outcome.DRAW;
        else actual = Outcome.AWAY;

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

        if (matchCount % 1000 == 0) {
            System.out.println("Avg Brier: "
                    + (totalBrier / matchCount));
        }

        // ---- PROFIT ----
        boolean win =
                (bet.equals("HOME") && actual == Outcome.HOME) ||
                        (bet.equals("AWAY") && actual == Outcome.AWAY) ||
                        (bet.equals("DRAW") && actual == Outcome.DRAW);

        double odds =
                bet.equals("HOME") ? oddsHome :
                        bet.equals("AWAY") ? oddsAway :
                                oddsDraw;

        // 🔥 ODDS FILTER (HER!)
// 🔥 ODDS FILTER (KORREKT HER)
        if (odds < 1.6 || odds > 3.0) {
            return;
        }

        double profit = win ? stake * (odds - 1) : -stake;

        // oppdater bankroll
        if (win) bankroll.win(stake * (odds - 1));
        else bankroll.lose(stake);

        // ---- TRENING ----
        double actualHome = actual == Outcome.HOME ? 1.0 : 0.0;

        neural.train(
                features,
                actualHome,
                profit,
                brier
        );

        System.out.println("Ny bankroll: " + bankroll.getBankroll());
    }
}