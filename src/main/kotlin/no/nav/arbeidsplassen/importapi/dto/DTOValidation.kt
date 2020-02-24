package no.nav.arbeidsplassen.importapi.dto

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import no.nav.arbeidsplassen.importapi.ApiError
import no.nav.arbeidsplassen.importapi.ErrorType
import org.slf4j.LoggerFactory
import java.lang.Exception
import javax.inject.Singleton

@Singleton
class DTOValidation(private val objectMapper: ObjectMapper) {

//    companion object {
//        private val LOG = LoggerFactory.getLogger(DTOValidation::class.java)
//    }
//
//    fun parseJson(json: String): JsonNode {
//        try {
//            return objectMapper.readTree(json)
//        }
//        catch (e: Exception) {
//            when (e) {
//                is JsonParseException -> throw ApiError("Parse error: ${e.localizedMessage}", ErrorType.PARSE_ERROR)
//                else -> {
//                    LOG.error("Got unknown exception:", e)
//                    throw ApiError("Unknown error: ${e.localizedMessage}", ErrorType.UNKNOWN)
//                }
//            }
//        }
//    }
//
//    fun parseToDTO(jsonNode: JsonNode): TransferDTO {
//        try {
//            return objectMapper.treeToValue(jsonNode, TransferDTO::class.java)
//        }
//        catch (e: Exception) {
//            when (e) {
//                is MissingKotlinParameterException -> throw ApiError("Missing parameter: ${e.parameter.name}", ErrorType.MISSING_PARAMETER)
//                else -> {
//                    LOG.error("Got unknown exception:", e)
//                    throw ApiError("Unknown error: ${e.localizedMessage}", ErrorType.UNKNOWN)
//                }
//            }
//        }
//    }

}
