package ac.uk.ebi.biostd.itest.itest

import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import java.io.File

/**
 * Holds application properties and write them into its YAML representation.
 */
class PropertyHolder {
    private val properties = mutableListOf<Pair<String, String>>()

    fun addProperty(name: String, value: String): Boolean = properties.add(name to value)

    fun writeProperties() {
        val options = DumperOptions()
        options.setIndent(2)
        options.isPrettyFlow = true
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK)
        val yaml = Yaml(options)

        val propertyFile = File(this::class.java.getResource("/application_base.yml")!!.toURI())
        val baseProperties = propertyFile.inputStream().use { yaml.load<MutableMap<String, Any>>(it) }
        val finalProperties = mergeWith(baseProperties)

        val result = File(this::class.java.getResource("/application.yml")!!.toURI())
        result.createNewFile()
        result.writeText(yaml.dump(finalProperties))
    }

    private fun mergeWith(result: MutableMap<String, Any>): MutableMap<String, Any> {
        for ((key, value) in properties) {
            val keys = key.split(".")
            var currentMap = result
            for (i in 0 until keys.size - 1) {
                val subKey = keys[i]
                currentMap = currentMap.getOrPut(subKey) { mutableMapOf<String, Any>() } as MutableMap<String, Any>
            }
            currentMap[keys.last()] = value
        }

        return result
    }
}
