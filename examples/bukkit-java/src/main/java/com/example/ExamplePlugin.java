package com.example;

import com.squareup.moshi.Moshi;
import revxrsal.zapper.ZapperJavaPlugin;

public class ExamplePlugin extends ZapperJavaPlugin {

    @Override
    public void onEnable() {
        System.out.println(Moshi.class);
    }

    @Override
    public void onDisable() {
    }
}
