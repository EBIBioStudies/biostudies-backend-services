package ac.uk.ebi.pmc.config

import ac.uk.ebi.pmc.client.PmcApi
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import okhttp3.Interceptor
import okhttp3.Interceptor.Chain
import okhttp3.OkHttpClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

private val SERVERS = listOf(
    "ves-pg-a6.ebi.ac.uk",
    "ves-pg-a7.ebi.ac.uk",
    "ves-oy-a6.ebi.ac.uk",
    "ves-oy-a7.ebi.ac.uk")

@Configuration
class WebConfig {

    @Bean
    fun pmcApi(): PmcApi {
        return Retrofit.Builder()
            .baseUrl("http://ves-pg-a6.ebi.ac.uk")
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .client(httpClient())
            .build()
            .create(PmcApi::class.java)
    }

    private fun httpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HostSelectionInterceptor())
            .readTimeout(1, TimeUnit.MINUTES)
            .writeTimeout(1, TimeUnit.MINUTES)
            .build()
    }

    private class HostSelectionInterceptor : Interceptor {

        private val servers = RoundRobinIterable(SERVERS).iterator()

        override fun intercept(chain: Chain): okhttp3.Response {
            var request = chain.request()
            val newUrl = request.url().newBuilder()
                .host(servers.next())
                .build()
            request = request.newBuilder()
                .url(newUrl)
                .build()
            return chain.proceed(request)
        }
    }

    private class RoundRobinIterable(private val source: List<String>) : Iterable<String> {
        override fun iterator(): Iterator<String> {
            return object : Iterator<String> {

                private var index = 0

                override fun hasNext() = true

                override fun next(): String {
                    val res = source[index]
                    index = (index + 1) % source.size
                    return res
                }
            }
        }
    }
}
