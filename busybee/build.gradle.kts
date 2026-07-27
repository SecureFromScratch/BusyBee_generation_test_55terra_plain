plugins {
	java
	id("org.springframework.boot") version "4.0.6"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.securefromscratch"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

sourceSets.configureEach {
	java {
		exclude("the_road_to_success/**")
		exclude("**/the_road_to_success/**")
	}
}

tasks.withType<JavaCompile>().configureEach {
	exclude("the_road_to_success/**")
	exclude("**/the_road_to_success/**")
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-jooq")
	implementation("org.springframework.boot:spring-boot-flyway")
	implementation("org.springframework.vault:spring-vault-core:4.0.3")
	implementation("org.flywaydb:flyway-mysql")
	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8")
	implementation("io.github.owasp-untrust:untrust-boxedpath:0.3")
	implementation("org.jsoup:jsoup:1.21.2")
	runtimeOnly("com.mysql:mysql-connector-j")
	implementation("com.google.guava:guava:33.6.0-jre")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testImplementation("org.springframework.security:spring-security-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.named<Test>("test") {
	dependsOn(gradle.includedBuild("tesseract_mock_java").task(":installDist"))
}
