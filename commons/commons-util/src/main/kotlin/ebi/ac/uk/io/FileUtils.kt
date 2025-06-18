package ebi.ac.uk.io

import ebi.ac.uk.io.FileUtilsHelper.createDirectories
import ebi.ac.uk.io.FileUtilsHelper.createFileHardLink
import ebi.ac.uk.io.FileUtilsHelper.createFolderHardLinks
import ebi.ac.uk.io.FileUtilsHelper.createFolderIfNotExist
import ebi.ac.uk.io.FileUtilsHelper.createParentDirectories
import ebi.ac.uk.io.FileUtilsHelper.createSymLink
import ebi.ac.uk.io.ext.isEmpty
import ebi.ac.uk.io.ext.listFilesOrEmpty
import ebi.ac.uk.io.ext.notExist
import ebi.ac.uk.io.ext.size
import mu.KotlinLogging
import net.openhft.hashing.LongHashFunction
import org.apache.commons.codec.digest.DigestUtils
import org.springframework.core.io.InputStreamSource
import java.io.File
import java.io.InputStream
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Files
import java.nio.file.Files.deleteIfExists
import java.nio.file.Files.exists
import java.nio.file.Path
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.attribute.PosixFilePermissions
import kotlin.io.path.inputStream
import kotlin.streams.toList as kotlinToList

val RW_______: Set<PosixFilePermission> = PosixFilePermissions.fromString("rw-------")
val RWX______: Set<PosixFilePermission> = PosixFilePermissions.fromString("rwx------")
val RWX__X___: Set<PosixFilePermission> = PosixFilePermissions.fromString("rwx--x---")
val RW_RW____: Set<PosixFilePermission> = PosixFilePermissions.fromString("rw-rw----")
val RWXRWX___: Set<PosixFilePermission> = PosixFilePermissions.fromString("rwxrwx---")
val RWXR_X__X: Set<PosixFilePermission> = PosixFilePermissions.fromString("rwxr-x--x")
val RW_R__R__: Set<PosixFilePermission> = PosixFilePermissions.fromString("rw-r--r--")
val RWXR_XR_X: Set<PosixFilePermission> = PosixFilePermissions.fromString("rwxr-xr-x")

@Suppress("TooManyFunctions")
object FileUtils {
    fun copyOrReplaceFile(
        source: File,
        target: File,
        permissions: Permissions,
    ) = when (isDirectory(source)) {
        true -> FileUtilsHelper.copyFolder(source.toPath(), target.toPath(), permissions)
        false -> FileUtilsHelper.copyFile(source.toPath(), target.toPath(), permissions)
    }

    fun copyOrReplaceFile(
        source: InputStreamSource,
        target: File,
        permissions: Permissions,
    ) {
        source.inputStream.use { FileUtilsHelper.copyFile(it, target.toPath(), permissions) }
    }

    fun getOrCreateFolder(
        folder: Path,
        permissions: Set<PosixFilePermission>,
    ): Path {
        require(exists(folder).not() || isDirectory(folder.toFile())) { "'$folder' points to a file" }
        createFolderIfNotExist(folder, permissions)
        return folder
    }

    fun createEmptyFolder(
        folder: Path,
        permissions: Set<PosixFilePermission>,
    ) {
        deleteFile(folder.toFile())
        createDirectories(folder, permissions)
    }

    fun createParentFolders(
        folder: Path,
        permissions: Set<PosixFilePermission>,
    ) {
        createDirectories(folder.parent, permissions)
    }

    fun deleteFile(file: File) {
        when {
            isDirectory(file) -> FileUtilsHelper.deleteFolder(file.toPath())
            exists(file.toPath()) -> Files.delete(file.toPath())
        }
    }

    fun deleteEmptyDirectories(dir: File) {
        dir
            .walkBottomUp()
            .asSequence()
            .filter { it.isDirectory && it.isEmpty() }
            .forEach { Files.delete(it.toPath()) }
    }

    fun cleanDirectory(dir: File) {
        dir
            .listFilesOrEmpty()
            .asSequence()
            .forEach { deleteFile(it) }
    }

    fun moveFile(
        source: File,
        target: File,
        permissions: Permissions,
    ) {
        deleteFile(target)
        when (isDirectory(source)) {
            true -> FileUtilsHelper.moveFolder(source.toPath(), target.toPath(), permissions)
            false -> FileUtilsHelper.moveFile(source.toPath(), target.toPath(), permissions)
        }
    }

    fun createHardLink(
        file: File,
        sourcePath: Path,
        targetPath: Path,
        permissions: Permissions,
    ) {
        val filePath = file.toPath()
        val target = targetPath.resolve(sourcePath.relativize(filePath))

        if (file.isDirectory) {
            createFolderHardLinks(filePath, target, permissions)
        } else {
            createFileHardLink(filePath, target, permissions)
        }
    }

    fun createSymbolicLink(
        path: Path,
        symLinkPath: Path,
        permissions: Set<PosixFilePermission>,
    ) {
        createSymLink(path, symLinkPath, permissions)
    }

    fun writeContent(
        source: File,
        content: String,
        permissions: Permissions = Permissions(RW_______, RWX______),
    ) {
        val filePath = source.toPath()
        Files.write(createParentDirectories(source.toPath(), permissions.folder), content.toByteArray())
        Files.setPosixFilePermissions(filePath, permissions.file)
    }

    fun isDirectory(file: File): Boolean = Files.isDirectory(file.toPath())

    fun size(
        file: File,
        iterateDirectories: Boolean = true,
    ): Long = if (file.isDirectory && iterateDirectories) calculateDirectorySize(file) else Files.size(file.toPath())

    fun md5(file: File): String = if (file.isFile) calculateMd5(file) else hashFolder(file).toString()

    fun md5(value: String): String = DigestUtils.md5Hex(value).uppercase()

    fun listAllFiles(file: File): List<File> {
        require(file.isDirectory) { "$file need to be a directory" }
        return Files
            .walk(file.toPath())
            .skip(1)
            .sorted()
            .map { it.toFile() }
            .kotlinToList()
    }

    fun listFiles(file: File): List<File> =
        if (isDirectory(file)) {
            Files.list(file.toPath()).use { stream -> stream.map { it.toFile() }.kotlinToList() }
        } else {
            emptyList()
        }

    private fun calculateMd5(file: File): String = file.inputStream().use { DigestUtils.md5Hex(it).uppercase() }

    private fun calculateDirectorySize(dir: File) =
        dir
            .walkTopDown()
            .filter { it.isFile }
            .map { it.size() }
            .sum()
}

private val logger = KotlinLogging.logger {}

@Suppress("TooManyFunctions")
internal object FileUtilsHelper {
    fun createFolderIfNotExist(
        file: Path,
        permissions: Set<PosixFilePermission>,
    ) {
        if (exists(file).not()) createDirectories(file, permissions)
    }

    fun createFolderHardLinks(
        sourcePath: Path,
        targetPath: Path,
        permissions: Permissions,
    ) {
        Files.walkFileTree(sourcePath, HardLinkFileVisitor(sourcePath, targetPath, permissions))
    }

    fun createFileHardLink(
        filePath: Path,
        target: Path,
        permissions: Permissions,
    ) {
        runSafely {
            logger.info { "Processing Hardlink for file $filePath into target $target" }
            deleteIfExists(target)
            FileUtils.createParentFolders(target, permissions.folder)
            Files.createLink(target, filePath)
            Files.setPosixFilePermissions(target, permissions.file)
            logger.info { "Finished Hardlink for file $filePath into target $target" }
        }
    }

    fun createSymLink(
        link: Path,
        target: Path,
        permissions: Set<PosixFilePermission>,
    ) {
        if (exists(link)) Files.delete(link)
        Files.createSymbolicLink(createParentDirectories(link, permissions), target)
    }

    fun copyFolder(
        source: Path,
        target: Path,
        permissions: Permissions,
    ) {
        deleteFolder(target)
        Files.walkFileTree(source, CopyFileVisitor(source, target, permissions))
    }

    fun moveFolder(
        source: Path,
        target: Path,
        permissions: Permissions,
    ) {
        deleteFolder(target)
        Files.walkFileTree(source, MoveFileVisitor(source, target, permissions))
        deleteFolder(source)
    }

    fun copyFile(
        source: Path,
        target: Path,
        permissions: Permissions,
    ) {
        source.inputStream().use { copyFile(it, target, permissions) }
    }

    fun copyFile(
        source: InputStream,
        target: Path,
        permissions: Permissions,
    ) {
        runSafely {
            Files.copy(source, createParentDirectories(target, permissions.folder), REPLACE_EXISTING)
            Files.setPosixFilePermissions(target, permissions.file)
        }
    }

    fun moveFile(
        source: Path,
        target: Path,
        permissions: Permissions,
    ) {
        Files.move(source, createParentDirectories(target, permissions.folder), REPLACE_EXISTING)
        Files.setPosixFilePermissions(target, permissions.file)
    }

    fun createParentDirectories(
        path: Path,
        permissions: Set<PosixFilePermission>,
    ): Path {
        createDirectories(path.parent, permissions)
        return path
    }

    fun createDirectories(
        directoryPath: Path,
        permissions: Set<PosixFilePermission>,
    ): Path {
        var parent = directoryPath.root

        for (path in parent.relativize(directoryPath)) {
            parent = parent.resolve(path)
            if (parent.notExist()) createDirectory(parent, permissions)
        }

        return directoryPath
    }

    private fun createDirectory(
        path: Path,
        permissions: Set<PosixFilePermission>,
    ) {
        runSafely {
            Files.createDirectory(path)
            Files.setPosixFilePermissions(path, permissions)
        }
    }

    /*
     * Some methods require ignoring exceptions of the type FileAlreadyExistsException in order to become thread safe.
     * See this discussion for more details:
     * https://github.com/EBIBioStudies/biostudies-backend-services/pull/733#discussion_r1280085979
     */
    private fun runSafely(func: () -> Unit) {
        runCatching {
            func()
        }.onFailure {
            if ((it is FileAlreadyExistsException).not()) throw it
        }
    }

    fun deleteFolder(path: Path) {
        if (exists(path)) {
            Files
                .walk(path)
                .sorted(Comparator.reverseOrder())
                .forEach { deleteIfExists(it) }
        }
    }
}

data class Permissions(
    val file: Set<PosixFilePermission>,
    val folder: Set<PosixFilePermission>,
)

fun hashFolder(folder: File): Long {
    val hasher = LongHashFunction.xx3()

    fun listFiles(root: File): List<Pair<String, File>> =
        root
            .walkTopDown()
            .sortedBy { it.relativeTo(root).invariantSeparatorsPath }
            .map { it.relativeTo(root).invariantSeparatorsPath to it }
            .toList()

    @Suppress("MagicNumber")
    fun hashFileContent(file: File): Long {
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var hash = 0L
            var bytesRead: Int

            while (input.read(buffer).also { bytesRead = it } != -1) {
                hash = hash xor hasher.hashBytes(buffer, 0, bytesRead)
            }
            return hash
        }
    }

    fun hashFile(pair: Pair<String, File>): Long {
        val (relativePath, file) = pair
        val pathBytes = relativePath.toByteArray()

        return when {
            file.isFile -> hasher.hashBytes(pathBytes) xor hashFileContent(file)
            else -> hasher.hashBytes(pathBytes) xor hasher.hashBytes("<DIR>".toByteArray())
        }
    }

    require(folder.isDirectory)
    logger.info { "Calculating folder '${folder.absolutePath}' hash" }
    val hash =
        listFiles(folder)
            .map { hashFile(it) }
            .fold(0L) { acc, h -> acc xor h }
    logger.info { "Folder hash '${folder.absolutePath}' calculation completed" }
    return hash
}
