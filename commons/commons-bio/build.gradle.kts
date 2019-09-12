import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies

dependencies {
    compile(project(":commons:commons-util"))

    BaseTestCompileDependencies.forEach { testCompile(it) }
    BaseTestRuntimeDependencies.forEach { testCompile(it) }
}
