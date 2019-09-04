package ebi.ac.uk.security.events

import ebi.ac.uk.security.integration.model.events.PasswordReset
import ebi.ac.uk.security.integration.model.events.UserActivated
import ebi.ac.uk.security.integration.model.events.UserRegister
import io.reactivex.subjects.PublishSubject

internal class Events {
    companion object {
        val userPreRegister by lazy { PublishSubject.create<UserRegister>() }
        val userRegister by lazy { PublishSubject.create<UserActivated>() }
        val passwordReset by lazy { PublishSubject.create<PasswordReset>() }
    }
}
