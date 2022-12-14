package ac.uk.ebi.biostd.persistence.model

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "Counter")
class DbCounter(
    @Column
    val name: String
) {
    @Id
    @GeneratedValue
    var id: Long = 0L

    @Column(name = "maxCount")
    var count: Long = 0
}
