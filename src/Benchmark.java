import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Random;
import java.util.function.Supplier;

public class Benchmark {
    private static final int RUNS = 5;
    private static final int WARMUP = 10;
    private static volatile Object sink;

    public static String randomString(Random random, int size) {
        String alphabet = "abcd";
        StringBuilder text = new StringBuilder(size);
        for (int i = 0; i < size; i++) text.append(alphabet.charAt(random.nextInt(alphabet.length())));
        return text.toString();
    }

    private static void measure(PrintWriter averages, PrintWriter raw, String algorithm,
                                int n, int m, String scenario, Supplier<Object> task) {
        for (int i = 0; i < WARMUP; i++) sink = task.get();
        long total = 0;
        long pre = 0;
        long comparisons = 0;
        int value = 0;
        for (int run = 1; run <= RUNS; run++) {
            long start = System.nanoTime();
            Object answer = task.get();
            long elapsed = System.nanoTime() - start;
            sink = answer;
            total += elapsed;
            if (answer instanceof StringMatching.Result) {
                StringMatching.Result result = (StringMatching.Result) answer;
                pre = result.preprocessingComparisons;
                comparisons = result.searchComparisons;
                value = result.indices.size();
            } else if (answer instanceof Integer) {
                value = (Integer) answer;
            } else {
                value = ((int[]) answer).length;
            }
            raw.printf(Locale.US, "%s,%d,%d,%s,%d,%d%n", algorithm, n, m, scenario, run, elapsed);
        }
        averages.printf(Locale.US, "%s,%d,%d,%s,%.6f,%d,%d,%d%n",
                algorithm, n, m, scenario, total / (RUNS * 1_000_000.0), pre, comparisons, value);
    }

    public static void main(String[] args) throws Exception {
        Files.createDirectories(Paths.get("results"));
        Random random = new Random(42);
        try (PrintWriter averages = new PrintWriter("results/averages.csv", "UTF-8");
             PrintWriter raw = new PrintWriter("results/raw.csv", "UTF-8");
             PrintWriter environment = new PrintWriter("results/environment.txt", "UTF-8")) {
            environment.println("java.version=" + System.getProperty("java.version"));
            environment.println("java.vm.name=" + System.getProperty("java.vm.name"));
            environment.println("os.name=" + System.getProperty("os.name"));
            environment.println("os.arch=" + System.getProperty("os.arch"));
            environment.println("availableProcessors=" + Runtime.getRuntime().availableProcessors());
            environment.println("processor=" + System.getenv("PROCESSOR_IDENTIFIER"));
            environment.println("seed=42; alphabet=abcd; warmup=10; measuredRuns=5; patternLength=20");
            averages.println("algorithm,n,m,scenario,average_ms,preprocessing_comparisons,search_comparisons,result");
            raw.println("algorithm,n,m,scenario,run,elapsed_ns");
            for (int size : new int[]{100, 500, 1000}) {
                String a = randomString(random, size);
                String b = randomString(random, size);
                measure(averages, raw, "LCS", size, size, "random", () -> DynamicProgramming.lcs(a, b));
                measure(averages, raw, "Edit Distance", size, size, "random", () -> DynamicProgramming.editDistance(a, b));
            }
            for (int size : new int[]{10000, 50000, 100000}) {
                String text = randomString(random, size);
                String present = text.substring(size / 2, size / 2 + 20);
                String absent;
                do {
                    absent = randomString(random, 20);
                } while (!StringMatching.naive(text, absent).indices.isEmpty());
                for (String scenario : new String[]{"present", "absent"}) {
                    final String pattern = scenario.equals("present") ? present : absent;
                    if (!StringMatching.naive(text, pattern).indices.equals(StringMatching.kmp(text, pattern).indices)
                            || !StringMatching.naive(text, pattern).indices.equals(StringMatching.rabinKarp(text, pattern).indices)) {
                        throw new AssertionError("Benchmark results disagree");
                    }
                    measure(averages, raw, "Naive", size, 20, scenario, () -> StringMatching.naive(text, pattern));
                    measure(averages, raw, "KMP", size, 20, scenario, () -> StringMatching.kmp(text, pattern));
                    measure(averages, raw, "Rabin-Karp", size, 20, scenario, () -> StringMatching.rabinKarp(text, pattern));
                }
            }
            for (int size : new int[]{1000, 5000, 10000}) {
                String text = randomString(random, size);
                int[] sa = Kasai.simpleSuffixArray(text);
                measure(averages, raw, "Kasai", size, 0, "random", () -> Kasai.lcp(text, sa));
            }
        }
        System.out.println("Saved results/averages.csv, results/raw.csv, results/environment.txt");
    }
}
