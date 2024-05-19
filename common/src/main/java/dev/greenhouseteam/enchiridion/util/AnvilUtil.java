package dev.greenhouseteam.enchiridion.util;

public class AnvilUtil {
    private static boolean anvilContext = false;

    public static boolean getAnvilContext() {
        return anvilContext;
    }

    public static void setAnvilContext(boolean value) {
        anvilContext = value;
    }
}
