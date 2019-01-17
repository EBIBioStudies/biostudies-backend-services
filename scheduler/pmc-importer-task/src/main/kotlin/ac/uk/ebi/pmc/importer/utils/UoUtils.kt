package ac.uk.ebi.pmc.importer.utils

import java.io.StringWriter
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousFileChannel
import java.nio.channels.CompletionHandler
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

suspend fun fileToString(path: Path): String {
    val buffer = ByteBuffer.allocate(1024 * 4)
    val content = StringWriter()
    var position = 0

    val fileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.READ)
    var read = fileChannel.aRead(buffer)
    while (read > 0) {
        buffer.flip()
        content.append(StandardCharsets.UTF_8.decode(buffer))

        position += buffer.limit()
        buffer.clear()
        read = fileChannel.aRead(buffer, position)
    }

    fileChannel.close()
    return content.toString()
}

suspend fun AsynchronousFileChannel.aRead(buf: ByteBuffer, position: Int = 0): Int =
    suspendCoroutine { cont ->
        read(buf, position.toLong(), Unit, object : CompletionHandler<Int, Unit> {
            override fun completed(bytesRead: Int, attachment: Unit) = cont.resume(bytesRead)
            override fun failed(exception: Throwable, attachment: Unit) = cont.resumeWithException(exception)
        })
    }