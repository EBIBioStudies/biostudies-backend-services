package ac.uk.ebi.biostd.factory

import ebi.ac.uk.security.integration.model.api.SecurityUser
import io.mockk.mockk
import java.time.LocalDateTime

interface TestSecurityUser {
    val email: String
    val fullName: String
    val superuser: Boolean
    val notificationsEnabled: Boolean

    fun asSecurityUser() =
        SecurityUser(
            id = 3,
            email = email,
            fullName = fullName,
            login = null,
            orcid = "0000-0002-1825-0097",
            secret = "69214a2f-f80b-4f33-86b7-26d3bd0453aa",
            superuser = superuser,
            lastActivity = LocalDateTime.parse("2024-01-01T12:00:00"),
            userFolder = mockk(),
            groupsFolders = listOf(mockk()),
            permissions = emptySet(),
            notificationsEnabled = notificationsEnabled,
            adminCollections = emptyList(),
        )
}
