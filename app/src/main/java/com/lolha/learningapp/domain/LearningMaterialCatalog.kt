package com.lolha.learningapp.domain

data class LearningMaterial(
    val id: String,
    val subject: String,
    val title: String,
    val description: String,
    val level: String,
    val url: String,
    val notes: String = "",
)

object LearningMaterialCatalog {
    val materials = listOf(
        LearningMaterial(
            id = "jp-nhk-easy",
            subject = "japanese",
            title = "NHK News Web Easy",
            description = "Short Japanese news written for learners, useful for daily reading and vocabulary.",
            level = "Beginner-Intermediate",
            url = "https://www3.nhk.or.jp/news/easy/",
            notes = "Good for 10-15 minute reading practice.",
        ),
        LearningMaterial(
            id = "jp-tadoku",
            subject = "japanese",
            title = "Tadoku Free Graded Readers",
            description = "Free graded Japanese reading materials with simple stories.",
            level = "Beginner",
            url = "https://tadoku.org/japanese/free-books/",
            notes = "Use for reading fluency before harder grammar work.",
        ),
        LearningMaterial(
            id = "en-british-council",
            subject = "english",
            title = "British Council LearnEnglish",
            description = "Structured English reading, listening, grammar, and vocabulary practice.",
            level = "Beginner-Advanced",
            url = "https://learnenglish.britishcouncil.org/",
            notes = "Good default source for English homework support.",
        ),
        LearningMaterial(
            id = "en-purdue-owl",
            subject = "english",
            title = "Purdue OWL Writing Lab",
            description = "Writing, grammar, essay structure, and citation guidance.",
            level = "Intermediate-Advanced",
            url = "https://owl.purdue.edu/",
            notes = "Use for English writing revision tasks.",
        ),
        LearningMaterial(
            id = "draw-line-of-action",
            subject = "drawing",
            title = "Line of Action",
            description = "Timed gesture drawing practice for figures, hands, animals, and expressions.",
            level = "Beginner-Advanced",
            url = "https://line-of-action.com/",
            notes = "Useful for short daily drawing drills.",
        ),
        LearningMaterial(
            id = "draw-drawabox",
            subject = "drawing",
            title = "Drawabox",
            description = "Structured fundamentals for line control, form, perspective, and construction.",
            level = "Beginner-Intermediate",
            url = "https://drawabox.com/",
            notes = "Best for disciplined fundamentals assignments.",
        ),
        LearningMaterial(
            id = "fit-ace",
            subject = "fitness",
            title = "ACE Exercise Library",
            description = "General exercise references with movement instructions.",
            level = "General",
            url = "https://www.acefitness.org/resources/everyone/exercise-library/",
            notes = "Use only for general training, not medical guidance.",
        ),
    )

    fun forSubject(subject: String): List<LearningMaterial> =
        materials.filter { it.subject.equals(subject, ignoreCase = true) }

    fun promptReference(): String =
        materials.joinToString(separator = "\n") { material ->
            "- ${material.id}: ${material.subject}, ${material.title}, ${material.level}, ${material.url}"
        }
}

