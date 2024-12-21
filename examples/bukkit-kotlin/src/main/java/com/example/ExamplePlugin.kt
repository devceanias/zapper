package com.example

import revxrsal.zapper.ZapperJavaPlugin

class ExamplePlugin : ZapperJavaPlugin() {

    companion object {
        init {
            println("Kotlin is already loaded!")
        }
    }

    override fun onEnable() {
        println("Hello from Kotlin!")
    }

    override fun onDisable() {
    }
}
