package com.example.service.gemini

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Domain & DTO models for structured study content generated from study notes.
 */

@JsonClass(generateAdapter = true)
data class KeyConceptItem(
    @Json(name = "concept")
    val concept: String,
    @Json(name = "explanation")
    val explanation: String
)

@JsonClass(generateAdapter = true)
data class DefinitionItem(
    @Json(name = "term")
    val term: String,
    @Json(name = "definition")
    val definition: String
)

@JsonClass(generateAdapter = true)
data class RevisionQuestionItem(
    @Json(name = "question")
    val question: String,
    @Json(name = "answer")
    val answer: String
)

@JsonClass(generateAdapter = true)
data class StudySummaryResult(
    @Json(name = "title")
    val title: String = "",
    @Json(name = "overview")
    val overview: String = "",
    @Json(name = "keyConcepts")
    val keyConcepts: List<KeyConceptItem> = emptyList(),
    @Json(name = "importantPoints")
    val importantPoints: List<String> = emptyList(),
    @Json(name = "definitions")
    val definitions: List<DefinitionItem> = emptyList(),
    @Json(name = "examTakeaways")
    val examTakeaways: List<String> = emptyList(),
    @Json(name = "quickRevisionQuestions")
    val quickRevisionQuestions: List<RevisionQuestionItem> = emptyList()
) {
    /**
     * Converts structured summary into clean, readable markdown for rendering or storage
     */
    fun toFormattedMarkdown(): String {
        val sb = StringBuilder()
        if (title.isNotBlank()) {
            sb.append("# ").append(title).append("\n\n")
        }
        if (overview.isNotBlank()) {
            sb.append("### Overview\n").append(overview).append("\n\n")
        }
        if (keyConcepts.isNotEmpty()) {
            sb.append("### Key Concepts\n")
            keyConcepts.forEach { item ->
                sb.append("- **").append(item.concept).append("**: ").append(item.explanation).append("\n")
            }
            sb.append("\n")
        }
        if (definitions.isNotEmpty()) {
            sb.append("### Key Definitions\n")
            definitions.forEach { def ->
                sb.append("- **").append(def.term).append("**: ").append(def.definition).append("\n")
            }
            sb.append("\n")
        }
        if (importantPoints.isNotEmpty()) {
            sb.append("### Important Points\n")
            importantPoints.forEach { point ->
                sb.append("- ").append(point).append("\n")
            }
            sb.append("\n")
        }
        if (examTakeaways.isNotEmpty()) {
            sb.append("### High-Yield Exam Takeaways\n")
            examTakeaways.forEach { takeaway ->
                sb.append("★ ").append(takeaway).append("\n")
            }
            sb.append("\n")
        }
        if (quickRevisionQuestions.isNotEmpty()) {
            sb.append("### Quick Revision Questions\n")
            quickRevisionQuestions.forEachIndexed { index, q ->
                sb.append("**Q").append(index + 1).append("**: ").append(q.question).append("\n")
                sb.append("**A**: ").append(q.answer).append("\n\n")
            }
        }
        return sb.toString().trim()
    }
}

@JsonClass(generateAdapter = true)
data class FlashcardItem(
    @Json(name = "front")
    val front: String,
    @Json(name = "back")
    val back: String,
    @Json(name = "hint")
    val hint: String? = null,
    @Json(name = "keyConcept")
    val keyConcept: String? = null
)

@JsonClass(generateAdapter = true)
data class FlashcardListWrapper(
    @Json(name = "flashcards")
    val flashcards: List<FlashcardItem> = emptyList()
)

@JsonClass(generateAdapter = true)
data class ProcessedStudyNotesResult(
    @Json(name = "summary")
    val summary: StudySummaryResult,
    @Json(name = "flashcards")
    val flashcards: List<FlashcardItem> = emptyList(),
    @Json(name = "suggestedQuizTopics")
    val suggestedQuizTopics: List<String> = emptyList()
)
