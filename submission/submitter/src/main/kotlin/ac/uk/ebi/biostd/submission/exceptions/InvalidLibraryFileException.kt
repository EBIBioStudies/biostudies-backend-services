package ac.uk.ebi.biostd.submission.exceptions

import ebi.ac.uk.model.Section

class InvalidLibraryFileException(val sections: List<Section>, message: String) : RuntimeException(message)
