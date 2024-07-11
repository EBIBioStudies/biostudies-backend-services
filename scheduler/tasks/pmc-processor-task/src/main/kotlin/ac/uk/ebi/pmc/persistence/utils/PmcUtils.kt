package ac.uk.ebi.pmc.persistence.utils

object PmcUtils {
    private val regex = ".*bs-(\\d{2}-\\d{2}-\\d{4}-\\d+)\\.txt\\.gz".toRegex()
    private val error = "Invalid file name format expecting to match $regex"
    private const val PARTS = 4

    /**
     * Extract the sequence number from a pmc file. ie. bs-07-02-2024-6.txt.gz
     */
    fun extractSequence(filePath: String): Int {
        val match = regex.find(filePath)
        val datePart = match?.groups?.get(1)?.value ?: throw IllegalArgumentException(error)

        // Split the date part
        val dateComponents = datePart.split("-")
        if (dateComponents.size != PARTS) throw IllegalArgumentException(error)

        // Reformat the date
        val (day, month, year, sequence) = dateComponents
        return "$year$month$day$sequence".toInt()
    }
}
