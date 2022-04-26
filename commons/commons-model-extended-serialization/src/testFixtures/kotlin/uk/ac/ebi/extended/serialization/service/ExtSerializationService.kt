package uk.ac.ebi.extended.serialization.service

import ebi.ac.uk.extended.model.ExtFile
import java.io.File

fun ExtSerializationService.files(file: File): List<ExtFile> = file.inputStream().use { deserializeList(it).toList() }
