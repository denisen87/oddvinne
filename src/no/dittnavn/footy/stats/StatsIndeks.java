package no.dittnavn.footy.stats;

import no.dittnavn.footy.model.Match;
import no.dittnavn.footy.util.TeamNameNormalizer;
import org.json.JSONObject;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class StatsIndeks {

    private final Map<String, TeamStats> teams = new HashMap<>();
    private Set<String> trainedMatches = new HashSet<>();
    private Map<String, String> lastUpdateDates = new HashMap<>();

    // ===============================
    // 🔑 ENESTE KILDE TIL SANNHET
    // ===============================

    private String key(String name) {
        if (name == null) return null;

        return TeamNameNormalizer.normalize(name)
                .toLowerCase()
                .replaceAll("\\s*\\(.*?\\)", "")   // 🔥 FJERNER (srb), (gre), (cze) osv
                .trim()
                .replace(".", "")
                .replace("-", " ")
                .replaceAll("\\s+", " ");          // rydder doble spaces
    }

    // ===============================
    // TEAM ACCESS
    // ===============================

    public boolean hasTeam(String name) {
        String k = key(name);
        return k != null && teams.containsKey(k);
    }

    public TeamStats getTeam(String name) {
        String k = key(name);
        return k == null ? null : teams.get(k);
    }

    public TeamStats getOrCreate(String name) {
        String k = key(name);
        if (k == null) return null;
        return teams.computeIfAbsent(k, TeamStats::new);
    }

    public int teamCount() {
        return teams.size();
    }
/*
    public void printAllTeams() {
        System.out.println("=== LAG I STATSINDEKS ===");
        for (String team : teams.keySet()) {
            System.out.println(team);
        }
        System.out.println("=== SLUTT ===");
    }

 */

    public TeamStats getRandomTeam() {
        if (teams.isEmpty()) return null;
        List<TeamStats> list = new ArrayList<>(teams.values());
        return list.get(new Random().nextInt(list.size()));
    }

    // ===============================
    // UPDATE LOGIC
    // ===============================

    public void update(Match m) {
        TeamStats home = getOrCreate(m.getHomeTeam());
        TeamStats away = getOrCreate(m.getAwayTeam());

        home.updateFromMatch(m);
        away.updateFromMatch(m);
    }

    // ===============================
    // FILE SAVE / LOAD
    // ===============================

    public void saveToFile() {
        try {
            JSONObject json = new JSONObject();

            for (String key : teams.keySet()) {
                TeamStats t = teams.get(key);

                JSONObject obj = new JSONObject();
                obj.put("games", t.getGames());
                obj.put("wins", t.getWins());
                obj.put("draws", t.getDraws());
                obj.put("losses", t.getLosses());
                obj.put("elo", t.getElo());

                json.put(key, obj);
            }

            FileWriter writer = new FileWriter("data/stats.json");
            writer.write(json.toString(2));
            writer.close();

            System.out.println("Stats lagret.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadFromFile() {
        try {
            String content = new String(Files.readAllBytes(Paths.get("data/stats.json")));
            JSONObject json = new JSONObject(content);

            for (String key : json.keySet()) {
                JSONObject obj = json.getJSONObject(key);

                TeamStats team = new TeamStats(key);
                team.setGames(obj.getInt("games"));
                team.setWins(obj.getInt("wins"));
                team.setDraws(obj.getInt("draws"));
                team.setLosses(obj.getInt("losses"));
                team.setElo(obj.getDouble("elo"));

                teams.put(key, team);
            }

            System.out.println("Stats lastet.");
        } catch (Exception e) {
            System.out.println("Ingen lagret stats funnet.");
        }
    }

    // ===============================
    // META DATA
    // ===============================

    public Set<String> getTrainedMatches() {
        return trainedMatches;
    }

    public void setTrainedMatches(Set<String> trainedMatches) {
        this.trainedMatches = trainedMatches;
    }

    public String getLastUpdateDate(String leagueCode) {
        return lastUpdateDates.get(leagueCode);
    }

    public void setLastUpdateDate(String leagueCode, String date) {
        lastUpdateDates.put(leagueCode, date);
    }

    public Collection<TeamStats> getAllTeams() {
        return teams.values();
    }

    public void printAllTeams() {
        System.out.println("=== TEAMS ===");

        for (String team : teams.keySet()) {
            System.out.println(team);
        }
    }


}