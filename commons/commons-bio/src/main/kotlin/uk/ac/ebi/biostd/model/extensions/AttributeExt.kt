package uk.ac.ebi.biostd.model.extensions

import uk.ac.ebi.biostd.model.Attribute
import uk.ac.ebi.biostd.model.AttributeDetail

val Attribute.nameAttrsNames: List<String>
    get() = nameAttrs.map(AttributeDetail::name)

val Attribute.nameAttrsValues: List<String?>
    get() = nameAttrs.map(AttributeDetail::value)

val Attribute.valueAttrsNames: List<String>
    get() = valueAttrs.map(AttributeDetail::name)

val Attribute.valueAttrsValues: List<String?>
    get() = valueAttrs.map(AttributeDetail::value)
