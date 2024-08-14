package ac.uk.ebi.biostd.submission.converters

import ebi.ac.uk.api.SubmitAttribute
import ebi.ac.uk.extended.model.ExtAttributeDetail
import ebi.ac.uk.model.constants.AttributeFields.NAME
import ebi.ac.uk.model.constants.AttributeFields.VALUE
import org.springframework.boot.configurationprocessor.json.JSONObject
import org.springframework.core.convert.converter.Converter

class ExtAttributeDetailConverter : Converter<String, ExtAttributeDetail> {
    override fun convert(source: String): ExtAttributeDetail {
        val attribute = JSONObject(source)
        return ExtAttributeDetail(attribute[NAME.value].toString(), attribute[VALUE.value].toString())
    }
}

class SubmitAttributeConverter : Converter<String, SubmitAttribute> {
    override fun convert(source: String): SubmitAttribute {
        val attribute = JSONObject(source)
        return SubmitAttribute(attribute[NAME.value].toString(), attribute[VALUE.value].toString())
    }
}
