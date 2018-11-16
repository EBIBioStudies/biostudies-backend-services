package ac.uk.ebi.biostd.persistence.model

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "IdGen")
class Sequence {

    @Id
    @GeneratedValue
    var id: Long = 0L

    @Column
    lateinit var prefix: String

    @Column
    lateinit var suffix: String

    @Column(name = "counter_id")
    var counter: Long = 0L
}
