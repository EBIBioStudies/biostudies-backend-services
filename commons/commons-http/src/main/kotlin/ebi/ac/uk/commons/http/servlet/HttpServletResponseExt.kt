package ebi.ac.uk.commons.http.servlet

import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import java.io.OutputStream

/**
 * Set response status execute OutputStream consumer before closed it.
 */
fun HttpServletResponse.write(
    httpStatus: HttpStatus,
    writer: (OutputStream) -> Any,
) {
    status = httpStatus.value()
    outputStream.also { writer(it) }.flush()
}
