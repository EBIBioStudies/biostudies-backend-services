package ac.uk.ebi.biostd.common.config

import ac.uk.ebi.biostd.common.properties.LoggingProperties
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener

class PropertyLogger(private val loggingProperties: LoggingProperties) {
    private val logger: Logger = LoggerFactory.getLogger(PropertyLogger::class.java)

    @EventListener
    @Suppress("UnusedPrivateMember")
    fun handleContextRefresh(event: ContextRefreshedEvent) {
        logger.info("===================APPLICATION PROPERTIES========================")
        logger.info("------------------------SPRING-----------------------------------")
        loggingProperties.spring.forEach { (t, u) -> logger.info("* $t: $u") }
        logger.info("--------------------------MONGO----------------------------------")
        loggingProperties.mongo.forEach { (t, u) -> logger.info("* $t: $u") }
        logger.info("------------------------MYSQL-JPA--------------------------------")
        loggingProperties.mysql.forEach { (t, u) -> logger.info("* $t: $u") }
        logger.info("------------------------RABBITMQ---------------------------------")
        loggingProperties.rabbit.forEach { (t, u) -> logger.info("* $t: $u") }
        logger.info("-------------------------SERVLET---------------------------------")
        loggingProperties.servlet.forEach { (t, u) -> logger.info("* $t: $u") }
        logger.info("--------------------------APP------------------------------------")
        loggingProperties.app.forEach { (t, u) -> logger.info("* $t: $u") }
        logger.info("--------------------------FIRE-----------------------------------")
        loggingProperties.fire.forEach { (t, u) -> logger.info("* $t: $u") }
        logger.info("-------------------------SECURITY--------------------------------")
        loggingProperties.security.forEach { (t, u) -> logger.info("* $t: $u") }
        logger.info("=================================================================")
        logger.info("=================================================================")
    }
}
