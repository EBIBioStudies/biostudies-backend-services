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
import ebi.ac.uk.dsl.json.jsonArray
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.size
import org.bson.types.ObjectId
import org.springframework.http.HttpStatus.BAD_REQUEST
import java.io.File
import java.time.Instant

class TestWireMockTransformer(
    private val folder: File
) : ResponseDefinitionTransformer() {
    override fun getName(): String = "testWireMockTransformer"

    override fun transform(
        request: Request,
        responseDefinition: ResponseDefinition?,
        files: FileSource?,
        parameters: Parameters?
    ): ResponseDefinition {
        when (request.method) {
            POST -> {
                val objectId = ObjectId.get().timestamp
                val file = folder.resolve(objectId.toString())
                file.createNewFile()
                file.writeBytes(request.parts.first().body.asBytes())

                return okJson(
                    jsonObj {
                        "objectId" to objectId
                        "fireOid" to "fireOid-${getFileName(request)}"
                        "objectMd5" to file.md5()
                        "objectSize" to file.size()
                        "createTime" to Instant.now().toString()
                        "filesystemEntry" to null
                    }.toString()
                ).build()
            }
            GET -> return okJson(jsonArray().toString()).build()
            PUT -> {
                val objectId = parameters!!["fireId"]
                val relPath = folder.resolve(request.getHeader("x-fire-path"))
                val file = folder.resolve(objectId.toString())
                relPath.parentFile.mkdirs()
                file.renameTo(relPath)

                return ResponseDefinition()
            }
            DELETE -> return ResponseDefinition()
            else -> throw WebClientException(BAD_REQUEST, "http method ${request.method.name} is not supported")
        }
    }

    private fun getFileName(request: Request): String {
        val string = request.getPart("file").headers.getHeader("content-disposition").values().first()
        return string.substringAfter("filename=\"").substringBefore("\"")
    }
}
