package ebi.ac.uk.security.integration

import ebi.ac.uk.security.integration.model.events.UserPreRegister
import ebi.ac.uk.security.integration.model.events.UserRegister
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class SecurityEvents {

    companion object {

        // Subjects
        internal val userPreRegister by lazy { PublishSubject.create<UserPreRegister>() }
        internal val userRegister by lazy { PublishSubject.create<UserRegister>() }

        // Observables
        fun userPreRegister(): Observable<UserPreRegister> = userPreRegister
        fun userRegister(): Observable<UserRegister> = userRegister
    }
}
