package ac.uk.ebi.biostd.submission.converters

import org.springframework.boot.configurationprocessor.json.JSONObject
import org.springframework.core.convert.converter.Converter
import uk.ac.ebi.biostd.api.SubmitAttribute
import uk.ac.ebi.biostd.extended.model.ExtAttributeDetail
import uk.ac.ebi.biostd.model.constants.AttributeFields.NAME
import uk.ac.ebi.biostd.model.constants.AttributeFields.VALUE

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
