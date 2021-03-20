package ac.uk.ebi.biostd.persistence.doc.commons

import org.springframework.data.mongodb.core.query.Update
import org.springframework.util.Assert

class ExtendedUpdate : Update() {
    fun multiply(key: String, multiplier: Int): ExtendedUpdate {
        Assert.notNull(multiplier, "Multiplier must not be null.")
        addMultiFieldOperation("\$mul", key, multiplier)
        return this
    }
}
