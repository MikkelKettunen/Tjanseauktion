package Tjanseauktion

import java.io.BufferedWriter
import java.io.FileWriter
import java.io.IOException
import java.util.ArrayList

/**
 * Created by chrae on 29-08-2017.
 */
class CLI(private var auctions: ArrayList<Auction>, private var teams: ArrayList<Team>, private val num_free: Int, private val num_pr_gr: Int) {
    private var writer: Writer? = null
    var quit = false
    private var auctionStarted = false
    private var endAuction = false
    private var currentAuction: Auction? = null
    private val logWriter = LogWriter()

    fun start() {
        writer = Writer()
        while (!quit) {
            try {
                ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor()
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

            writer!!.writeIntro(auctions!!.size, num_free, num_pr_gr)
            writer!!.writeTeamList(teams)
            writer!!.writeInputPrompt()
            getCommand()
        }
    }

    private fun getCommand() {
        val command = System.console().readLine()

        if (command.startsWith(":"))
            handleAdminCommand(command)
        else
            handleAuctionInput(command)
    }

    private fun handleAdminCommand(command: String) {
        when (command) {
            ":q" -> quit = true
            ":quit" -> quit = true
            ":start" -> startAuctions()
            ":s" -> startAuctions()
            ":break" -> breakAuctions()
            ":b" -> breakAuctions()
            ":end" -> end()
            ":e" -> end()
            ":load" -> readLog()
            ":l" -> readLog()
            else -> {
                writer!!.writeUnknownCommand()
                getCommand()
            }
        }
    }

    private fun readLog() {
        val reader = LogReader("log.txt")
        teams = reader.teams
        auctions = reader.auctions
        writer!!.writeFileLoaded()
        start()
    }

    private fun end() {
        if (!auctionStarted) {
            writer!!.writeNoAuction()
            getCommand()
            return
        }
        if (!currentAuction!!.hasBid()) {
            writer!!.writeAuctionHasNoBid()
            getCommand()
            return
        }
        endAuction = true
        currentAuction!!.complete()
    }

    private fun handleAuctionInput(input: String) {
        if (!input.matches("^[0-9].*$".toRegex())) {
            writer!!.writeUnknownCommand()
            getCommand()
            return
        }
        if (!auctionStarted) {
            writer!!.writeNoAuction()
            getCommand()
            return
        }

        val commands = input.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        var team: Team? = null
        for (t in teams!!) {
            if (t.num == Integer.parseInt(commands[0]))
                team = t
        }

        if (team == null) {
            writer!!.writeTeamNotFound()
            getCommand()
            return
        }

        val bid: Int
        try {
            bid = decodeBidString(commands[1])
        } catch (e: IndexOutOfBoundsException) {
            writer!!.writeUnknownCommand()
            getCommand()
            return
        } catch (e: NumberFormatException) {
            writer!!.writeUnknownCommand()
            getCommand()
            return
        }

        if (!team.canAfford(bid)) {
            writer!!.writeCannotAfford(team.name)
            getCommand()
            return
        }
        if (!currentAuction!!.bid(bid, team.num)) {
            writer!!.writeIsNotEnough()
            getCommand()
        }
    }

    private fun breakAuctions() {
        if (!auctionStarted) {
            writer!!.writeNoAuction()
            getCommand()
            return
        }

        auctionStarted = false
    }

    private fun startAuctions() {
        if (auctionStarted) {
            writer!!.writeAuction()
            getCommand()
            return
        }

        try {
            BufferedWriter(FileWriter("log.txt", false)).close()
        } catch (e: Exception) {
            println("There was an error creating the log file")
        }

        auctionStarted = true
        var n = 1
        for (a in auctions!!) {
            if (!auctionStarted || quit)
                break
            currentAuction = a
            if (a.isCompleted) {
                n++
                saveStatus()
                continue
            }

            while (!endAuction && !quit && auctionStarted) {
                try {
                    ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor()
                } catch (e: IOException) {
                    e.printStackTrace()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }

                writer!!.writeAuctionInfo(currentAuction!!.chore, n, auctions!!.size)
                if (currentAuction!!.hasBid()) {
                    for (t in teams!!) {
                        if (t.num == currentAuction!!.bidder)
                            writer!!.writeCurrentBid(encodeBidString(currentAuction!!.bid), t.name)
                    }
                }
                writer!!.writeTeamList(teams)
                writer!!.writeInputPrompt()
                getCommand()
            }

            n++
            if (endAuction) {
                teams!!
                        .filter { it.num == currentAuction!!.bidder }
                        .forEach { it.buyChore(a.chore, currentAuction!!.bid) }
            }

            endAuction = false

            saveStatus()
        }

        if (!quit) {
            val outputWriter = OutputWriter("Tjanser.pdf", teams!!)
            outputWriter.writeOutput()

            writer!!.writeOutput()
            getCommand()
        }
    }

    private fun saveStatus() {
        logWriter.writeStatus(auctions, teams)
    }

    private fun decodeBidString(bid: String): Int {
        val coins = bid.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        return Integer.parseInt(coins[0]) * 493 + Integer.parseInt(coins[1]) * 29 + Integer.parseInt(coins[2])
    }

    private fun encodeBidString(bid: Int): String {
        //ToDo: A high valuta is worth 493 this year. should be changed!
        val high = Math.floor((bid / 493).toDouble()).toInt()
        //ToDo: A mid valuta is worth 29 this year. should be changed!
        val mid = Math.floor((bid % 493 / 29).toDouble()).toInt()
        val low = bid % 493 % 29

        return high.toString() + "." + mid + "." + low
    }
}
