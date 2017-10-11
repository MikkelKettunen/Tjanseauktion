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

    init {
        try {
            val reader = BufferedReader(FileReader(filename))
            var line: String?
            var isAuction = false
            var strs: Array<String>
            line = reader.readLine()
            while (line != null) {
                if (line == "auctions") {
                    isAuction = true
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

                        for (t in teams) {
                            if (t.num == Integer.parseInt(strs[2]))
                                t.addChore(strs[0])
                        }
                    }

                    auctions.add(auction)
                }
                line = reader.readLine()
            }
        } catch (e: IOException) {
            println("Error reading the log file.")
        } catch (e: IndexOutOfBoundsException) {
            println("File is incorrect format.")
        }

    }
}
