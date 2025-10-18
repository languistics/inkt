plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(project(":"))
}

val javaLanguageVersion: String by project
kotlin {
    explicitApiWarning()
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(javaLanguageVersion))
    }
}