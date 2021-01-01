package ch.lucaro.gswatch.data

data class GoogleScholarPage(val user: String, val totalCitations: Int, val citationStatistics: Map<Int, Int>, val publications: List<PublicationOverview>)
