package ebi.ac.uk.model.extensions

import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.AttributeDetail

val Attribute.nameAttrsNames: List<String>
    get() = nameAttrs.map(AttributeDetail::name)

val Attribute.nameAttrsValues: List<String>
    get() = nameAttrs.map(AttributeDetail::value)

val Attribute.valueAttrsNames: List<String>
    get() = valueAttrs.map(AttributeDetail::name)

val Attribute.valueAttrsValues: List<String>
    get() = valueAttrs.map(AttributeDetail::value)
