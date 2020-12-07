package ac.uk.ebi.biostd.persistence.model

import org.hibernate.annotations.Type
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "SubmissionRequest")
class DbSubmissionRequest(
    @Column
    var accNo: String,

    @Column
    var version: Int,

    @Column
    @Type(type = "text")
    var request: String
) {
    @Id
    @GeneratedValue
    var id: Long = 0L
}
