package com.example;

import com.zaxxer.hikari.HikariConfig;
import revxrsal.zapper.ZapperJavaPlugin;

public class ExamplePlugin extends ZapperJavaPlugin {

    @Override
    public void onEnable() {
        System.out.println(HikariConfig.class);
    }

    @Override
    public void onDisable() {
    }
}
