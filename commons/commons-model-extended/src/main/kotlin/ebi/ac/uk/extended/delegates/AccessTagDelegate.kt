package ebi.ac.uk.extended.delegates

import ebi.ac.uk.extended.model.ExtAccessTag
import ebi.ac.uk.extended.model.ExtSubmission
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

internal class AccessTagDelegate : ReadOnlyProperty<ExtSubmission, List<ExtAccessTag>> {
    override fun getValue(
        thisRef: ExtSubmission,
        property: KProperty<*>,
    ): List<ExtAccessTag> {
        val tags = thisRef.collections.map { ExtAccessTag(it.accNo) }.plus(ExtAccessTag(thisRef.owner))
        return if (thisRef.released) tags.plus(ExtAccessTag("Public")) else tags
    }
}
