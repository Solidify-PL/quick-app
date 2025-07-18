package com.rhizomind.quickapp.cache;

import com.rhizomind.quickapp.Manifest;

public class SemVer {

    public static int compareSemVer(Manifest v1, Manifest v2) {
        return compareSemVer(v1.getVersion(), v2.getVersion());
    }

    private static int compareSemVer(String v1, String v2) {
        int[] parts1 = parseSemVer(v1);
        int[] parts2 = parseSemVer(v2);

        for (int i = 0; i < 3; i++) {
            int cmp = Integer.compare(parts1[i], parts2[i]);
            if (cmp != 0) {
                return cmp;
            }
        }
        return 0;
    }

    private static int[] parseSemVer(String version) {
        String[] parts = version.split("\\.");
        int[] nums = new int[3];

        for (int i = 0; i < 3; i++) {
            if (i < parts.length) {
                String numPart = parts[i].replaceAll("[^0-9]", ""); // ignoruj np. "-alpha"
                nums[i] = numPart.isEmpty() ? 0 : Integer.parseInt(numPart);
            } else {
                nums[i] = 0;
            }
        }
        return nums;
    }

}
