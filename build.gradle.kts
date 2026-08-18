plugins {
    id("java")
}

group = "ac.ghost"
version = "1.0.2"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(files("Nukkit-MOT-SNAPSHOT.jar"))
    testCompileOnly(files("Nukkit-MOT-SNAPSHOT.jar"))
    testRuntimeOnly(files("Nukkit-MOT-SNAPSHOT.jar"))

    compileOnly("org.projectlombok:lombok:1.18.40")
    annotationProcessor("org.projectlombok:lombok:1.18.40")

    implementation("it.unimi.dsi:fastutil:8.5.15")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.jar {
    archiveFileName.set("Ghost-${project.version}.jar")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from("THIRD_PARTY_NOTICES.md") {
        into("META-INF")
    }
    from({
        configurations.runtimeClasspath.get().filter {
            !it.name.equals("Nukkit-MOT-SNAPSHOT.jar", ignoreCase = true)
        }.map {
            if (it.isDirectory) it else zipTree(it)
        }
    })
}

tasks.test {
    useJUnitPlatform()
}
