import Dependencies.Guava
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies

dependencies {
    compile(project(":commons:commons-util"))
    compile(project(":commons:commons-bio"))
    compile(Guava)

    testCompile(project(":commons:commons-test"))
    BaseTestCompileDependencies.forEach { testCompile(it) }
    BaseTestRuntimeDependencies.forEach { testCompile(it) }
}
