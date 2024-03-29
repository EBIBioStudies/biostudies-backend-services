import java.math.BigDecimal.ZERO

apply(plugin = "java")
apply(plugin = "jacoco")

configure<JavaPluginExtension> {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

configure<JacocoPluginExtension> {
    toolVersion = "0.8.7"
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    finalizedBy(tasks.named("jacocoTestReport"), verifyCoverage)
    extensions.configure(JacocoTaskExtension::class) {
        setDestinationFile(file("$buildDir/jacoco/jacocoTest.exec"))
        classDumpDir = file("$buildDir/jacoco/classpathdumps")
    }
}

tasks.named<JacocoReport>("jacocoTestReport") {
    executionData.setFrom(fileTree("$buildDir/jacoco") { include("*.exec") })
    reports {
        xml.isEnabled = false
        csv.isEnabled = false
        html.destination = file("$buildDir/reports/jacocoHtml")
    }
}

val coverage: String? by project

val verifyCoverage = tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
    executionData.setFrom(fileTree("$buildDir/jacoco") { include("*.exec") })
    dependsOn(tasks.named("test"))
    violationRules {
        rule { limit { counter = "LINE"; value = "COVEREDRATIO"; minimum = coverage?.toBigDecimal() ?: ZERO } }
    }
}
