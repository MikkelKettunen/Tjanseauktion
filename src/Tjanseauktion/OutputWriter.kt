package Tjanseauktion

import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfWriter


import java.io.FileOutputStream
import java.util.ArrayList

/**
 * Created by chrae on 02-09-2017.
 */
class OutputWriter(private val filename: String, private val teams: ArrayList<Team>) {
    private val titleFont = Font(Font.FontFamily.HELVETICA, 20f, Font.BOLD)
    private val teamHeadlineFont = Font(Font.FontFamily.HELVETICA, 16f, Font.BOLD)
    private val infoFont = Font(Font.FontFamily.HELVETICA, 12f, Font.NORMAL)
    private var document: Document? = null

    fun writeOutput() {
        try {
            document = Document(PageSize.A4)
            PdfWriter.getInstance(document, FileOutputStream(filename))
            document!!.open()
            addContent()
            document!!.close()
        } catch (e: Exception) {
            println("There was an error creating the output file")
            e.printStackTrace()

        }

    }

    @Throws(DocumentException::class)
    private fun addContent() {
        val content = Paragraph()
        // We add one empty line
        addEmptyLine(content, 1)
        // Lets write a big header
        content.add(Paragraph("Resultat af tjanseauktion", titleFont))
        addEmptyLine(content, 1)
        for (t in teams) {
            content.add(Paragraph(t.name, teamHeadlineFont))
            t.chores.mapTo(content) { Paragraph(it, infoFont) }

            addEmptyLine(content, 1)
        }

        document!!.add(content)
    }

    private fun addEmptyLine(paragraph: Paragraph, number: Int) {
        for (i in 0 until number) {
            paragraph.add(Paragraph(" "))
        }
    }
}
