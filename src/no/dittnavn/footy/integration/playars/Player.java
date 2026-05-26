
package no.dittnavn.footy.integration.playars;

public class Player {

    private String name;
    private String nationality;
    private String position;
    private String foot;
    private int height;
    private int weight;
    private String birthdate;
    private String shirtNumber;
    private String team;

    public Player(
            String name,
            String nationality,
            String position,
            String foot,
            int height,
            int weight,
            String birthdate,
            String shirtNumber,
            String team
    ) {
        this.name = name;
        this.nationality = nationality;
        this.position = position;
        this.foot = foot;
        this.height = height;
        this.weight = weight;
        this.birthdate = birthdate;
        this.shirtNumber = shirtNumber;
        this.team = team;
    }

    public String getName() {
        return name;
    }

    public String getNationality() {
        return nationality;
    }

    public String getPosition() {
        return position;
    }

    public String getFoot() {
        return foot;
    }

    public int getHeight() {
        return height;
    }

    public int getWeight() {
        return weight;
    }

    public String getBirthdate() {
        return birthdate;
    }

    public String getShirtNumber() {
        return shirtNumber;
    }

    public String getTeam(){
        return team;
    }
}