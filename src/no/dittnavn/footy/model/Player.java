package no.dittnavn.footy.model;

public class Player {

    private String name;
    private String position;
    private double rating;
    private int goals;
    private int assists;
    private int minutes;
    private boolean injured;

    public Player(String name, String position, double rating,
                  int goals, int assists, int minutes, boolean injured) {

        this.name = name;
        this.position = position;
        this.rating = rating;
        this.goals = goals;
        this.assists = assists;
        this.minutes = minutes;
        this.injured = injured;
    }

    public double impactScore(){

        double base = rating * 0.6;

        base += goals * 0.08;
        base += assists * 0.05;

        if(minutes < 900) base *= 0.6;

        if(injured) base *= 0.3;

        return base;
    }

    public boolean isInjured(){
        return injured;
    }

    @Override
    public String toString() {
        return name + " - " + position;
    }

}
