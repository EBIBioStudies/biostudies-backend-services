import PluginVersions.DetektVersion

object PluginVersions {
    const val DetektVersion = "1.19.0"
    const val GitPropertiesVersion = "2.4.0-rc1"
    const val KotlinPluginVersion = "1.6.10"
    const val KtLintVersion = "10.2.1"
    const val ShadowPluginVersion = "5.0.0"
    const val SshVersion = "2.10.1"
    const val SpringBootPluginVersion = "2.7.1"
    const val SpringDependencyManagementPluginVersion = "1.0.12.RELEASE"
}

object Plugins {
    const val DetektFormattingPlugin = "io.gitlab.arturbosch.detekt:detekt-formatting:$DetektVersion"
    const val DetektPlugin = "io.gitlab.arturbosch.detekt"
    const val GitProperties = "com.gorylenko.gradle-git-properties"
    const val JacocoPlugin = "jacoco"
    const val KotlinPlugin = "org.jetbrains.kotlin.jvm"
    const val KotlinAllOpenPlugin = "org.jetbrains.kotlin.plugin.allopen"
    const val KotlinJpaPlugin = "org.jetbrains.kotlin.plugin.jpa"
    const val KotlinSpringPlugin = "org.jetbrains.kotlin.plugin.spring"
    const val KtLintPlugin = "org.jlleitschuh.gradle.ktlint"
    const val ShadowPlugin = "com.github.johnrengelman.shadow"
    const val SshPlugin = "org.hidetake.ssh"
    const val SpringBootPlugin = "org.springframework.boot"
    const val SpringDependencyManagementPlugin = "io.spring.dependency-management"
}
