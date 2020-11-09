package ac.uk.ebi.pmc.config

import ac.uk.ebi.pmc.client.PmcApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level.BASIC
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

@Configuration
class WebConfig {

    @Bean
    fun pmcApi(): PmcApi {
        return Retrofit.Builder()
            .baseUrl("http://www.ft-loading.europepmc.org")
            .client(httpClient())
            .build()
            .create(PmcApi::class.java)
    }

    private fun httpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().setLevel(BASIC))
            .readTimeout(1, TimeUnit.MINUTES)
            .writeTimeout(1, TimeUnit.MINUTES)
            .build()
    }
}
