package ac.uk.ebi.biostd.common.properties

import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.NestedConfigurationProperty

data class SecurityProperties(
    val captchaKey: String = "",
    val checkCaptcha: Boolean = false,
    val tokenHash: String,
    val environment: String,
    val requireActivation: Boolean = false,
    val preventFileDeletion: Boolean,
    @NestedConfigurationProperty
    val instanceKeys: InstanceKeys = InstanceKeys(),
    val filesProperties: FilesProperties,
)

enum class StorageMode {
    FTP,
    NFS,
}

@ConstructorBinding
data class FilesProperties(
    val defaultMode: StorageMode,
    val filesDirPath: String,
    val magicDirPath: String,
    @NestedConfigurationProperty
    val ftp: FtpProperties,
)

data class FtpProperties(
    val ftpRootPath: String,
    val ftpDirPath: String,
    val ftpUser: String,
    val ftpPassword: String,
    val ftpUrl: String,
    val ftpPort: Int,
    val defaultTimeout: Long,
    val connectionTimeout: Long,
)

data class InstanceKeys(
    val dev: String = "",
    val beta: String = "",
    val prod: String = "",
)
