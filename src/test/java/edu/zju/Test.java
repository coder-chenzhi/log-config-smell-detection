package edu.zju;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Test {

    public static void testRemoveElementFromSet() {
        Set<String> sets = new HashSet(Arrays.asList("a", "b", "c"));
        while (!sets.isEmpty()) {
            String tmp = sets.iterator().next();
            sets.remove(tmp);
            System.out.println(tmp);
        }
    }

    public static void main(String[] args) {
        testRemoveElementFromSet();
    }
}
