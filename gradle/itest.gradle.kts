val sourceSets = the<SourceSetContainer>()

sourceSets {
    create("itest") {
        java.srcDir(file("src/integTest/java"))
        resources.srcDir(file("src/integTest/resources"))
        compileClasspath += sourceSets["main"].output + configurations["testRuntimeClasspath"]
        runtimeClasspath += output + sourceSets["main"].output + compileClasspath
    }
}

val itest = tasks.create<Test>("itest") {
    testClassesDirs = sourceSets["itest"].output.classesDirs
    classpath = sourceSets["itest"].runtimeClasspath

    useJUnitPlatform()
    extensions.configure(JacocoTaskExtension::class) {
        setDestinationFile(file("$buildDir/jacoco/jacocoITest.exec"))
        classDumpDir = file("$buildDir/jacoco/classpathdumps")
    }
}

tasks.getByName<JacocoCoverageVerification>("jacocoTestCoverageVerification") { dependsOn(itest) }
