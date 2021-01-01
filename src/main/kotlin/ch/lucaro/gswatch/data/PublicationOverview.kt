package ch.lucaro.gswatch.data

data class PublicationOverview(val id: String, val title: String, val authors: List<String>, val venue: String, val year: Int, val citationCount: Int, val reference: String)
