package ch.lucaro.gswatch

import ch.lucaro.gswatch.data.GoogleScholarPage
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File
import kotlin.system.exitProcess

object Main {

    @JvmStatic
    fun main(args: Array<String>) {

        if (args.isEmpty()) {
            System.err.println("no parameters specified")
            exitProcess(-1)
        }

        val quietMode = args.any { it == "-q" || it == "--quiet" }
        val noStoreMode = args.any { it == "-n" || it == "--no-store" }
        val statisticsMode = args.any { it == "-s" || it == "--statistics" }

        val userIds = args.filter { it.length == 12 && !it.startsWith("-") }


        if (userIds.isEmpty()) {
            System.err.println("no google scholar user id specified")
            exitProcess(-1)
        }

        val gss = GoogleScholarScraper()

        val mapper = jacksonObjectMapper().enable(SerializationFeature.INDENT_OUTPUT)

        val baseDir = File("store")
        if (!noStoreMode) {
            baseDir.mkdir()
        }

        val pages = mutableMapOf<String, GoogleScholarPage>()

        for (userId in userIds) {

            val page = gss.scrapeProfilePage(userId)

            pages[userId] = page

            val mostRecentPage = baseDir.listFiles { file: File -> file.isFile && file.name.startsWith(userId) }
                ?.maxByOrNull { it.name.split(".")[1].toLong() }
                ?.run { mapper.readValue<GoogleScholarPage>(this) }

            if (!noStoreMode) {
                val referenceDir = File(baseDir, userId)
                referenceDir.mkdir()
                mapper.writeValue(File(baseDir, "${userId}.${System.currentTimeMillis()}.json"), page)
            }

            if (!quietMode) {
                var changes = false

                if (mostRecentPage != null) {
                    if (mostRecentPage.totalCitations != page.totalCitations) {
                        println("found difference in citation count: was ${mostRecentPage.totalCitations}, is ${page.totalCitations} now")
                        changes = true
                    }
                    page.publications.forEach { publication ->
                        val lastPublication = mostRecentPage.publications.find { it.id == publication.id }
                        if (lastPublication == null) {
                            println("found new publication: $publication")
                            changes = true
                        } else if (lastPublication.citationCount != publication.citationCount) {
                            println("found difference in citation count for '${publication.title}', was ${lastPublication.citationCount}, is ${publication.citationCount} now")
                            println("see https://scholar.google.ch/scholar?&cites=${publication.reference}&scisbd=1 for more information")
                            changes = true
                        }
                    }
                    if (!changes) {
                        println("found no changes to previous profile version")
                    }
                } else {
                    println("found no prior version of profile '${userId}', nothing to compare to")
                }
            }

        }

        if (statisticsMode && pages.isNotEmpty()) {

            val writer = File("statistics.tsv").printWriter()
            writer.println("id\tname\tpublications\tcitations\th\ti10\tfirst_year\tlast_year")

            pages.forEach { (id, page) ->
                writer.println("$id\t${page.name}\t${page.publications.size}\t${page.totalCitations}\t${page.h}\t${page.i10}\t${page.publications.minBy { it.year }.year}\t${page.publications.maxBy { it.year }.year}")
            }

            writer.flush()
            writer.close()

        }

    }

}