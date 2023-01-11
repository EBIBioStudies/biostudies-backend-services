package ac.uk.ebi.biostd.common.config

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ebi.ac.uk.base.toPrettyString
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(prefix = "app", name = ["enablePropertiesLog"], havingValue = "true")
class PropertyLogger(@Autowired private val applicationProperties: ApplicationProperties) {
    private val logger: Logger = LoggerFactory.getLogger(PropertyLogger::class.java)

    @EventListener
    @Suppress("UnusedPrivateMember")
    fun handleContextRefresh(event: ContextRefreshedEvent) {
        logger.info("===================APPLICATION PROPERTIES========================")
        logger.info(applicationProperties.toPrettyString())
        logger.info("=================================================================")
        logger.info("=================================================================")
    }
}
