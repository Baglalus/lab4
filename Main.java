import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Main {
    private static final Pattern SPLITTER = Pattern.compile("[\\p{Punct}\\d\\s«…»–]+");
    private static final Comparator<String> BY_LENGTH = Comparator
            .comparingInt(Main::length).reversed().thenComparing(Comparator.naturalOrder());

    public static void main(String[] args) throws IOException {
        if (args.length > 2) {
            System.err.println("Использование: java Main.java [входной файл] [папка результатов]");
            System.exit(1);
        }
        Path input = Path.of(args.length > 0 ? args[0] : "lenin.txt");
        Path output = Path.of(args.length > 1 ? args[1] : "results");
        List<String> words = getWords(input);
        Files.createDirectories(output);

        task1(words, output);
        Map<String, Long> frequencies = task2(words, output);
        task3(words, output);
        task4(words, output);
        task5(words, output);
        task6(frequencies, output);
        System.out.println("Результаты записаны в " + output);
    }

    private static void task1(List<String> words, Path output) throws IOException {
        List<String> unique = new ArrayList<>(new TreeSet<>(words));
        write(output, "1.txt", unique);
        List<String> byLength = unique.stream().sorted(BY_LENGTH).toList();
        write(output, "1a.txt", byLength);
        write(output, "1b.txt", byLength.stream().filter(Main::isFourToSeven).toList());
    }

    private static void solveWordFrequenciesBySorting(List<String> words, Path output) throws IOException {
        // Задание 2: сортировка списка и подсчёт соседних одинаковых слов.
        List<String> sorted = new ArrayList<>(words);
        sorted.sort(Comparator.naturalOrder());
        List<String> counted = new ArrayList<>();
        for (int i = 0; i < sorted.size();) {
            int end = i + 1;
            while (end < sorted.size() && sorted.get(i).equals(sorted.get(end))) {
                end++;
            }
            counted.add(sorted.get(i) + "\t" + (end - i));
            i = end;
        }
        write(output, "2.txt", counted);
    }

    private static Map<String, Long> task2(List<String> words, Path output) throws IOException {
        solveWordFrequenciesBySorting(words, output);
        // Задание 2а: подсчёт с помощью отображения.
        Map<String, Long> frequencies = new LinkedHashMap<>();
        for (String word : words) {
            frequencies.merge(word, 1L, Long::sum);
        }
        write(output, "2a.txt", format(frequencies));
        Map<String, Long> filtered = new LinkedHashMap<>();
        frequencies.forEach((word, count) -> {
            if (isFourToSeven(word)) {
                filtered.put(word, count);
            }
        });
        write(output, "2b.txt", format(filtered));
        write(output, "2v.txt", format(new TreeMap<>(filtered)));
        return frequencies;
    }

    private static void task3(List<String> words, Path output) throws IOException {
        Map<Integer, Long> lengths = new TreeMap<>();
        for (String word : words) {
            lengths.merge(length(word), 1L, Long::sum);
        }
        write(output, "3.txt", format(lengths));
    }

    private static void task4(List<String> words, Path output) throws IOException {
        long totalCharacters = words.stream().mapToLong(Main::length).sum();
        write(output, "4.txt", List.of(Long.toString(totalCharacters)));
    }

    private static void task5(List<String> words, Path output) throws IOException {
        Map<String, Long> characters = new TreeMap<>();
        for (String word : words) {
            word.codePoints().forEach(codePoint ->
                    characters.merge(new String(Character.toChars(codePoint)), 1L, Long::sum));
        }
        write(output, "5.txt", format(characters));
        write(output, "5a.txt", format(characters));
        List<Map.Entry<String, Long>> characterRanking = new ArrayList<>(characters.entrySet());
        characterRanking.sort(Map.Entry.<String, Long>comparingByValue().reversed()
                .thenComparing(Map.Entry.comparingByKey()));
        write(output, "5b.txt", characterRanking.stream()
                .map(entry -> entry.getKey() + "\t" + entry.getValue()).toList());
    }

    private static void task6(Map<String, Long> frequencies, Path output) throws IOException {
        Map<Integer, List<Map.Entry<String, Long>>> grouped = new TreeMap<>();
        frequencies.entrySet().forEach(entry -> grouped
                .computeIfAbsent(length(entry.getKey()), ignored -> new ArrayList<>()).add(entry));
        List<String> topWords = new ArrayList<>();
        for (var group : grouped.entrySet()) {
            group.getValue().sort(Map.Entry.<String, Long>comparingByValue().reversed()
                    .thenComparing(Map.Entry.comparingByKey()));
            group.getValue().stream().limit(15).forEach(entry ->
                    topWords.add(group.getKey() + "\t" + entry.getKey() + "\t" + entry.getValue()));
        }
        write(output, "6.txt", topWords);
    }

    private static List<String> getWords(Path path) throws IOException {
        try (Stream<String> lines = Files.lines(path, StandardCharsets.UTF_8)) {
            return lines.flatMap(SPLITTER::splitAsStream)
                    .filter(word -> !word.isEmpty())
                    .map(word -> word.toLowerCase(Locale.ROOT))
                    .toList();
        }
    }

    private static int length(String word) {
        return word.codePointCount(0, word.length());
    }

    private static boolean isFourToSeven(String word) {
        int length = length(word);
        return length >= 4 && length <= 7;
    }

    private static List<String> format(Map<?, Long> map) {
        return map.entrySet().stream()
                .map(entry -> entry.getKey() + "\t" + entry.getValue()).toList();
    }

    private static void write(Path directory, String filename, List<String> lines) throws IOException {
        Files.write(directory.resolve(filename), lines, StandardCharsets.UTF_8);
    }
}
