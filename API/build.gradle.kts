plugins {
    id("java-library")
    id("java")
}

repositories {
    mavenCentral()
}

dependencies {
    api("jakarta.annotation:jakarta.annotation-api:3.0.0")
    compileOnly("net.kyori:adventure-platform-bungeecord:4.4.1")
    compileOnly("com.google.guava:guava:33.5.0-jre")

    testImplementation(platform("org.junit:junit-bom:6.0.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-parameters")
    options.compilerArgs.addAll(listOf("-Xlint:deprecation", "-Xlint:unchecked"))
}

java {
    withSourcesJar()
    withJavadocJar()
}