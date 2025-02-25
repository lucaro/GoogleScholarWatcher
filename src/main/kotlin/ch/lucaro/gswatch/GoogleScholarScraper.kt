package ch.lucaro.gswatch

import ch.lucaro.gswatch.data.GoogleScholarPage
import ch.lucaro.gswatch.data.PublicationOverview
import ch.lucaro.gswatch.data.Reference
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

class GoogleScholarScraper {

    private val userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:84.0) Gecko/20100101 Firefox/84.0"
    private var lastRequest = "https://scholar.google.com"

    private fun get(url: String): Document {

        val doc = Jsoup.connect(url)
            .userAgent(userAgent)
            .referrer(lastRequest)
            .get()

        lastRequest = url

        return doc

    }

    fun scrapeProfilePage(userId: String): GoogleScholarPage {

        val doc = get("https://scholar.google.com/citations?&cstart=0&pagesize=10000&user=$userId")

        val stats = doc.select("#gsc_rsb_st td.gsc_rsb_std").toList().map { it.text().toIntOrNull() ?: 0 }

        val total = stats.getOrElse(0) { 0 }
        val h = stats.getOrElse(2) { 0 }
        val i10 = stats.getOrElse(4) { 0 }

        val name = doc.select("div#gsc_prf_in").first()?.text() ?: ""

        val citationStats =
            doc.select(".gsc_g_t").map { it.text().toInt() }
                .zip(doc.select(".gsc_g_al").map { it.text().toInt() })
                .toMap()

        val publications = doc.select(".gsc_a_tr").map {
            val id = it.select("a.gsc_a_at").attr("href").substringAfter("citation_for_view=").substringBefore("&")

            val title = it.select(".gsc_a_at").text()

            val gray = it.select(".gs_gray").map(Element::text)
            val authors = (gray.firstOrNull() ?: "unknown authors").split(",").map(String::trim)
            val venue = gray.getOrNull(1) ?: "unknown venue"

            val citationCount = it.select(".gsc_a_ac").text().toIntOrNull() ?: 0
            val year = it.select(".gsc_a_h").text().toIntOrNull() ?: 0

            val reference = it.selectFirst("a.gsc_a_ac")?.attr("href")?.substringAfter("cites=")?.substringBefore('&') ?: ""

            PublicationOverview(id, title, authors, venue, year, citationCount, reference)

        }

        return GoogleScholarPage(userId, name, total, h, i10, citationStats, publications)
    }

    fun scrapeReferencesPages(referenceId: String, expectedReferences: List<Reference> = emptyList()): List<Reference> {
        val references = mutableListOf<Reference>()

        do {
            val doc = get("https://scholar.google.ch/scholar?start=${references.size}&cites=${referenceId}") //&scisbd=1

            references += doc.select(".gs_r.gs_or.gs_scl").map {

                val title = it.select(".gs_rt").text()
                val info = it.select(".gs_a").text()
                val link = it.select(".gs_rt").select("a").attr("href")
                val directLink = it.select(".gs_or_ggsm").select("a").attr("href")

                Reference(title, info, link, directLink)

            }

            val hasNext = doc.select(".gs_ico.gs_ico_nav_next").isNotEmpty()

            if (hasNext){
                Thread.sleep(20000 + (Math.random() * 10000).toLong())
            }

        } while (hasNext)

        return references
    }

}