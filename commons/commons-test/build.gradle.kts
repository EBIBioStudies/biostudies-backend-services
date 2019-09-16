import Dependencies.Arrow
import Dependencies.KotlinStdLib
import TestDependencies.AssertJ
import TestDependencies.JunitExtensions

dependencies {
    compile(project(":commons:commons-bio"))

    compile(Arrow)
    compile(AssertJ)
    compile(JunitExtensions)
    compile(KotlinStdLib)
}
