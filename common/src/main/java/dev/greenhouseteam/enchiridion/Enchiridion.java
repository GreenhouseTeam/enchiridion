package dev.greenhouseteam.enchiridion;

import dev.greenhouseteam.enchiridion.platform.EnchiridionPlatformHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Enchiridion {
    public static final String MOD_ID = "examplemod";
    public static final String MOD_NAME = "ExampleMod";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);
    private static EnchiridionPlatformHelper helper;

    public static void init(EnchiridionPlatformHelper helper) {
        Enchiridion.helper = helper;
    }

    public static EnchiridionPlatformHelper getHelper() {
        return helper;
    }
}