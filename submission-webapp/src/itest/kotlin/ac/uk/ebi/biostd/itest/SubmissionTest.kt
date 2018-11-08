package ac.uk.ebi.biostd.itest

import ac.uk.ebi.biostd.config.SubmitterConfig
import ebi.ac.uk.model.Submission
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@Import(value = [TestConfig::class])
@SpringBootTest(webEnvironment = RANDOM_PORT)
class SubmissionTest {

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Test
    fun `context load`() {
        val submission = restTemplate.postForEntity("/submissions", Submission(accNo = "example"), Submission::class.java)
        assertThat(submission).isNotNull();
    }
}

@Configuration
@Import(value = [SubmitterConfig::class])
class TestConfig {

    /*@Bean
    fun properties(): ApplicationProperties {
        val properties = ApplicationProperties()
        properties.basePath = Paths.get("/home/jcamilorada/Projects/NFS")
        return properties
    }*/
}
