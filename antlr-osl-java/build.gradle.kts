plugins {
    kotlin("jvm")
    antlr
}

dependencies {
    antlr(libs.antlr4)
}

tasks.generateGrammarSource {
    arguments.addAll(listOf("-visitor", "-package", "info.spiralframework.antlr.osl"))
    outputDirectory = file("$buildDir/generated-src/antlr/main/info/spiralframework/antlr/osl")
}

tasks.compileKotlin {
    dependsOn("generateGrammarSource")
}