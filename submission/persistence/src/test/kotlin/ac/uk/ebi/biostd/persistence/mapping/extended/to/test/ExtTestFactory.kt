package ac.uk.ebi.biostd.persistence.mapping.extended.to.test

import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtFile
import java.io.File as SystemFile

internal val extTestAttribute get() = ExtAttribute("a", "b", false, emptyList(), emptyList())
internal val extTestFile get() = ExtFile("fileName", SystemFile("random_path"), listOf(extTestAttribute))
