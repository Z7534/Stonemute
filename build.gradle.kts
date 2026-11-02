plugins {
    id("java-library")
    id("maven-publish")
    id("com.gradleup.shadow") version "9.6.1"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.lucko.me/")
    maven("https://maven.maxhenkel.de/repository/public")
    maven("https://repo.codemc.org/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.2.build.+")
    compileOnly("de.maxhenkel.voicechat:voicechat-api:2.6.24")
    compileOnly("litebans:api:0.6.1")
    compileOnly("net.luckperms:api:5.4")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-XDstringConcat=inline")
}

tasks {
    processResources {
        val props = mapOf("version" to version, "description" to project.description)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }

    jar {
        enabled = false
    }

    shadowJar {
        archiveClassifier.set("")
    }

    build {
        dependsOn(shadowJar)
    }
}
