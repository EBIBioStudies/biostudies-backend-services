import PluginVersions.DetektVersion

object PluginVersions {
    const val DetektVersion = "1.23.0"
    const val KotlinPluginVersion = Versions.KotlinVersion
    const val KtLintVersion = "10.2.1"
    const val ShadowPluginVersion = "8.1.1"
    const val SshVersion = "2.10.1"
    const val SpringBootPluginVersion = "2.7.1"
    const val SpringDependencyManagementPluginVersion = "1.0.12.RELEASE"
    const val GradleRetryVersion = "1.5.5"
}

object Plugins {
    const val DetektFormattingPlugin = "io.gitlab.arturbosch.detekt:detekt-formatting:$DetektVersion"
    const val JacocoPlugin = "jacoco"
    const val KotlinPlugin = "org.jetbrains.kotlin.jvm"
    const val KotlinAllOpenPlugin = "org.jetbrains.kotlin.plugin.allopen"
    const val KotlinJpaPlugin = "org.jetbrains.kotlin.plugin.jpa"
    const val KotlinSpringPlugin = "org.jetbrains.kotlin.plugin.spring"
    const val ShadowPlugin = "com.github.johnrengelman.shadow"
    const val SshPlugin = "org.hidetake.ssh"
    const val SpringBootPlugin = "org.springframework.boot"
    const val SpringDependencyManagementPlugin = "io.spring.dependency-management"
    const val GradleRetry = "org.gradle.test-retry"
}
