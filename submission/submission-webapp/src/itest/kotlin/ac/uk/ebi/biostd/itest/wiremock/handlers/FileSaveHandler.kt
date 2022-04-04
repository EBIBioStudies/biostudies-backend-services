package ac.uk.ebi.biostd.itest.wiremock.handlers

import ac.uk.ebi.biostd.itest.wiremock.FireMockDatabase
import com.github.tomakehurst.wiremock.http.Request
import com.github.tomakehurst.wiremock.http.RequestMethod
import com.github.tomakehurst.wiremock.http.ResponseDefinition

class FileSaveHandler(private val fireDB: FireMockDatabase) : RequestHandler {

    override val requestMethod: RequestMethod = RequestMethod.POST
    override val urlPattern: Regex = "/fire/objects".toRegex()

    override fun handle(rqt: Request): ResponseDefinition =
        ResponseDefinition.okForJson(fireDB.saveFile(fileName(rqt), rqt.parts.first().body.asBytes()))


    private fun fileName(request: Request): String {
        val string = request.getPart("file").headers.getHeader("content-disposition").values().first()
        return string.substringAfter("filename=\"").substringBefore("\"")
    }
}
