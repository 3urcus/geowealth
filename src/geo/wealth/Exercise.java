package geo.wealth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Exercise {

    private static final String WORD_SOURCE_URL =
            "https://raw.githubusercontent.com/nikiiv/JavaCodingTestOne/master/scrabble-words.txt";
    private static final Predicate<String> ONE_LETTER_PREDICATE = s -> s.contains("I") || s.contains("A");
    private static final Map<Integer, Set<String>> ALL_WORDS_BY_LENGTH = loadAllWords();

    public static void main(String[] args) {
        var start = System.nanoTime();
        Map<String, Set<String>> wordsByRootWord = ALL_WORDS_BY_LENGTH.get(9).stream()
                .filter(ONE_LETTER_PREDICATE)
                .collect(Collectors.groupingBy(w -> w, Collectors.toSet()));
        findValidWords(wordsByRootWord, 8);
        System.out.println("Execution (minus load) in milliseconds " + TimeUnit.MILLISECONDS.convert(
                System.nanoTime() - start, TimeUnit.NANOSECONDS));
    }

    private static void findValidWords(Map<String, Set<String>> wordsByRootWord, int targetLength) {
        if (targetLength == 1) {
            List<String> result = new ArrayList<>();
            for (Map.Entry<String, Set<String>> e : wordsByRootWord.entrySet()) {
                e.getValue().stream()
                        .filter(ONE_LETTER_PREDICATE)
                        .findFirst()
                        .ifPresent(w -> result.add(e.getKey()));
            }
            return;
        }

        Set<String> targetLengthWords = ALL_WORDS_BY_LENGTH.get(targetLength);
        Map<String, Set<String>> newWordsByRoot = new HashMap<>();
        for (Map.Entry<String, Set<String>> e : wordsByRootWord.entrySet()) {
            for (String w : e.getValue()) {
                for (int i = 0; i < w.length(); i++) {
                    String subStringedWord = new StringBuilder(w)
                            .deleteCharAt(i)
                            .toString();
                    if (targetLengthWords.contains(subStringedWord)) {
                        newWordsByRoot.computeIfAbsent(e.getKey(), k -> new HashSet<>()).add(subStringedWord);
                    }
                }
            }
        }

        findValidWords(newWordsByRoot, targetLength - 1);
    }

    private static Map<Integer, Set<String>> loadAllWords() {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new URL(WORD_SOURCE_URL).openConnection().getInputStream()))) {
            return br.lines()
                    .skip(2)
                    .collect(Collectors.groupingBy(String::length, Collectors.toSet()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}