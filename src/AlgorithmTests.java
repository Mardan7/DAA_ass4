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

    public static void main(String[] args) {
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
            for (int r = 1; r < sa.length; r++) {
                int h = 0;
                while (sa[r] + h < text.length() && sa[r - 1] + h < text.length()
                        && text.charAt(sa[r] + h) == text.charAt(sa[r - 1] + h)) h++;
                check(actual[r] == h, "Random LCP");
            }
        }
        System.out.println("PASS: " + checks + " checks");
    }
}
