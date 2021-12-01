import Dependencies.Arrow
import Projects.CommonsModelExtended

dependencies {
    api(project(CommonsModelExtended))
    implementation(Arrow)

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}
