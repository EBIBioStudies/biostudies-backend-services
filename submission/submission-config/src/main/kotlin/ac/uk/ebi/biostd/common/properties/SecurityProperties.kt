package ac.uk.ebi.biostd.common.properties

import org.springframework.boot.context.properties.NestedConfigurationProperty

data class SecurityProperties(
    val captchaKey: String = "",
    val checkCaptcha: Boolean = false,
    val tokenHash: String,
    val environment: String,
    val requireActivation: Boolean = false,

    @NestedConfigurationProperty
    val instanceKeys: InstanceKeys = InstanceKeys(),
    val filesProperties: FilesProperties,
)

enum class MagicFolderType {
    FTP, NFS
}

data class FilesProperties(
    val defaultMode: MagicFolderType,
    val filesDirPath: String,
    val magicDirPath: String,
)

data class InstanceKeys(
    val dev: String = "",
    val beta: String = "",
    val prod: String = "",
)
