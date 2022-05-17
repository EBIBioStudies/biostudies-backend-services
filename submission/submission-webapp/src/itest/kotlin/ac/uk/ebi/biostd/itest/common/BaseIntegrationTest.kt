package ac.uk.ebi.biostd.itest.common

import org.testcontainers.containers.MySQLContainer

const val CHARACTER_SET = "utf8mb4"
const val COLLATION = "utf8mb4_unicode_ci"
const val FIRE_USERNAME = "fireUsername"
const val FIRE_PASSWORD = "firePassword"

class SpecificMySQLContainer(image: String) : MySQLContainer<SpecificMySQLContainer>(image)
