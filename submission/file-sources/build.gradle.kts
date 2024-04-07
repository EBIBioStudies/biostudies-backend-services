import Dependencies.CommonsNet
import Projects.CommonsBio
import Projects.CommonsModelExtended
import Projects.CommonsModelExtendedMapping
import Projects.CommonsTest
import Projects.FireWebClient
import Projects.FtpWebClient
import Projects.SubmissionPersistenceCommonApi
import Projects.SubmissionSecurity
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies

plugins {
    id(Plugins.SpringBootPlugin) version PluginVersions.SpringBootPluginVersion apply false
    id(Plugins.SpringDependencyManagementPlugin) version PluginVersions.SpringDependencyManagementPluginVersion
}

dependencies {
    api(project(CommonsBio))
    api(project(CommonsModelExtended))
    api(project(CommonsModelExtendedMapping))
    api(project(FireWebClient))
    api(project(FtpWebClient))
    api(project(SubmissionSecurity))
    api(project(SubmissionPersistenceCommonApi))
    implementation(CommonsNet)

    testApi(project(CommonsTest))
    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
}
