package ac.uk.ebi.biostd.persistence.model

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "AccessTag")
data class AccessTag(

        @Id
        @GeneratedValue
        var id: Long = 0L,

        @Column
        var name: String
)
