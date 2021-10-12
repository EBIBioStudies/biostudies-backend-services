package ac.uk.ebi.biostd.itest.common

import ac.uk.ebi.biostd.client.exception.WebClientException
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.common.FileSource
import com.github.tomakehurst.wiremock.extension.Parameters
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer
import com.github.tomakehurst.wiremock.http.Request
import com.github.tomakehurst.wiremock.http.RequestMethod.DELETE
import com.github.tomakehurst.wiremock.http.RequestMethod.GET
import com.github.tomakehurst.wiremock.http.RequestMethod.POST
import com.github.tomakehurst.wiremock.http.RequestMethod.PUT
import com.github.tomakehurst.wiremock.http.ResponseDefinition
import ebi.ac.uk.dsl.file
import ebi.ac.uk.dsl.json.jsonArray
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.io.ext.createNewFile
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.notExist
import ebi.ac.uk.io.ext.size
import org.bson.types.ObjectId
import org.springframework.http.HttpStatus.BAD_REQUEST
import java.io.File
import java.time.Instant

class TestWireMockTransformer(private val folder: File) : ResponseDefinitionTransformer() {
    override fun getName(): String = "testWireMockTransformer"

    override fun transform(
        request: Request,
        responseDefinition: ResponseDefinition?,
        files: FileSource?,
        parameters: Parameters?
    ): ResponseDefinition {
        when (request.method) {
            POST -> {
                val subFilesFolder = submissionFolder(request.getHeader("sub-relpath"))
                val fileName = getFileName(request)
                val file: File = if (fileName.substringBefore(".") == subFilesFolder.parentFile.name) {
                    subFilesFolder.parentFile.createNewFile(fileName, request.parts.first().body.asString())
                } else {
                    subFilesFolder.createNewFile(fileName, request.parts.first().body.asString())
                }

                return okJson(
                    jsonObj {
                        "objectId" to ObjectId.get().timestamp
                        "fireOid" to fileName
                        "objectMd5" to file.md5()
                        "objectSize" to file.size()
                        "createTime" to Instant.now().toString()
                        "filesystemEntry" to null
                    }.toString()
                ).build()
            }
            GET -> {
                return okJson(jsonArray().toString()).build()
            }
            PUT -> {
                return ResponseDefinition()
            }
            DELETE -> {
                return ResponseDefinition()
            }
            else -> throw WebClientException(BAD_REQUEST, "http method ${request.method.name} is not supported")
        }
    }

    private fun submissionFolder(relPath: String): File {
        return if (relPath.endsWith("Files").not()) {
            val filesPath = folder.resolve(relPath).resolve("Files")
            filesPath.mkdirs()
            filesPath
        } else {
            val subFile = folder.resolve(relPath)
            if (subFile.notExist()) subFile.mkdirs()
            subFile
        }
    }

    private fun getFileName(request: Request): String {
        val string = request.getPart("file").headers.getHeader("content-disposition").values().first()
        return string.substringAfter("filename=\"").substringBefore("\"")
    }
}
