package ac.uk.ebi.biostd.persistence.model

import ac.uk.ebi.biostd.persistence.converters.AuxInfoConverter
import org.springframework.data.domain.AbstractAggregateRoot
import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Lob
import javax.persistence.ManyToMany
import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement

@Entity
class User(

    @Id
    @GeneratedValue
    var id: Long = 0L,

    @Column
    var email: String,

    @Column
    var fullName: String,

    @Column
    var secret: String,

    @Lob
    var passwordDigest: ByteArray,

    @ManyToMany(mappedBy = "users", fetch = FetchType.EAGER)
    val groups: MutableSet<UserGroup> = mutableSetOf(),

    @Column
    var superuser: Boolean = false

) : AbstractAggregateRoot<User>() {

    @Column
    var active: Boolean = false

    @Column(name = "auxProfileInfo")
    @Convert(converter = AuxInfoConverter::class)
    var auxInfo: AuxInfo = AuxInfo()

    var login: String? = null

    @Column
    var activationKey: String? = null

    /**
     * Register the user to the given group.
     */
    fun addGroup(userGroup: UserGroup) = groups.add(userGroup)

    fun register(activationKey: String): User {
        this.activationKey = activationKey
        return this
    }

    fun activated(): User {
        this.active = true
        return this
    }
}

@XmlRootElement(name = "aux")
@XmlAccessorType(XmlAccessType.NONE)
class AuxInfo {

    @XmlElement(name = "param")
    val parameters: List<Parameter> = emptyList()
}

@XmlAccessorType(XmlAccessType.NONE)
class Parameter {

    @XmlElement(name = "name")
    var name: String? = null

    @XmlElement(name = "value")
    var value: String? = null
}
