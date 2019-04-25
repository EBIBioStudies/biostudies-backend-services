import Dependencies.SpringWeb

dependencies {
    compile(project(":commons:commons-util"))
    compile(project(":commons:commons-bio"))
    compile(project(":commons:commons-serialization"))

    compile(SpringWeb)
}
