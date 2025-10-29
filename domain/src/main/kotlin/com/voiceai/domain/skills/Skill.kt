package com.voiceai.domain.skills

/**
 * Interface for AI skills (function calling / tool use).
 * Skills enable the AI to perform actions beyond text generation.
 */
interface Skill {
    /**
     * Unique identifier for this skill
     */
    val name: String

    /**
     * Human-readable description of what this skill does
     */
    val description: String

    /**
     * Parameters this skill accepts
     */
    val parameters: List<Parameter>

    /**
     * Execute the skill with given parameters
     */
    suspend fun execute(params: Map<String, Any>): SkillResult

    /**
     * Validate parameters before execution
     */
    suspend fun validate(params: Map<String, Any>): ValidationResult

    /**
     * Estimate execution time (for routing decisions)
     */
    fun estimateLatencyMs(): Long = 500L
}

/**
 * Parameter definition for a skill
 */
data class Parameter(
    val name: String,
    val type: ParameterType,
    val description: String,
    val required: Boolean = true,
    val defaultValue: Any? = null,
    val validation: ParameterValidation? = null
)

enum class ParameterType {
    STRING,
    INTEGER,
    FLOAT,
    BOOLEAN,
    ENUM,
    OBJECT,
    ARRAY
}

/**
 * Validation rules for parameters
 */
data class ParameterValidation(
    val minValue: Number? = null,
    val maxValue: Number? = null,
    val minLength: Int? = null,
    val maxLength: Int? = null,
    val pattern: String? = null,
    val allowedValues: List<Any>? = null
)

/**
 * Result of skill execution
 */
sealed class SkillResult {
    data class Success(
        val data: Any,
        val message: String = "",
        val executionTimeMs: Long
    ) : SkillResult()

    data class Failure(
        val error: SkillError,
        val message: String,
        val retryable: Boolean = false
    ) : SkillResult()
}

enum class SkillError {
    INVALID_PARAMETERS,
    MISSING_PERMISSION,
    NETWORK_ERROR,
    TIMEOUT,
    RATE_LIMITED,
    SERVICE_UNAVAILABLE,
    UNKNOWN_ERROR
}

/**
 * Validation result for parameters
 */
sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val errors: List<String>) : ValidationResult()
}

/**
 * Registry for managing available skills
 */
interface SkillRegistry {
    /**
     * Register a new skill
     */
    fun register(skill: Skill)

    /**
     * Get skill by name
     */
    fun getSkill(name: String): Skill?

    /**
     * Get all registered skills
     */
    fun getAllSkills(): List<Skill>

    /**
     * Get skills matching a query
     */
    fun searchSkills(query: String): List<Skill>

    /**
     * Get formatted description for LLM prompt
     */
    fun getSkillsPrompt(): String
}

/**
 * Context passed to skills during execution
 */
data class SkillExecutionContext(
    val userId: String,
    val sessionId: String,
    val conversationHistory: List<String> = emptyList(),
    val userLocation: Location? = null,
    val deviceCapabilities: DeviceCapabilities = DeviceCapabilities()
)

data class Location(
    val latitude: Double,
    val longitude: Double,
    val city: String? = null,
    val country: String? = null
)

data class DeviceCapabilities(
    val hasInternet: Boolean = true,
    val hasGPS: Boolean = true,
    val hasCamera: Boolean = true,
    val hasMicrophone: Boolean = true
)
