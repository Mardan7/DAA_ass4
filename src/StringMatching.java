import java.util.ArrayList;
import java.util.List;

public class StringMatching {
    public static class Result {
        public final List<Integer> indices = new ArrayList<>();
        public long preprocessingComparisons;
        public long searchComparisons;
    }

    // An empty pattern matches at all boundaries, including the end of the text.
    private static Result specialCase(String text, String pattern) {
        if (pattern.length() == 0) {
            Result result = new Result();
            for (int i = 0; i <= text.length(); i++) result.indices.add(i);
            return result;
        }
        return pattern.length() > text.length() ? new Result() : null;
    }

    public static Result naive(String text, String pattern) {
        Result result = specialCase(text, pattern);
        if (result != null) return result;
        result = new Result();
        for (int i = 0; i <= text.length() - pattern.length(); i++) {
            int j = 0;
            while (j < pattern.length()) {
                result.searchComparisons++;
                if (text.charAt(i + j) != pattern.charAt(j)) break;
                j++;
            }
            if (j == pattern.length()) result.indices.add(i);
        }
        return result;
    }

    public static Result kmp(String text, String pattern) {
        Result result = specialCase(text, pattern);
        if (result != null) return result;
        result = new Result();
        int[] lps = new int[pattern.length()];
        int length = 0;
        int i = 1;
        while (i < pattern.length()) {
            result.preprocessingComparisons++;
            if (pattern.charAt(i) == pattern.charAt(length)) {
                lps[i++] = ++length;
            } else if (length > 0) {
                length = lps[length - 1];
            } else {
                lps[i++] = 0;
            }
        }
        i = 0;
        int j = 0;
        while (i < text.length()) {
            result.searchComparisons++;
            if (text.charAt(i) == pattern.charAt(j)) {
                i++;
                j++;
                if (j == pattern.length()) {
                    result.indices.add(i - j);
                    j = lps[j - 1];
                }
            } else if (j > 0) {
                j = lps[j - 1];
            } else {
                i++;
            }
        }
        return result;
    }

    public static Result rabinKarp(String text, String pattern) {
        return rabinKarp(text, pattern, 1_000_000_007L);
    }

    // A configurable modulus lets tests deliberately create hash collisions.
    static Result rabinKarp(String text, String pattern, long modulus) {
        Result result = specialCase(text, pattern);
        if (result != null) return result;
        result = new Result();
        long base = 257;
        long highestPower = 1;
        long patternHash = 0;
        long windowHash = 0;
        int m = pattern.length();
        for (int i = 0; i < m; i++) {
            patternHash = (patternHash * base + pattern.charAt(i)) % modulus;
            windowHash = (windowHash * base + text.charAt(i)) % modulus;
            if (i < m - 1) highestPower = highestPower * base % modulus;
        }
        for (int i = 0; i <= text.length() - m; i++) {
            if (patternHash == windowHash) {
                int j = 0;
                while (j < m) {
                    result.searchComparisons++;
                    if (text.charAt(i + j) != pattern.charAt(j)) break;
                    j++;
                }
                if (j == m) result.indices.add(i);
            }
            if (i < text.length() - m) {
                windowHash = (windowHash - text.charAt(i) * highestPower % modulus
                        + modulus) % modulus;
                windowHash = (windowHash * base + text.charAt(i + m)) % modulus;
            }
        }
        return result;
    }
}
