plugins {
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.dependency.management)
    alias(libs.plugins.openapi.generator)
	  java
}

group = "org.openhospital"
version = "0.0.1-SNAPSHOT"
description = "SmartDoc Document Management API"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(libs.versions.java.get())
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
    implementation(libs.bundles.spring.boot)
    implementation(libs.bundles.extra.libs)

    compileOnly(libs.bundles.compile)
    annotationProcessor(libs.bundles.annotation.processors)
    testImplementation(libs.bundles.test)
    testRuntimeOnly(libs.bundles.test.runtime)
}

openApiGenerate {
    generatorName = "spring"
    packageName.set("org.openhospital.smartdoc.openapi")
    inputSpec = "${projectDir}/src/main/resources/static/openapi.yaml"
    globalProperties.set(mapOf("models" to ""))

    configOptions.putAll(
        mapOf(
            "useSpringBoot3"        to "true",
            "useJakartaEe"          to "true",
            "dateLibrary"           to "java8",
            "useTags"               to "true",
            "interfaceOnly"          to "true",
            "sourceFolder" to "src/main/kotlin"
        )
    )
}

sourceSets {
    main {
        java.srcDir(project.layout.buildDirectory.dir("generate-resources/main/src/main/java"))
    }
}

tasks.compileJava {
    dependsOn("openApiGenerate")
    options.compilerArgs.addAll(listOf("-Amapstruct.defaultComponentModel=spring"))
}

tasks.withType<Test> {
	useJUnitPlatform()
}
