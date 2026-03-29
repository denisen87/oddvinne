package no.dittnavn.footy.stats;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;
import java.util.HashSet;

public class StatsRepository {

    private static final String FILE = "data/stats.json";

    private Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // 🔥 lagrer hvilke matcher som allerede er trent
    private Set<Integer> trainedMatches = new HashSet<>();

    // 🔵 LOAD
    public StatsIndeks load() {
        try (FileReader reader = new FileReader(FILE)) {

            StatsData data = gson.fromJson(reader, StatsData.class);

            if (data == null) {
                System.out.println("StatsData var null – starter tom.");
                return new StatsIndeks();
            }

            // last trainedMatches
            if (data.trainedMatches != null) {
                this.trainedMatches = data.trainedMatches;
            } else {
                this.trainedMatches = new HashSet<>();
            }

            // last indeks
            if (data.indeks != null) {
                return data.indeks;
            } else {
                return new StatsIndeks();
            }

        } catch (Exception e) {
            System.out.println("StatsIndeks ble ikke lastet fra fil, returnerer tomt objekt.");
            return new StatsIndeks();
        }
    }

    // 🔵 SAVE
    public void save(StatsIndeks indeks) {
        try (FileWriter writer = new FileWriter(FILE)) {

            StatsData data = new StatsData();
            data.indeks = indeks;
            data.trainedMatches = trainedMatches;

            gson.toJson(data, writer);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 🔵 DEDUP API
    public boolean isMatchTrained(int id){
        return trainedMatches.contains(id);
    }

    public void markMatchTrained(int id){
        trainedMatches.add(id);
    }
}
