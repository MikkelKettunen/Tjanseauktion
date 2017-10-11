package Tjanseauktion

import java.util.ArrayList

/**
 * Created by chrae on 28-08-2017.
 */
class Team(val name: String, val num: Int) {
    var coins: Int = 0
    val chores = ArrayList<String>()

    //ToDo: A high valuta is worth 493 this year. should be changed!
    //ToDo: A mid valuta is worth 29 this year. should be changed!
    val coinString: String
        get() {
            val high = Math.floor((coins / 493).toDouble()).toInt()
            val mid = Math.floor((coins % 493 / 29).toDouble()).toInt()
            val low = coins % 493 % 29

            return high.toString() + "." + mid + "." + low
        }

    val logOutput: String
        get() = num.toString() + ";" + name + ";" + coins

    init {
        //ToDo: Remember to update!
        coins = 493 + 72 * 29 + 392
    }

    fun buyChore(chore: String, coins: Int) {
        chores.add(chore)
        this.coins -= coins
    }

    fun canAfford(amount: Int): Boolean {
        return coins >= amount
    }

    fun addChore(chore: String) {
        chores.add(chore)
    }

    override fun toString(): String {
        var s = num.toString() + " " + name + " " + coinString

        for (c in chores) {
            s += " " + c
        }

        return s
    }
}
