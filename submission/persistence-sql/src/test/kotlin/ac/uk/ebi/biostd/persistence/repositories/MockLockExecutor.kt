package ac.uk.ebi.biostd.persistence.repositories

class MockLockExecutor : LockExecutor {
    override fun <T> executeLocking(lockName: String, timeout: Int, executable: () -> T): T = executable()
}
