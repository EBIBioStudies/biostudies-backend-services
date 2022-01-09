package ebi.ac.uk.commons.http.spring

import org.springframework.util.LinkedMultiValueMap

fun multiValueMap(vararg pairs: Pair<Any, Any>): LinkedMultiValueMap<Any, Any> {
    return LinkedMultiValueMap(pairs.toList().groupBy({ it.first }, { it.second }))
}
