package ch.lucaro.gswatch.data

data class GoogleScholarPage(val user: String, val name: String = "", val totalCitations: Int, val h: Int = 0, val i10: Int = 0, val citationStatistics: Map<Int, Int>, val publications: List<PublicationOverview>)
