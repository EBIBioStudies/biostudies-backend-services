package ac.uk.ebi.biostd.submission.config

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import uk.ac.ebi.biostd.base.toPrettyString

private val logger = KotlinLogging.logger {}

@Component
@ConditionalOnProperty(prefix = "app", name = ["enablePropertiesLog"], havingValue = "true")
class PropertyLogger(
    @Autowired private val applicationProperties: ApplicationProperties,
) {
    @EventListener
    @Suppress("UnusedPrivateMember")
    fun handleContextRefresh(event: ContextRefreshedEvent) {
        logger.info("===================APPLICATION PROPERTIES========================")
        logger.info(applicationProperties.toPrettyString())
        logger.info("=================================================================")
    }
}
