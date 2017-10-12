package Tjanseauktion

/**
 * Created by chrae on 28-08-2017.
 */
class Auction(val chore: String) {
    var bid = -1
        private set
    var bidder = -1
        private set
    var isSecret = false
        private set
    private var hasBid = false
    var isCompleted = false
        private set

    val logOutput: String
        get() = "$chore;$bid;$bidder;$isSecret;$hasBid;$isCompleted"

    fun getChoreAsString(): String {
        return if (isSecret) "SECRET - " + chore else chore
    }

    fun setSecret() {
        isSecret = true
    }

    fun bid(bid: Int, bidder: Int): Boolean {
        if (bid <= this.bid)
            return false
        this.bid = bid
        this.bidder = bidder
        hasBid = true
        return true
    }

    fun hasBid(): Boolean {
        return hasBid
    }

    fun complete() {
        isCompleted = true
    }

    override fun toString(): String {
        var s = ""
        if (isSecret)
            s = "SECRET - "
        s += if (bidder != 0)
            "$chore $bid $bidder"
        else
            chore + " NOBID"
        return s
    }
}
