import Dependencies.Jsch
import Dependencies.KotlinStdLib
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies

dependencies {
    compile(KotlinStdLib)
    compile(project(":commons:commons-util"))
    compile(Jsch)

    BaseTestCompileDependencies.forEach { testCompile(it) }
    BaseTestRuntimeDependencies.forEach { testCompile(it) }
}
