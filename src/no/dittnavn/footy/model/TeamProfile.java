package no.dittnavn.footy.model;

public class TeamProfile {

    private int injuredPlayers;
    private int suspendedPlayers;
    private double coachRating; // 0-1
    private double motivation;  // 0-1
    private double fatigue;     // 0-1 (1 = veldig sliten)

    public TeamProfile() {
        this.coachRating = 0.5;
        this.motivation = 0.5;
        this.fatigue = 0.0;
    }

    // setters
    public void setInjuredPlayers(int n) { injuredPlayers = n; }
    public void setSuspendedPlayers(int n) { suspendedPlayers = n; }
    public void setCoachRating(double r) { coachRating = r; }
    public void setMotivation(double m) { motivation = m; }
    public void setFatigue(double f) { fatigue = f; }

    // getters
    public int getInjuredPlayers() { return injuredPlayers; }
    public int getSuspendedPlayers() { return suspendedPlayers; }
    public double getCoachRating() { return coachRating; }
    public double getMotivation() { return motivation; }
    public double getFatigue() { return fatigue; }

    @Override
    public String toString() {
        return "Skader=" + injuredPlayers +
                ", Susp=" + suspendedPlayers +
                ", Coach=" + coachRating +
                ", Motivasjon=" + motivation +
                ", Fatigue=" + fatigue;
    }



}


