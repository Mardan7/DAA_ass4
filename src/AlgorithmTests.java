import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AlgorithmTests {
    private static int checks;

    private static void check(boolean condition, String name) {
        checks++;
        if (!condition) throw new AssertionError(name);
    }

    private static void matching(String text, String pattern, List<Integer> expected) {
        check(StringMatching.naive(text, pattern).indices.equals(expected), "Naive: " + text);
        check(StringMatching.kmp(text, pattern).indices.equals(expected), "KMP: " + text);
        check(StringMatching.rabinKarp(text, pattern).indices.equals(expected), "RK: " + text);
        check(StringMatching.rabinKarp(text, pattern, 2).indices.equals(expected), "RK collisions");
    }

    // Independent LCS oracle: enumerate subsequences rather than filling a DP table.
    private static int exhaustiveLcs(String a, String b) {
        int best = 0;
        for (int mask = 0; mask < (1 << a.length()); mask++) {
            int position = 0;
            int length = 0;
            boolean found = true;
            for (int i = 0; i < a.length(); i++) {
                if ((mask & (1 << i)) == 0) continue;
                position = b.indexOf(a.charAt(i), position);
                if (position < 0) {
                    found = false;
                    break;
                }
                position++;
                length++;
            }
            if (found) best = Math.max(best, length);
        }
        return best;
    }

    // Enumerate edit paths on short inputs without using the implementation's table.
    private static int exhaustiveEdit(String a, String b) {
        if (a.isEmpty()) return b.length();
        if (b.isEmpty()) return a.length();
        int replace = (a.charAt(0) == b.charAt(0) ? 0 : 1)
                + exhaustiveEdit(a.substring(1), b.substring(1));
        return Math.min(replace, 1 + Math.min(exhaustiveEdit(a.substring(1), b),
                exhaustiveEdit(a, b.substring(1))));
    }

    private static void exhaustiveDynamicProgramming() {
        List<String> inputs = new ArrayList<>();
        for (int length = 0; length <= 4; length++) {
            for (int bits = 0; bits < (1 << length); bits++) {
                StringBuilder word = new StringBuilder();
                for (int i = 0; i < length; i++) word.append((bits & (1 << i)) == 0 ? 'a' : 'b');
                inputs.add(word.toString());
            }
        }
        for (String a : inputs) {
            for (String b : inputs) {
                check(DynamicProgramming.lcs(a, b) == exhaustiveLcs(a, b), "Exhaustive LCS");
                check(DynamicProgramming.editDistance(a, b) == exhaustiveEdit(a, b), "Exhaustive edit");
            }
        }
    }

    public static void main(String[] args) {
        exhaustiveDynamicProgramming();
        check(DynamicProgramming.lcs("abc", "abc") == 3, "LCS identical");
        check(DynamicProgramming.lcs("abc", "xyz") == 0, "LCS different");
        check(DynamicProgramming.lcs("", "abc") == 0, "LCS empty");
        check(DynamicProgramming.lcs("abc", "") == 0, "LCS empty second");
        check(DynamicProgramming.lcs("ABCBDAB", "BDCABA") == 4, "LCS general");
        check(DynamicProgramming.editDistance("abc", "abc") == 0, "Edit identical");
        check(DynamicProgramming.editDistance("", "abc") == 3, "Edit empty");
        check(DynamicProgramming.editDistance("abc", "") == 3, "Edit empty second");
        check(DynamicProgramming.editDistance("cat", "cut") == 1, "Substitution");
        check(DynamicProgramming.editDistance("cat", "cart") == 1, "Insertion");
        check(DynamicProgramming.editDistance("cart", "cat") == 1, "Deletion");
        check(DynamicProgramming.editDistance("kitten", "sitting") == 3, "Edit combined");
        matching("abcxx", "abc", Arrays.asList(0));
        matching("xxabcxx", "abc", Arrays.asList(2));
        matching("xxabc", "abc", Arrays.asList(2));
        matching("aaaaa", "aaa", Arrays.asList(0, 1, 2));
        matching("abcabc", "abc", Arrays.asList(0, 3));
        matching("abc", "z", Arrays.asList());
        matching("ab", "abc", Arrays.asList());
        matching("ab", "", Arrays.asList(0, 1, 2));
        matching("", "", Arrays.asList(0));
        matching("", "a", Arrays.asList());
        matching("ababababac", "ababac", Arrays.asList(4));
        matching("aAaA", "Aa", Arrays.asList(1));
        matching("\uD83D\uDE00a\uD83D\uDE00", "\uD83D\uDE00", Arrays.asList(0, 3));
        String repetitive = new String(new char[1000]).replace('\0', 'a');
        String lateMismatch = new String(new char[19]).replace('\0', 'a') + "b";
        matching(repetitive, lateMismatch, Arrays.asList());
        check(StringMatching.naive(repetitive, lateMismatch).searchComparisons == 19620,
                "Naive late-mismatch comparison count");
        check(StringMatching.kmp(repetitive, lateMismatch).searchComparisons == 1981,
                "KMP late-mismatch comparison count");
        check(Arrays.equals(Kasai.lcp("banana", new int[]{5, 3, 1, 0, 4, 2}),
                new int[]{0, 1, 3, 0, 0, 2}), "Banana LCP");
        check(Arrays.equals(Kasai.simpleSuffixArray("banana"), new int[]{5, 3, 1, 0, 4, 2}), "Banana SA");
        check(Kasai.lcp("", new int[0]).length == 0, "Empty LCP");
        check(Arrays.equals(Kasai.lcp("a", new int[]{0}), new int[]{0}), "Single LCP");
        check(Arrays.equals(Kasai.lcp("aaaa", new int[]{3, 2, 1, 0}), new int[]{0, 1, 2, 3}), "Repeated LCP");
        Random random = new Random(7);
        for (int trial = 0; trial < 200; trial++) {
            String text = Benchmark.randomString(random, random.nextInt(40));
            String pattern = Benchmark.randomString(random, random.nextInt(8));
            List<Integer> expected = new ArrayList<>();
            for (int i = 0; i <= text.length() - pattern.length(); i++) {
                if (text.startsWith(pattern, i)) expected.add(i);
            }
            matching(text, pattern, expected);
            int[] sa = Kasai.simpleSuffixArray(text);
            int[] actual = Kasai.lcp(text, sa);
            check(actual.length == 0 || actual[0] == 0, "First LCP is zero");
            for (int r = 1; r < sa.length; r++) {
                check(text.substring(sa[r - 1]).compareTo(text.substring(sa[r])) < 0,
                        "Suffix array lexicographic order");
                int h = 0;
                while (sa[r] + h < text.length() && sa[r - 1] + h < text.length()
                        && text.charAt(sa[r] + h) == text.charAt(sa[r - 1] + h)) h++;
                check(actual[r] == h, "Random LCP");
            }
        }
        System.out.println("PASS: " + checks + " checks");
    }
}
