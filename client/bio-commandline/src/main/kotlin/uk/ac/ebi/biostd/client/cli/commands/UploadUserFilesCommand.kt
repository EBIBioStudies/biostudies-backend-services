package uk.ac.ebi.biostd.client.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import uk.ac.ebi.biostd.client.cli.common.CommonParameters.PASSWORD_HELP
import uk.ac.ebi.biostd.client.cli.common.CommonParameters.SERVER_HELP
import uk.ac.ebi.biostd.client.cli.common.CommonParameters.USER_HELP
import uk.ac.ebi.biostd.client.cli.common.UploadUserFilesParameters.DATAMOVER_HELP
import uk.ac.ebi.biostd.client.cli.common.UploadUserFilesParameters.FILE_HELP
import uk.ac.ebi.biostd.client.cli.common.UploadUserFilesParameters.REL_PATH_HELP
import uk.ac.ebi.biostd.client.cli.dto.SecurityConfig
import uk.ac.ebi.biostd.client.cli.services.UserFilesService
import java.io.File

internal class UploadUserFilesCommand(
    private val userFilesService: UserFilesService,
) : CliktCommand(name = "uploadUserFiles") {
    private val server by option("-s", "--server", help = SERVER_HELP).required()
    private val user by option("-u", "--user", help = USER_HELP).required()
    private val password by option("-p", "--password", help = PASSWORD_HELP).required()
    private val file by option("-f", "--file", help = FILE_HELP).required()
    private val relPath by option("-rp", "--relPath", help = REL_PATH_HELP).default("")
    private val datamover by option("--datamover", help = DATAMOVER_HELP).flag(default = false)

    override fun run() =
        runBlocking {
            val config = SecurityConfig(server, user, password)
            if (datamover) datamoverUpload(config) else userFilesService.httpUpload(config, File(file), relPath)
            echo("SUCCESS: User files uploaded")
        }

    private suspend fun datamoverUpload(securityConfig: SecurityConfig) {
        runCatching {
            userFilesService.datamoverUpload(securityConfig, File(file), relPath)
        }.getOrElse { error ->
            if (error !is Exception || error is CancellationException) throw error
            throw CliktError(error.message ?: "Datamover upload failed", error)
        }
    }
}
