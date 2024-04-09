import java.math.BigDecimal.ZERO

apply(plugin = "java")
apply(plugin = "jacoco")

configure<JavaPluginExtension> {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

configure<JacocoPluginExtension> {
    toolVersion = "0.8.11"
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    finalizedBy(tasks.named("jacocoTestReport"), verifyCoverage)
    extensions.configure(JacocoTaskExtension::class) {
        setDestinationFile(file("${project.layout.buildDirectory.get()}/jacoco/jacocoTest.exec"))
        classDumpDir = file("${project.layout.buildDirectory.get()}/jacoco/classpathdumps")
    }
}

tasks.named<JacocoReport>("jacocoTestReport") {
    executionData.setFrom(fileTree("${project.layout.buildDirectory.get()}/jacoco") { include("*.exec") })
    reports {
        xml.required = false
        csv.required = false
        html.outputLocation = file("${project.layout.buildDirectory.get()}/reports/jacocoHtml")
    }
}

val coverage: String? by project

val verifyCoverage =
    tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
        executionData.setFrom(fileTree("${project.layout.buildDirectory.get()}/jacoco") { include("*.exec") })
        dependsOn(tasks.named("test"))
        violationRules {
            rule {
                limit {
                    counter = "LINE"
                    value = "COVEREDRATIO"
                    minimum = coverage?.toBigDecimal() ?: ZERO
                }
            }
        }
    }
