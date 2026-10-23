package uk.ac.ebi.biostd.client.cli.services

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.FileTime
import java.nio.file.attribute.PosixFileAttributes
import java.nio.file.attribute.PosixFilePermissions
import kotlin.io.path.createDirectories
import kotlin.io.path.createDirectory
import kotlin.io.path.readBytes
import kotlin.io.path.readText
import kotlin.io.path.writeBytes
import kotlin.io.path.writeText

internal class DataMoverTransferTest {
    @TempDir
    lateinit var folder: Path

    @BeforeEach
    fun requireGnuRsync() {
        val version =
            runCatching {
                val process = ProcessBuilder("rsync", "--version").redirectErrorStream(true).start()
                process.inputStream.bufferedReader().readText().also { process.waitFor() }
            }.getOrDefault("")
        assumeTrue(Regex("^rsync\\s+version\\s+\\d").containsMatchIn(version), "Transfer tests require GNU rsync on PATH")
    }

    @Test
    fun `flatten nested files while preserving filenames and HTTP permissions`() {
        val source = folder.resolve("source ' with ${'$'} characters").createDirectory()
        source.resolve("root.txt").writeText("root")
        val nested = source.resolve("nested").createDirectory()
        val name = "a ' quoted \" ${'$'}(echo wrong) `echo wrong`\nfile.txt"
        nested.resolve(name).writeText("nested")

        val (status, logs) = execute(source)
        val target = folder.resolve("home/incoming")

        assertThat(status).withFailMessage(logs).isZero()
        assertThat(target.resolve("root.txt").readText()).isEqualTo("root")
        assertThat(target.resolve(name).readText()).isEqualTo("nested")
        assertThat(target.resolve("nested")).doesNotExist()
        assertThat(Files.getPosixFilePermissions(target.resolve(name))).isEqualTo(PosixFilePermissions.fromString("rw-rw----"))
        assertThat(Files.getPosixFilePermissions(target)).isEqualTo(PosixFilePermissions.fromString("rwxrwx---"))
    }

    @Test
    fun `overwrite existing file even with matching size and modification time`() {
        val source = folder.resolve("data.txt")
        source.writeText("new-data")
        val target = folder.resolve("home/incoming").createDirectories().resolve("data.txt")
        target.writeText("old-data")
        val timestamp = FileTime.fromMillis(1700000000000)
        Files.setLastModifiedTime(source, timestamp)
        Files.setLastModifiedTime(target, timestamp)

        val (status, logs) = execute(source)

        assertThat(status).withFailMessage(logs).isZero()
        assertThat(target.readText()).isEqualTo("new-data")
    }

    @Test
    fun `copy file symlink contents as HTTP does`() {
        val original = folder.resolve("original.txt")
        original.writeText("contents")
        val source = folder.resolve("link.txt")
        Files.createSymbolicLink(source, original)

        val (status, logs) = execute(source)
        val target = folder.resolve("home/incoming/link.txt")

        assertThat(status).withFailMessage(logs).isZero()
        assertThat(Files.isSymbolicLink(target)).isFalse()
        assertThat(target.readText()).isEqualTo("contents")
    }

    @Test
    fun `resume retained partial data and remove it after completing the file`() {
        val source = folder.resolve("large.dat")
        val data = ByteArray(262144) { (it % 251).toByte() }
        source.writeBytes(data)
        val partial = folder.resolve("home/incoming/.rsync-partial").createDirectories().resolve("large.dat")
        partial.writeBytes(data.copyOfRange(0, data.size / 2))

        val (status, logs) = execute(source)

        assertThat(status).withFailMessage(logs).isZero()
        assertThat(folder.resolve("home/incoming/large.dat").readBytes()).isEqualTo(data)
        assertThat(partial).doesNotExist()
        val matchedBytes = Regex("Matched data: ([0-9,]+)").find(logs)!!.groupValues[1].replace(",", "").toLong()
        assertThat(matchedBytes).isPositive()
    }

    @Test
    fun `retain interrupted copy while leaving existing complete destination intact`() {
        val source = folder.resolve("large.dat")
        source.writeBytes(ByteArray(2097152) { (it % 251).toByte() })
        val destination = folder.resolve("home/incoming").createDirectories().resolve("large.dat")
        destination.writeText("previous complete upload")
        val rsync = System.getenv("PATH").split(File.pathSeparator).map { Path.of(it, "rsync") }.first { Files.isExecutable(it) }
        val bin = folder.resolve("bin").createDirectory()
        val wrapper = bin.resolve("rsync")
        wrapper.writeText(
            """
            #!/bin/bash
            '${rsync.toString().replace("'", "'\"'\"'")}' --bwlimit=128 "${'$'}@" &
            child=${'$'}!
            sleep 1
            kill -INT "${'$'}child"
            wait "${'$'}child"
            """.trimIndent(),
        )
        wrapper.toFile().setExecutable(true)

        val (status, logs) = execute(source, bin = bin)

        assertThat(status).withFailMessage(logs).isNotZero()
        assertThat(destination.readText()).isEqualTo("previous complete upload")
        assertThat(folder.resolve("home/incoming/.rsync-partial/large.dat")).isNotEmptyFile()

        val (retryStatus, retryLogs) = execute(source)
        assertThat(retryStatus).withFailMessage(retryLogs).isZero()
        assertThat(destination.readBytes()).isEqualTo(source.readBytes())
    }

    private fun execute(
        source: Path,
        target: Path = folder.resolve("home/incoming"),
        bin: Path? = null,
    ): Pair<Int, String> {
        val home = folder.resolve("home").createDirectories()
        val group = Files.readAttributes(home, PosixFileAttributes::class.java).group().name
        val command = DataMoverUserFilesService.transferCommand(source.toFile(), target, group)
        val builder = ProcessBuilder("bash", "-c", command).redirectErrorStream(true)
        if (bin != null) builder.environment()["PATH"] = "$bin${File.pathSeparator}${System.getenv("PATH")}"
        val process = builder.start()
        val logs = process.inputStream.bufferedReader().readText()
        return process.waitFor() to logs
    }
}
