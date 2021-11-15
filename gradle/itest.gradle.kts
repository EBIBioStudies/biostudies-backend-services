val sourceSets = the<SourceSetContainer>()

sourceSets {
    create("itest") {
        java.srcDir(file("src/itest/java"))
        resources.srcDir(file("src/itest/resources"))
        compileClasspath += sourceSets["main"].output + configurations["testRuntimeClasspath"]
        runtimeClasspath += output + sourceSets["main"].output + compileClasspath
    }
}

val copySqlSchema = tasks.create<Copy>("copySqlSchema") {
    from("$rootDir/infrastructure/src/main/resources/setup/mysql/Schema.sql")
    into("$buildDir/resources/itest")
}

val itest = tasks.create<Test>("itest") {
    dependsOn(copySqlSchema)
    testClassesDirs = sourceSets["itest"].output.classesDirs
    classpath = sourceSets["itest"].runtimeClasspath

    val testingMode = project.property("itest.mode")
    println("##### Running integration tests in $testingMode mode #######")
    systemProperty("itest.mode", testingMode)

    val enableFire = project.property("enableFire")
    println("##### Running integration tests with fireEnable=$enableFire mode #######")
    systemProperty("enableFire", enableFire)

    useJUnitPlatform()
    extensions.configure(JacocoTaskExtension::class) {
        setDestinationFile(file("$buildDir/jacoco/jacocoITest.exec"))
        classDumpDir = file("$buildDir/jacoco/classpathdumps")
    }
}

tasks.getByName<JacocoCoverageVerification>("jacocoTestCoverageVerification") { dependsOn(itest) }
