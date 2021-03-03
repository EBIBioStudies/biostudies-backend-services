package ac.uk.ebi.biostd.persistence.doc.test.doc.ext

import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtAttributeDetail

const val SUBMISSION_ATTRIBUTE_NAME = "name"
const val SUBMISSION_ATTRIBUTE_VALUE = "value"
const val SUBMISSION_ATTRIBUTE_REFERENCE = false
val SUBMISSION_ATTR_NAME_ATTRS = listOf<ExtAttributeDetail>()
val SUBMISSION_ATTR_VALUE_ATTRS = listOf<ExtAttributeDetail>()
val submissionAttribute = ExtAttribute(
    name = SUBMISSION_ATTRIBUTE_NAME,
    value = SUBMISSION_ATTRIBUTE_VALUE,
    reference = SUBMISSION_ATTRIBUTE_REFERENCE,
    nameAttrs = SUBMISSION_ATTR_NAME_ATTRS,
    valueAttrs = SUBMISSION_ATTR_VALUE_ATTRS
)
const val ROOT_SEC_ATTRIBUTE_NAME = "name1"
const val ROOT_SEC_ATTRIBUTE_VALUE = "value1"
const val ROOT_SEC_ATTRIBUTE_REFERENCE = false
val ROOT_SEC_ATTR_NAME_ATTRS = listOf<ExtAttributeDetail>()
val ROOT_SEC_ATTR_VALUE_ATTRS = listOf<ExtAttributeDetail>()
val rootSectionAttribute = ExtAttribute(
    name = ROOT_SEC_ATTRIBUTE_NAME,
    value = ROOT_SEC_ATTRIBUTE_VALUE,
    reference = ROOT_SEC_ATTRIBUTE_REFERENCE,
    nameAttrs = ROOT_SEC_ATTR_NAME_ATTRS,
    valueAttrs = ROOT_SEC_ATTR_VALUE_ATTRS
)
const val SUB_SEC_TABLE_ATTR_NAME = "name2"
const val SUB_SEC_TABLE_ATTR_VALUE = "value2"
const val SUB_SEC_TABLE_ATTR_REFERENCE = false
val SUB_SEC_TABLE_ATTR_NAME_ATTRS = listOf<ExtAttributeDetail>()
val SUB_SEC_TABLE_ATTR_VALUE_ATTRS = listOf<ExtAttributeDetail>()
val subSectionTableAttribute = ExtAttribute(
    name = SUB_SEC_TABLE_ATTR_NAME,
    value = SUB_SEC_TABLE_ATTR_VALUE,
    reference = SUB_SEC_TABLE_ATTR_REFERENCE,
    nameAttrs = SUB_SEC_TABLE_ATTR_NAME_ATTRS,
    valueAttrs = SUB_SEC_TABLE_ATTR_VALUE_ATTRS
)
const val ROOT_SEC_LINK_ATTRIBUTE_NAME = "name3"
const val ROOT_SEC_LINK_ATTRIBUTE_VALUE = "value3"
const val ROOT_SEC_LINK_ATTRIBUTE_REFERENCE = false
val ROOT_SEC_LINK_ATTR_NAME_ATTRS = listOf<ExtAttributeDetail>()
val ROOT_SEC_LINK_ATTRIBUTE_VALUE_ATTRS = listOf<ExtAttributeDetail>()
val rootSectionLinkAttribute = ExtAttribute(
    name = ROOT_SEC_LINK_ATTRIBUTE_NAME,
    value = ROOT_SEC_LINK_ATTRIBUTE_VALUE,
    reference = ROOT_SEC_LINK_ATTRIBUTE_REFERENCE,
    nameAttrs = ROOT_SEC_LINK_ATTR_NAME_ATTRS,
    valueAttrs = ROOT_SEC_LINK_ATTRIBUTE_VALUE_ATTRS
)
const val ROOT_SEC_TABLE_LINK_ATTRIBUTE_NAME = "name4"
const val ROOT_SEC_TABLE_LINK_ATTRIBUTE_VALUE = "value4"
const val ROOT_SEC_TABLE_LINK_ATTRIBUTE_REFERENCE = false
val ROOT_SEC_TABLE_LINK_ATTR_NAME_ATTRS = listOf<ExtAttributeDetail>()
val ROOT_SEC_TABLE_LINK_ATTR_VALUE_ATTRS = listOf<ExtAttributeDetail>()
val rootSectionTableLinkAttribute = ExtAttribute(
    name = ROOT_SEC_TABLE_LINK_ATTRIBUTE_NAME,
    value = ROOT_SEC_TABLE_LINK_ATTRIBUTE_VALUE,
    reference = ROOT_SEC_TABLE_LINK_ATTRIBUTE_REFERENCE,
    nameAttrs = ROOT_SEC_TABLE_LINK_ATTR_NAME_ATTRS,
    valueAttrs = ROOT_SEC_TABLE_LINK_ATTR_VALUE_ATTRS
)
