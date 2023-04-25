package ac.uk.ebi.biostd.itest.common

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import com.amazonaws.services.s3.AmazonS3
import ebi.ac.uk.security.service.SecurityService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.ac.ebi.fire.client.integration.web.FireClientFactory
import uk.ac.ebi.fire.client.integration.web.S3Config

@Configuration
class TestConfig {
    @Bean
    fun securityTestService(
        userDataRepository: UserDataRepository,
        securityService: SecurityService,
    ) = SecurityTestService(securityService, userDataRepository)

    @Bean(name = ["TestCollectionValidator"])
    fun testCollectionValidator(): TestCollectionValidator = TestCollectionValidator()

    @Bean(name = ["FailCollectionValidator"])
    fun failCollectionValidator(): FailCollectionValidator = FailCollectionValidator()

    @Bean
    fun s3Service(appProperties: ApplicationProperties): AmazonS3 {
        val fireProps = appProperties.fire
        return FireClientFactory.amazonS3Client(
            S3Config(
                accessKey = fireProps.s3AccessKey,
                secretKey = fireProps.s3SecretKey,
                region = fireProps.s3region,
                endpoint = fireProps.s3endpoint,
                bucket = fireProps.s3bucket
            )
        )
    }
}
