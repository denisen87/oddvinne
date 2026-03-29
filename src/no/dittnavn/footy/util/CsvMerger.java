package no.dittnavn.footy.util;

import no.dittnavn.footy.loader.CsvMatchLoader;
import no.dittnavn.footy.model.Match;

import java.util.*;
import java.util.stream.Collectors;

public class CsvMerger {

    public static void merge(String mainFile, String newFile) {

        List<Match> existing = CsvMatchLoader.load(mainFile);
        List<Match> incoming = CsvMatchLoader.load(newFile);

        Set<String> keys = existing.stream()
                .map(Match::getUniqueKey)
                .collect(Collectors.toSet());

        int added = 0;

        for (Match m : incoming) {

            if (!keys.contains(m.getUniqueKey())) {
                existing.add(m);
                keys.add(m.getUniqueKey());
                added++;
            }
        }

        CsvMatchWriter.save(mainFile, existing);

        System.out.println("Nye kamper lagt til: " + added);
    }
}