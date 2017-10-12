package Tjanseauktion

import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException
import java.util.ArrayList

/**
 * Created by chrae on 02-09-2017.
 */
class LogReader(filename: String) {
    val teams = ArrayList<Team>()
    val auctions = ArrayList<Auction>()
    val successfully: Boolean

    init {
        var wasSuccessFull = true
        try {
            val reader = BufferedReader(FileReader(filename))
            var line: String?
            var isAuction = false
            var strs: Array<String>
            line = reader.readLine()
            while (line != null) {
                if (line == "auctions") {
                    isAuction = true
                    line = reader.readLine()
                    continue
                }

                if (!isAuction) {
                    strs = line.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                    val team = Team(strs[1], Integer.parseInt(strs[0]))
                    team.coins = Integer.parseInt(strs[2])
                    teams.add(team)
                } else {
                    strs = line.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                    val auction = Auction(strs[0])
                    val isSecret = java.lang.Boolean.parseBoolean(strs[3])
                    if (isSecret)
                        auction.setSecret()

                    val isComplete = java.lang.Boolean.parseBoolean(strs[5])
                    if (isComplete) {
                        auction.bid(Integer.parseInt(strs[1]), Integer.parseInt(strs[2]))
                        auction.complete()

                        teams
                                .filter { it.num == Integer.parseInt(strs[2]) }
                                .forEach { it.addChore(strs[0]) }
                    }

                    auctions.add(auction)
                }
                line = reader.readLine()
            }
        } catch (e: IOException) {
            wasSuccessFull = false
            println("Error reading the log file.")
        } catch (e: IndexOutOfBoundsException) {
            wasSuccessFull = false
            println("File is incorrect format.")
        }

        successfully = wasSuccessFull

    }
}
