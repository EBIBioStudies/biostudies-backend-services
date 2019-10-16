package ac.uk.ebi.biostd.persistence.mapping.extended.to.test

import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtLink
import ebi.ac.uk.extended.model.ExtSection
import java.io.File as SystemFile

internal val extTestAttribute2 get() = ExtAttribute("a", "b", false, emptyList(), emptyList())
internal val extTestFile2 get() = ExtFile("fileName", SystemFile("random_path"), listOf(extTestAttribute2))
internal val extTestLink2 get() = ExtLink("linkUrl", listOf(extTestAttribute2))
internal val extTestSection2 get() = ExtSection(accNo = "accNo", type = "type", attributes = listOf(extTestAttribute2))
