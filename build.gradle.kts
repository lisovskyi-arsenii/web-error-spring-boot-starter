plugins {
    id("java-library")
    id("io.spring.dependency-management") version "1.1.7"
    id("maven-publish")
}

repositories {
    mavenCentral()
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:4.1.0")
    }
}

val lombokVersion = "1.18.46"

dependencies {
    api("org.springframework.boot:spring-boot-starter-web")
    api("org.springframework.boot:spring-boot-starter-validation")

    compileOnly("org.springframework.boot:spring-boot-autoconfigure")
    compileOnly("org.springframework.boot:spring-boot-starter-security")
    compileOnly("org.projectlombok:lombok:$lombokVersion")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.projectlombok:lombok:$lombokVersion")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            groupId = "com.lisovskyi"
            artifactId = "lisovskyi-web-error-starter"
        }
    }

    repositories {
        mavenLocal()
    }
}

// BOM-managed deps have no explicit version in the POM — suppress the Gradle metadata validation.
tasks.named<GenerateModuleMetadata>("generateMetadataFileForMavenPublication") {
    suppressedValidationErrors.add("dependencies-without-versions")
}

// Spring Dependency Management causes Configuration Cache issues on publish: https://github.com/spring-gradle-plugins/dependency-management-plugin/issues/327
tasks.withType<GenerateMavenPom>().configureEach {
    notCompatibleWithConfigurationCache("Spring Dependency Management plugin is not compatible with Configuration Cache for POM generation")
}
