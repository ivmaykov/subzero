/*
 * This file was generated by the Gradle "init" task.
 */

import com.github.jengelman.gradle.plugins.shadow.ShadowExtension
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.squareup.subzero.java-conventions")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

dependencies {
    implementation(project(":proto"))
    implementation(project(":shared"))
    implementation("com.google.zxing:javase:3.5.0")
    implementation("org.yaml:snakeyaml:1.33")
    implementation("com.google.protobuf:protobuf-java-util:3.21.7")
    implementation("com.google.protobuf:protobuf-java:3.21.5")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.assertj:assertj-core:3.23.1")
    testImplementation("org.mockito:mockito-junit-jupiter:4.8.0")
    compileOnly(project(":fake_ncipher"))
}

description = "gui"

tasks {
    // Custom task to copy fake_ncipher jar so GUI works in dev mode
    register("copyFakeNCipherJar", Copy::class) {
        from(project(":fake_ncipher").tasks["jar"])
        into("build/libs")
    }

    // Disable non-shadow jar generation
    named<Jar>("jar") {
        enabled = false
    }

    // Configure shadow jar generation
    named<ShadowJar>("shadowJar") {
        enabled = true
        dependsOn(":gui:copyFakeNCipherJar")
        mergeServiceFiles()
        archiveClassifier.set("shaded")
        manifest {
            attributes["Main-Class"] = "com.squareup.subzero.SubzeroGui"
            attributes["Class-Path"] = "/opt/nfast/java/classes/nCipherKM.jar fake_ncipher-${version}.jar"
        }
        // signatures from foreign jars are bad news
        exclude("META-INF/*.SF")
        exclude("META-INF/*.DSA")
        exclude("META-INF/*.RSA")
    }

    // Make sure "gradle build" generates the shadow jar
    named("assemble") {
        dependsOn(":gui:shadowJar")
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            project.extensions.configure<ShadowExtension>() {
                component(this@create)
            }
        }
    }
}