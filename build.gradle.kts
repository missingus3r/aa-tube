buildscript {
    dependencies {
        // GeckoView 158 se compila con Kotlin 2.4.20; fijamos el compilador a la misma versión.
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.4.20")
    }
}
plugins {
    id("com.android.application") version "9.4.1" apply false
}
