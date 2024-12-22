package io.papermc.paper.util;

public final class MappingEnvironment {
    public static final boolean DISABLE_PLUGIN_REMAPPING = Boolean.getBoolean("paper.disablePluginRemapping");

    private MappingEnvironment() {
    }

    public static boolean reobf() {
        throw new RuntimeException("Stub!");
    }

    public static boolean hasMappings() {
        throw new RuntimeException("Stub!");
    }
}
