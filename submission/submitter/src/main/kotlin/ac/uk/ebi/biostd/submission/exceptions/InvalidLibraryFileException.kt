package ac.uk.ebi.biostd.submission.exceptions

import ebi.ac.uk.model.Section

class InvalidLibraryFileException(sections: List<Section>) : RuntimeException(
    "The following sections contain an invalid library file: ${sections.map { "${it.type} ${it.accNo}" }}")
