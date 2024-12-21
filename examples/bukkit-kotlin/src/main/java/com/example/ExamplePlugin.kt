package com.example

import revxrsal.zapper.ZapperJavaPlugin

class ExamplePlugin : ZapperJavaPlugin() {

    companion object {
        init {
            println("Nice! Kotlin has loaded!")
        }
    }

    override fun onEnable() {
        println("Hello from Kotlin!")
        println("This class is relocated: ${KotlinVersion::class.java}")
    }

    override fun onDisable() {
    }
}
