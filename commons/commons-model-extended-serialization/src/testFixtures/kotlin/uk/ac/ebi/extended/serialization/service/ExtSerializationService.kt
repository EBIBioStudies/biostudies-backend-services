package uk.ac.ebi.extended.serialization.service

import ebi.ac.uk.extended.model.ExtFile
import kotlinx.coroutines.flow.toList
import java.io.File

suspend fun ExtSerializationService.files(file: File): List<ExtFile> = file.inputStream().use { deserializeFileListAsFlow(it) }.toList()
