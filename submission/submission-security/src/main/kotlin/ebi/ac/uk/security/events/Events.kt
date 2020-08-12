package ebi.ac.uk.security.events

import ebi.ac.uk.security.integration.model.events.UserActivated
import io.reactivex.subjects.PublishSubject

internal object Events {
    val userRegister by lazy { PublishSubject.create<UserActivated>() }
}
