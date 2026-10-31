import java.util.Arrays;

public class Kasai {
    // lcp[0] = 0; lcp[r] compares suffixes sa[r - 1] and sa[r].
    // Input must be a valid lexicographically sorted suffix array.
    public static int[] lcp(String text, int[] sa) {
        int n = text.length();
        if (sa.length != n) throw new IllegalArgumentException("Wrong suffix array length");
        int[] rank = new int[n];
        for (int i = 0; i < n; i++) rank[sa[i]] = i;
        int[] lcp = new int[n];
        int h = 0;
        for (int i = 0; i < n; i++) {
            int r = rank[i];
            if (r == 0) {
                h = 0;
                continue;
            }
            int j = sa[r - 1];
            while (i + h < n && j + h < n && text.charAt(i + h) == text.charAt(j + h)) h++;
            lcp[r] = h;
            if (h > 0) h--;
        }
        return lcp;
    }

    // Only a helper for generating inputs; not included in Kasai timings.
    public static int[] simpleSuffixArray(String text) {
        Integer[] starts = new Integer[text.length()];
        for (int i = 0; i < starts.length; i++) starts[i] = i;
        Arrays.sort(starts, (a, b) -> {
            int i = a;
            int j = b;
            while (i < text.length() && j < text.length()) {
                int comparison = Character.compare(text.charAt(i), text.charAt(j));
                if (comparison != 0) return comparison;
                i++;
                j++;
            }
            return Integer.compare(text.length() - a, text.length() - b);
        });
        int[] sa = new int[starts.length];
        for (int i = 0; i < sa.length; i++) sa[i] = starts[i];
        return sa;
    }
}
