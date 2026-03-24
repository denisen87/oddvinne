package no.dittnavn.footy.analysis;

public class SimulationResult {

    private int homeWins;
    private int draws;
    private int awayWins;
    private int total;

    public SimulationResult(int homeWins, int draws, int awayWins, int total) {
        this.homeWins = homeWins;
        this.draws = draws;
        this.awayWins = awayWins;
        this.total = total;
    }

    public double getHomeProbability() {
        return homeWins / (double) total;
    }

    public double getDrawProbability() {
        return draws / (double) total;
    }

    public double getAwayProbability() {
        return awayWins / (double) total;
    }

    @Override
    public String toString() {
        return String.format(
                "Simulert %d kamper:%nHjemmeseier: %.1f%%%nUavgjort: %.1f%%%nBorteseier: %.1f%%",
                total,
                getHomeProbability()*100,
                getDrawProbability()*100,
                getAwayProbability()*100
        );
    }
}
