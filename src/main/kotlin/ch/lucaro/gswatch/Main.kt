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

        val userId = args.firstOrNull()

        if (userId == null){
            System.err.println("no google scholar user id specified")
            exitProcess(-1)
        }

        val mapper = jacksonObjectMapper().enable(SerializationFeature.INDENT_OUTPUT)

        val baseDir = File("store")

        val gss = GoogleScholarScraper()

        val referenceDir = File(baseDir, userId)
        referenceDir.mkdir()

        val mostRecentPage = baseDir.listFiles { file: File -> file.isFile && file.name.startsWith(userId) }.maxByOrNull { it.name.split(".")[1].toLong() }?.run { mapper.readValue<GoogleScholarPage>(this) }

        val page = gss.scrapeProfilePage(userId)

        mapper.writeValue(File(baseDir, "${userId}.${System.currentTimeMillis()}.json"), page)

        var changes = false

        if (mostRecentPage != null) {
            if(mostRecentPage.totalCitations != page.totalCitations){
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