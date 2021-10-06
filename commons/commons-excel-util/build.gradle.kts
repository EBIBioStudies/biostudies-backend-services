import Dependencies.PoiOxml
import Projects.TsvLibrary
import TestDependencies.AssertJ
import TestDependencies.Junit
import TestDependencies.JunitExtensions

dependencies {
    api(project(TsvLibrary))

    implementation(PoiOxml)

    testImplementation(Junit)
    testImplementation(JunitExtensions)
    testImplementation(AssertJ)
}
