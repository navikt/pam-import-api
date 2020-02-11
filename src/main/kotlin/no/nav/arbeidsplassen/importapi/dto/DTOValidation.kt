package no.nav.arbeidsplassen.importapi.dto

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import org.slf4j.LoggerFactory
import java.io.InputStream
import java.lang.Exception
import javax.inject.Singleton

@Singleton
class DTOValidation(private val objectMapper: ObjectMapper) {

    companion object {
        private val LOG = LoggerFactory.getLogger(DTOValidation::class.java)
    }
    fun jsonToNode(json: InputStream): JsonNode {
        try {
            return objectMapper.readTree(json)
        }
        catch (e: Exception) {
            when (e) {
                is JsonParseException -> throw ValidationError("Parse error: ${e.localizedMessage}", ErrorType.PARSE_ERROR)
                else -> {
                    LOG.error("Got unknown exception:", e)
                    throw ValidationError("Unknown error: ${e.localizedMessage}", ErrorType.UNKNOWN)
                }
            }
        }
    }

    fun nodeToDTO(jsonNode: JsonNode): TransferDTO {
        try {
            return objectMapper.treeToValue(jsonNode, TransferDTO::class.java)
        }
        catch (e: Exception) {
            when (e) {
                is MissingKotlinParameterException -> throw ValidationError("Missing parameter: ${e.parameter.name}", ErrorType.MISSING_PARAMETER)
                else -> {
                    LOG.error("Got unknown exception:", e)
                    throw ValidationError("Unknown error: ${e.localizedMessage}", ErrorType.UNKNOWN)
                }
            }
        }
    }
}

enum class ErrorType {
    PARSE_ERROR, MISSING_PARAMETER, INVALID_VALUE, UNKNOWN
}

class ValidationError(message: String, val type: ErrorType) : Throwable(message)