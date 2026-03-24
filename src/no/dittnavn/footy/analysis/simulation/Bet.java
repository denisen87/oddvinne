package no.dittnavn.footy.analysis.simulation;

public class Bet {

    private String type; // HOME / DRAW / AWAY
    private double odds;
    private double edge;

    public Bet(String type, double odds, double edge) {
        this.type = type;
        this.odds = odds;
        this.edge = edge;
    }

    public String getType() { return type; }
    public double getOdds() { return odds; }
    public double getEdge() { return edge; }
}