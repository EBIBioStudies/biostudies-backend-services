package ebi.ac.uk.commons.http.servlet

import org.springframework.http.HttpStatus
import java.io.OutputStream
import javax.servlet.http.HttpServletResponse

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
