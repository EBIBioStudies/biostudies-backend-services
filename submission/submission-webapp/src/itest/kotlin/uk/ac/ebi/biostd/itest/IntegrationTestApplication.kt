package uk.ac.ebi.biostd.itest

import ac.uk.ebi.biostd.SubmissionWebApp
import org.springframework.boot.SpringBootConfiguration
import org.springframework.context.annotation.Import
import uk.ac.ebi.biostd.itest.common.TestConfig

/**
 * This is required because moving the tests changed how Spring Boot discovers their configuration. SpringBootTest
 * searches upward from a test’s package for a @SpringBootConfiguration. Before the rename, the tests were beneath
 * ac.uk.ebi.biostd, where SubmissionWebApp lives. After the move to uk.ac.ebi.biostd.itest, that search could no longer
 * find it.
 *
 * TODO: Remove once the packages in src have been fixed
 */
@SpringBootConfiguration
@Import(SubmissionWebApp::class, TestConfig::class)
class IntegrationTestApplication
