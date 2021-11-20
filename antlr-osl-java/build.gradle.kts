plugins {
    kotlin("jvm")
    antlr
}

dependencies {
    antlr("org.antlr:antlr4:4.7.2")
}

tasks.generateGrammarSource {
    arguments.addAll(listOf("-visitor", "-package", "info.spiralframework.antlr.osl"))
    outputDirectory = file("$buildDir/generated-src/antlr/main/info/spiralframework/antlr/osl")
}

tasks.compileKotlin {
    dependsOn("generateGrammarSource")
}