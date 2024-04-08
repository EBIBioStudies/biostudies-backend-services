package ac.uk.ebi.biostd.persistence.model

import ac.uk.ebi.biostd.persistence.common.model.AccessPermission
import ac.uk.ebi.biostd.persistence.common.model.AccessType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "AccessPermission")
class DbAccessPermission(
    @Id
    @GeneratedValue
    val id: Long = 0,
    @Enumerated(EnumType.STRING)
    @Column(name = "access_type")
    override val accessType: AccessType,
    @ManyToOne
    @JoinColumn(name = "user_id")
    val user: DbUser,
    @ManyToOne
    @JoinColumn(name = "access_tag_id")
    override val accessTag: DbAccessTag,
) : AccessPermission
