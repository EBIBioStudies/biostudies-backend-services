package ac.uk.ebi.biostd.client.integration.web

import ac.uk.ebi.biostd.client.exception.bioWebClientErrorHandler
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.http.codec.ClientCodecConfigurer
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.DefaultUriBuilderFactory
import reactor.netty.http.client.HttpClient

internal fun webClientBuilder(baseUrl: String): WebClient.Builder {
    val exchangeStrategies =
        ExchangeStrategies.builder().codecs { configurer ->
            if (configurer is ClientCodecConfigurer) {
                configurer.defaultCodecs().maxInMemorySize(-1)
            }
        }.build()

    val httpClient =
        HttpClient.create().doOnConnected { connection ->
            connection.addHandlerLast(ReadTimeoutHandler(0))
            connection.addHandlerLast(WriteTimeoutHandler(0))
        }

    return WebClient.builder()
        .uriBuilderFactory(DefaultUriBuilderFactory(baseUrl))
        .clientConnector(ReactorClientHttpConnector(httpClient))
        .filter(bioWebClientErrorHandler())
        .exchangeStrategies(exchangeStrategies)
}
