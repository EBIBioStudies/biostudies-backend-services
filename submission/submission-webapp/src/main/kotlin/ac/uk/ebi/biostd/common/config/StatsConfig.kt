package ac.uk.ebi.biostd.common.config

import ac.uk.ebi.biostd.stats.web.handlers.StatsFileHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class StatsConfig {
    @Bean
    fun statsFileHandler(): StatsFileHandler = StatsFileHandler()
}
