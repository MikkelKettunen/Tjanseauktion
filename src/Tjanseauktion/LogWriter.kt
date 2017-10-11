package Tjanseauktion

import java.io.BufferedWriter
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.util.ArrayList

/**
 * Created by chrae on 31-08-2017.
 */
class LogWriter {
    fun writeStatus(auctions: ArrayList<Auction>, teams: ArrayList<Team>) {
        try {
            val logFile = BufferedWriter(OutputStreamWriter(FileOutputStream("log.txt"), StandardCharsets.UTF_8))

            for (t in teams) {
                logFile.write(t.logOutput)
                logFile.newLine()
            }
            logFile.write("auctions")
            logFile.newLine()
            for (a in auctions) {
                logFile.write(a.logOutput)
                logFile.newLine()
            }
            logFile.close()
        } catch (e: Exception) {
            println("There was an error writing the log file")
        }

    }
}
