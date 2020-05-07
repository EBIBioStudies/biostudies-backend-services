package ebi.ac.uk.api

import java.net.HttpURLConnection

class ClientResponse<T>(val body: T, val statusCode: Int) {

    companion object {
        fun <T> ok(value: T) = ClientResponse(value, HttpURLConnection.HTTP_OK)
    }
}
