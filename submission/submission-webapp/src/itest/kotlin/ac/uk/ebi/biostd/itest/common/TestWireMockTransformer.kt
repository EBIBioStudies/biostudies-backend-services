package ac.uk.ebi.biostd.itest.common

import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.common.FileSource
import com.github.tomakehurst.wiremock.extension.Parameters
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer
import com.github.tomakehurst.wiremock.http.Request
import com.github.tomakehurst.wiremock.http.ResponseDefinition
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.io.ext.createNewFile
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.size
import org.bson.types.ObjectId
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
        val subFilesFolder = submissionFolder(request.getHeader("sub-relpath"))
        val file = subFilesFolder.createNewFile(getFileName(request), request.parts.first().body.asString())

        return okJson(
            jsonObj {
                "objectId" to ObjectId.get().timestamp
                "fireOid" to ObjectId.get().toString()
                "objectMd5" to file.md5()
                "objectSize" to file.size()
                "createTime" to Instant.now().toString()
                "filesystemEntry" to null
            }.toString()
        ).build()
    }

    private fun submissionFolder(relPath: String): File {
        val filesPath = folder.resolve(relPath).resolve("Files")
        filesPath.mkdirs()
        return filesPath
    }

    private fun getFileName(request: Request): String {
        val string = request.getPart("file").headers.getHeader("content-disposition").values().first()
        return string.substringAfter("filename=\"").substringBefore("\"")
    }
}
