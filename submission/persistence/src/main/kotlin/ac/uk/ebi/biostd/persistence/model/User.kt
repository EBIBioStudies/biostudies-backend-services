package ac.uk.ebi.biostd.persistence.model

import ac.uk.ebi.biostd.persistence.converters.AuxInfoConverter
import java.time.OffsetDateTime
import javax.persistence.CascadeType.MERGE
import javax.persistence.CascadeType.PERSIST
import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.Lob
import javax.persistence.ManyToMany
import javax.persistence.OneToMany
import javax.persistence.Table
import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement

@Entity
@Table(name = "User")
class User(
    @Id
    @GeneratedValue
    var id: Long = 0L,

    @Column
    var email: String,

    @Column
    var fullName: String,

    @Column
    var keyTime: Long = OffsetDateTime.now().toInstant().toEpochMilli(),

    @Column
    var secret: String,

    @Lob
    var passwordDigest: ByteArray,

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "UserGroup_User",
        joinColumns = [JoinColumn(name = "users_id")],
        inverseJoinColumns = [JoinColumn(name = "groups_id")])
    val groups: MutableSet<UserGroup> = mutableSetOf(),

    @Column
    var superuser: Boolean = false
) {
    @Column
    var active: Boolean = false

    var login: String? = null

    @Column
    var activationKey: String? = null

    @Column(name = "auxProfileInfo")
    @Convert(converter = AuxInfoConverter::class)
    var auxInfo: AuxInfo = AuxInfo()

    @OneToMany(mappedBy = "user", cascade = [PERSIST, MERGE])
    val permissions: Set<AccessPermission> = emptySet()
}

@XmlRootElement(name = "aux")
@XmlAccessorType(XmlAccessType.NONE)
class AuxInfo {

    @XmlElement(name = "param")
    val parameters: MutableList<Parameter> = mutableListOf()

    operator fun get(name: String): String {
        return parameters.firstOrNull { it.name == name }?.value.orEmpty()
    }
}

@XmlAccessorType(XmlAccessType.NONE)
class Parameter {
    @XmlElement(name = "name")
    var name: String? = null

    @XmlElement(name = "value")
    var value: String? = null
}
