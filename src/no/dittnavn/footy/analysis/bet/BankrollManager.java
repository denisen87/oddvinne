package no.dittnavn.footy.analysis.bet;

public class BankrollManager {

    private double bankroll = 10000; // startkapital
    private double unitSize = 0.02;  // 2% per bet

    public double getBankroll(){
        return bankroll;
    }

    public double calculateStake(double edge){

        // edge = hvor mye value
        double stake = bankroll * unitSize * (1 + edge*2);

        // begrens risiko
        if(stake > bankroll * 0.1){
            stake = bankroll * 0.1;
        }

        return stake;
    }

    public void win(double odds, double stake){
        bankroll += stake * (odds - 1);
    }

    public void lose(double stake){
        bankroll -= stake;
    }

    public void print(){
        System.out.println("💰 Bankroll: " + bankroll);
    }

    public void win(double amount){
        bankroll += amount;
    }

}
