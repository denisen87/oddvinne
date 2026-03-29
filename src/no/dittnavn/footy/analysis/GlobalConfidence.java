package no.dittnavn.footy.analysis;

public class GlobalConfidence {

    private static double penalty = 0.0;

    // Kalles når modellen var høy confidence og tok feil
    public static void adjustDown(){
        penalty += 0.01;

        if(penalty > 0.2){
            penalty = 0.2; // maks straff
        }
    }

    // Kalles når modellen var høy confidence og hadde rett
    public static void adjustUp(){
        penalty -= 0.005;

        if(penalty < 0){
            penalty = 0;
        }
    }

    public static double getPenalty(){
        return penalty;
    }
}
