package ac.uk.ebi.biostd.submission.exceptions

import ebi.ac.uk.model.Section

class InvalidSectionAccNoException(sections: List<Section>) :
    RuntimeException("Sections containing library files must have an accession number: ${sections.map { it.type }}")
