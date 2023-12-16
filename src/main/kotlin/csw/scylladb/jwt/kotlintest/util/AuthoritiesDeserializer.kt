package csw.scylladb.jwt.kotlintest.util

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import java.io.IOException

class AuthoritiesDeserializer : JsonDeserializer<Set<String?>?>() {
    @Throws(IOException::class)
    override fun deserialize(p: JsonParser, context: DeserializationContext): Set<String> {
        val authorities: MutableSet<String> = HashSet()
        val node = p.codec.readTree<JsonNode>(p)
        if (node.isArray) {
            for (element in node) {
                val authorityNode = element["authority"]
                if (authorityNode != null) {
                    authorities.add(authorityNode.asText())
                }
            }
        }
        return authorities
    }
}