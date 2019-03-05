package ac.uk.ebi.pmc.client

import kotlinx.coroutines.Deferred
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Path

interface PmcApi {

    @GET("/articles/PMC{studyId}/bin/{file}")
    fun downloadFileAsync(@Path("studyId") studyId: String, @Path("file") fileName: String): Deferred<ResponseBody>
}
