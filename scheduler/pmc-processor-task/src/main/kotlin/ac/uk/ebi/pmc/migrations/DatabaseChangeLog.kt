package ac.uk.ebi.pmc.migrations

import com.github.cloudyrock.mongock.ChangeLog
import com.github.cloudyrock.mongock.ChangeSet
import com.github.cloudyrock.mongock.driver.mongodb.springdata.v3.decorator.impl.MongockTemplate

@ChangeLog
class DatabaseChangeLog {

    @ChangeSet(order = "001", id = "Create Schema", author = "System")
    fun createSchema(template: MongockTemplate) {
        template.createCollection("example")
    }
}
