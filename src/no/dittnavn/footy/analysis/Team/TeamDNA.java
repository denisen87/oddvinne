package no.dittnavn.footy.analysis.Team;

public class TeamDNA {

    // spillestil
    private double attackIndex = 0.5;
    private double defenseIndex = 0.5;
    private double tempoIndex = 0.5;

    // mentale egenskaper
    private double comebackAbility = 0.5;
    private double chokeFactor = 0.5;

    public TeamDNA() {}

    // getters
    public double getAttackIndex() {
        return attackIndex;
    }

    public double getDefenseIndex() {
        return defenseIndex;
    }

    public double getTempoIndex() {
        return tempoIndex;
    }

    public double getComebackAbility() {
        return comebackAbility;
    }

    public double getChokeFactor() {
        return chokeFactor;
    }

    // setters (det Main trenger)
    public void setAttackIndex(double attackIndex) {
        this.attackIndex = clamp(attackIndex);
    }

    public void setDefenseIndex(double defenseIndex) {
        this.defenseIndex = clamp(defenseIndex);
    }

    public void setTempoIndex(double tempoIndex) {
        this.tempoIndex = clamp(tempoIndex);
    }

    public void setComebackAbility(double comebackAbility) {
        this.comebackAbility = clamp(comebackAbility);
    }

    public void setChokeFactor(double chokeFactor) {
        this.chokeFactor = clamp(chokeFactor);
    }

    // auto-learning fra kamp
    public void updateFromMatch(int goalsScored, int goalsConceded) {

        if (goalsScored > 2) attackIndex += 0.02;
        if (goalsConceded < 1) defenseIndex += 0.02;

        attackIndex = clamp(attackIndex);
        defenseIndex = clamp(defenseIndex);
    }

    private double clamp(double v) {
        return Math.max(0, Math.min(1, v));
    }

    public void increaseChokeFactor(){
        chokeFactor = Math.min(1.0, chokeFactor + 0.02);
    }

    public void increaseComeback(){
        comebackAbility = Math.min(1.0, comebackAbility + 0.02);
    }
}

