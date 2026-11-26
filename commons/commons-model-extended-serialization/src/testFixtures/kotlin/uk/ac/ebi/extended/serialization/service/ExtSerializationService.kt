package uk.ac.ebi.extended.serialization.service

import kotlinx.coroutines.flow.toList
import uk.ac.ebi.biostd.extended.model.ExtFile
import java.io.File

suspend fun ExtSerializationService.files(file: File): List<ExtFile> = file.inputStream().use { deserializeFileListAsFlow(it) }.toList()
