package ebi.ac.uk.extended.delegates

import ebi.ac.uk.extended.model.ExtAccessTag
import ebi.ac.uk.extended.model.ExtSubmission
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

internal class AccessTagDelegate : ReadOnlyProperty<ExtSubmission, List<ExtAccessTag>> {

    override fun getValue(thisRef: ExtSubmission, property: KProperty<*>): List<ExtAccessTag> {
        val projects = thisRef.projects.map { ExtAccessTag(it.accNo) }
        return if (thisRef.released) projects.plus(ExtAccessTag("Public")) else projects
    }
}
