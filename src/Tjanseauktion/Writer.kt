package Tjanseauktion

import java.util.ArrayList

/**
 * Created by chrae on 29-08-2017.
 */
class Writer {
    fun writeIntro(totalAuctions: Int, num_free: Int, num_pr: Int) {
        println("Welcome. There are $totalAuctions auctions, $num_free free chores.\nEach team must have $num_pr chores.\nThe Teams are:")
    }

    fun writeCurrentBid(bid: String, bidder: String) {
        println("Current bid: " + bid)
        println("Current bidder: " + bidder)
    }

    fun writeAuctionInfo(name: String, auctionNum: Int, totalAuctions: Int) {
        println(name)
        println("Auction $auctionNum/$totalAuctions")
    }

    fun writeTeamList(teams: ArrayList<Team>) {
        val leftAlignFormat = "| %-3d | %-28s | %-8s | %-6d |%n"

        System.out.format("+-----+------------------------------+----------+--------+%n")
        System.out.format("| Num | Name                         | Coins    | Chores |%n")
        System.out.format("+-----+------------------------------+----------+--------+%n")
        for (t in teams) {
            System.out.format(leftAlignFormat, t.num, t.name, t.coinString, t.chores.size)
        }
        System.out.format("+-----+------------------------------+----------+--------+%n")
    }

    fun writeInputPrompt() {
        println("Write command: ")
    }

    fun writeUnknownCommand() {
        println("Unknown command. Try again:")
    }

    fun writeNoAuction() {
        println("No auction has started! Try again:")
    }

    fun writeAuction() {
        println("Auctions has already started! Try again:")
    }

    fun writeTeamNotFound() {
        println("Team not found. Please try again:")
    }

    fun writeCannotAfford(name: String) {
        println(name + " cannot afford this! Try again:")
    }

    fun writeIsNotEnough() {
        println("Current bid is not enough. Try again:")
    }

    fun writeAuctionHasNoBid() {
        println("Current auction has no bid. Try again:")
    }

    fun writeFileLoaded() {
        println("Logfile has successfully loaded. Input command:")
    }

    fun writeOutput() {
        println("Output file has been succesfully written. Input command:")
    }
}
