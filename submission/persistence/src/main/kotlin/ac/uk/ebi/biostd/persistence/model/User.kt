package ac.uk.ebi.biostd.persistence.model

import ac.uk.ebi.biostd.persistence.events.UserActivatedEvent
import ac.uk.ebi.biostd.persistence.events.UserRegisterEvent
import org.springframework.data.domain.AbstractAggregateRoot
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Lob
import javax.persistence.ManyToMany

@Entity
class User(

    @Column
    var login: String,

    @Column
    var email: String,

    @Column
    var secret: String

) : AbstractAggregateRoot<User>() {

    @Id
    @GeneratedValue
    var id: Long = 0L

    @Column
    var fullName: String? = null

    @Column
    var superuser: Boolean = false

    @Column
    var active: Boolean = false

    @Lob
    var passwordDigest: ByteArray = ByteArray(0)

    @ManyToMany(mappedBy = "users", fetch = FetchType.EAGER)
    val groups: MutableSet<UserGroup> = mutableSetOf()

    constructor(id: Long, login: String, email: String, secret: String) : this(login, email, secret) {
        this.id = id
    }

    /**
     * Register the user to the given group.
     */
    fun addGroup(userGroup: UserGroup) = groups.add(userGroup)

    /**
     * Register @see [UserRegisterEvent] to be dispatched when user is saved.
     */
    fun registered(activationLink: String?): User {
        registerEvent(UserRegisterEvent(this, activationLink))
        return this
    }

    /**
     * Register @see [UserActivatedEvent] to be dispatched when user is saved.
     */
    fun activated(): User {
        registerEvent(UserActivatedEvent(this))
        return this
    }
}
