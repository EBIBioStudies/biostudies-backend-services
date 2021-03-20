package ac.uk.ebi.pmc.client

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PmcApi {

    @GET("/files/getFile/PMC{studyId}")
    suspend fun downloadFileAsync(@Path("studyId") studyId: String, @Query("filename") fileName: String): ResponseBody

    @GET("/files/getFileStream/PMC{studyId}")
    suspend fun downloadFileStream(@Path("studyId") studyId: String, @Query("filename") fileName: String): ResponseBody
}
