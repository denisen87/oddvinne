package no.dittnavn.footy.analysis.bet;

import no.dittnavn.footy.analysis.OddsAnalyzer;

public class AutoBetEngine {

    private BankrollManager bankroll;

    public AutoBetEngine(BankrollManager bankroll){
        this.bankroll = bankroll;
    }

    public void evaluate(
            double pHome, double pDraw, double pAway,
            double oddsHome, double oddsDraw, double oddsAway){

        OddsAnalyzer analyzer = new OddsAnalyzer();

        double vHome = analyzer.calculateValue(pHome, oddsHome);
        double vDraw = analyzer.calculateValue(pDraw, oddsDraw);
        double vAway = analyzer.calculateValue(pAway, oddsAway);

        double bestValue = Math.max(vHome, Math.max(vDraw, vAway));

        if(bestValue <= 0){
            System.out.println("❌ Ingen value bet");
            return;
        }

        String betType = "";
        double odds = 0;

        if(bestValue == vHome){
            betType = "Hjemmeseier";
            odds = oddsHome;
        }
        else if(bestValue == vDraw){
            betType = "Uavgjort";
            odds = oddsDraw;
        }
        else{
            betType = "Borteseier";
            odds = oddsAway;
        }

        double stake = bankroll.calculateStake(bestValue);

        System.out.println("\n🤖 AI BET:");
        System.out.println("Spill: " + betType);
        System.out.println("Odds: " + odds);
        System.out.println("Stake: " + stake);
    }

    public void settleBet(boolean win, double odds, double stake){

        if(win){
            bankroll.win(odds, stake);
            System.out.println("🟢 BET WON");
        }else{
            bankroll.lose(stake);
            System.out.println("🔴 BET LOST");
        }

        bankroll.print();
    }
}
