package no.dittnavn.footy.util;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;

public class TeamNameNormalizer {

    private static final Map<String, String> NAME_MAP = new HashMap<>();

    static {
        // England
        NAME_MAP.put("sheffield wed", "sheffield wednesday");
        NAME_MAP.put("sheff wed", "sheffield wednesday");

        NAME_MAP.put("sheffield utd", "sheffield united");
        NAME_MAP.put("sheff utd", "sheffield united");

        NAME_MAP.put("man utd", "man united");
        NAME_MAP.put("manchester utd", "man united");

        NAME_MAP.put("west brom", "west bromwich");
        NAME_MAP.put("west bromwich", "west bromwich");

        NAME_MAP.put("oxford utd", "oxford united");
        NAME_MAP.put("oxford", "oxford united");

        NAME_MAP.put("qpr", "qpr");

        NAME_MAP.put("sheffield weds", "sheffield wednesday");
        NAME_MAP.put("man city", "manchester city");
        NAME_MAP.put("man utd", "manchester united");


        // Add more mappings as necessary
    }

    public static String normalize(String name) {
        if (name == null) return "";

        // Normalize and remove accents
        name = Normalizer.normalize(name, Normalizer.Form.NFD);
        name = name.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        // Convert to lowercase and remove extra spaces
        name = name.toLowerCase().trim();
        name = name.replace(".", "")
                .replace("'", "")
                .replace("-", " ");

        // Remove club identifiers like FC, AFC, etc.
        name = name.replace(" football club", "")
                .replace(" fc", "")
                .replace(" afc", "")
                .replace(" cf", "")
                .replace(" sc", "");

        // General abbreviations
        name = name.replace(" utd", " united")
                .replace(" weds", " wednesday");

        // Specific exceptions
        if (name.equals("din. zagreb")) return "dinamo zagreb";
        if (name.equals("din zagreb")) return "dinamo zagreb";

        // Remove extra spaces and trim
        name = name.replaceAll("\\s+", " ").trim();

        // Check the map for the normalized name
        if (NAME_MAP.containsKey(name)) {
            return NAME_MAP.get(name);
        }

        return name;
    }

    private static final Map<String, String> aliases = Map.ofEntries(
            Map.entry("argentinos", "argentinos jrs"),
            Map.entry("argentinos juniors", "argentinos jrs"),

            Map.entry("estudiantes", "estudiantes lp"),
            Map.entry("estudiantes la plata", "estudiantes lp"),

            Map.entry("central cordoba", "central cordoba"),
            Map.entry("rosario", "rosario central"),

            Map.entry("independiente rivadavia", "ind rivadavia"),

            Map.entry("huracan", "huracan"),
            Map.entry("barracas", "barracas central")
    );
}