package Tjanseauktion

import javafx.application.Application
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.Separator
import javafx.scene.control.TextField
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import javafx.stage.Stage
import java.io.*
import java.util.*

val midWorth = 29
val highWorth = 493
val startCoins = 5000

val highCoinName = "High"
val midCoinName = "Mid"
val lowCoinName = "Low"

class Program {
    private val logWriter = LogWriter()

    var num_chores_per_team: Int = 0
        private set
    private var chores: List<String> = mutableListOf()
    var teams = ArrayList<Team>()
        private set
    private var auctions = ArrayList<Auction>()
    private var randomGenerator = Random()

    var currentAuction: Auction? = null

    fun start(inChores: List<String>,
              teamsInput: List<String>,
              secrets: Int) {

        chores = inChores

        teamsInput.forEachIndexed { index, teamName ->
            teams.add(Team(teamName, index))
        }
        setupSecrets(secrets)
    }

    private fun setupSecrets(input_num_secrets: Int): Int {
        val num_secret = Math.min(input_num_secrets, chores.size)

        for (s in chores) {
            auctions.add(Auction(s))
        }

        for (i in 0 until num_secret) {
            var index = randomGenerator.nextInt(auctions.size)
            while (auctions[index].isSecret) {
                index = randomGenerator.nextInt(auctions.size)
            }

            auctions[index].setSecret()
        }

        // Calculate number of chores per team
        num_chores_per_team = Math.ceil(chores.size.toDouble() / teams.size).toInt()
        // Calculate number of free chores
        val num_free_chores = num_chores_per_team * teams.size % chores.size

        //Insert free chores
        (0 until num_free_chores).forEach { i ->
            val a = Auction("Fritjans")
            a.setSecret()
            auctions.add(a)
        }

        Collections.shuffle(auctions)

        val monday = auctions.filter { it.chore.contains("Mandag") }
        val notMonday = auctions.filter { !it.chore.contains("Mandag") }

        auctions.clear()

        auctions.addAll(monday)
        auctions.addAll(notMonday)

        return num_free_chores
    }

    fun buildNextAuction() {
        currentAuction = auctions.firstOrNull { !it.isCompleted }
    }

    fun getTeam(teamID: Int): Team? = teams.firstOrNull { it.num == teamID }
    fun save() {
        logWriter.writeStatus(auctions, teams)
    }

    fun load(): Boolean {
        val logReader = LogReader("log.txt")
        if (logReader.successfully) {
            auctions = logReader.auctions
            teams = logReader.teams
        }

        return logReader.successfully
    }

}


class Main : Application() {
    private var program: Program? = null


    private val root = StackPane()
    private val vBox = VBox()
    private val errorMessage = Label()

    init {
        vBox.alignment = Pos.CENTER
        vBox.padding = Insets(10.0)
        vBox.spacing = 12.0
    }

    override fun start(primaryStage: Stage?) {
        if (primaryStage == null) return

        createStartPage()

        root.children.addAll(vBox)
        root.alignment = Pos.CENTER

        primaryStage.title = "Tjanseauktion"
        primaryStage.scene = Scene(root, 1000.0, 1000.0)
        primaryStage.show()
    }

    private fun createTeam(team: Team): HBox {
        val hBox = HBox()

        val teamChoresCount = Label()
        teamChoresCount.text = "Chores: ${team.chores.size}"
        teamChoresCount.minWidth = 100.0

        val teamID = Label()
        teamID.text = "ID: " + team.num.toString()
        teamID.minWidth = 100.0

        val nameLabel = Label()
        nameLabel.text = team.name
        nameLabel.maxWidth = 200.0
        nameLabel.minWidth = 200.0


        val moneyLabel = Label()
        moneyLabel.text = "${team.coinString} | ${team.coins}"


        hBox.alignment = Pos.CENTER
        hBox.children.addAll(teamChoresCount, teamID, nameLabel, moneyLabel)
        return hBox
    }

    private fun createAuctionPage() {
        val program = program ?: return // something went very wrong if we did this

        program.buildNextAuction()

        val currentAuction = program.currentAuction
        if (currentAuction == null) {
            // we're done!
            createShowWinners()
            return
        }

        vBox.children.clear()


        val currentBID = Label()
        currentBID.text = "current bid: " + currentAuction.getChoreAsString()
        currentBID.font = Font("arial", 20.0)
        vBox.children.add(currentBID)

        val currentBIDCoins = Label()
        val bidder = program.getTeam(currentAuction.bidder)?.name ?: "No bidder"
        currentBIDCoins.text = "'$bidder' bids '${currentAuction.bid}' coins"
        currentBIDCoins.font = Font("arial", 20.0)
        vBox.children.add(currentBIDCoins)

        createVerticalSeparator()

        program.teams.forEachIndexed { index, team ->
            vBox.children.add(createTeam(team))
            createVerticalSeparator()
        }

        val bidInput = HBox()
        bidInput.alignment = Pos.CENTER
        bidInput.spacing = 20.0

        val (highInput, highHBox) = createBidBox(highCoinName)
        val (midInput, midHBox) = createBidBox(midCoinName)
        val (lowInput, lowHBox) = createBidBox(lowCoinName)
        val (teamInput, teamBox) = createBidBox("team")


        bidInput.children.addAll(highHBox, midHBox, lowHBox, teamBox)
        vBox.children.add(bidInput)

        val lowerButtons = HBox()
        lowerButtons.alignment = Pos.CENTER
        lowerButtons.spacing = 100.0

        val bidButton = Button()
        bidButton.text = "bid"
        bidButton.onAction = EventHandler {
            val high = highInput.text.toIntOrNull()
            val mid = midInput.text.toIntOrNull()
            val low = lowInput.text.toIntOrNull()
            val teamID = teamInput.text.toIntOrNull()
            if (high == null || mid == null || low == null || teamID == null) {
                // print error message
                errorMessage.text = "Error bid not accepted, could not convert to ints"
                return@EventHandler
            }

            val team = program.getTeam(teamID)
            if (team == null) {
                errorMessage.text = "Error could not find team with id $teamID"
                return@EventHandler
            }


            val auction = program.currentAuction
            if (auction == null) {
                errorMessage.text = "No auction"
                return@EventHandler
            }

            val bidCoins = high * highWorth + mid * midWorth + low

            if (!team.canAfford(bidCoins)) {
                errorMessage.text = "Team ${team.name}, could not afford $bidCoins"
                return@EventHandler
            }

            if (team.chores.size >= program.num_chores_per_team) {
                errorMessage.text = "Team ${team.name} already have ${program.num_chores_per_team} chores"
            }

            if (auction.bid(bidCoins, teamID)) {
                errorMessage.text = "Team ${team.name} bid $bidCoins coins"
                createAuctionPage()
            } else {
                errorMessage.text = "Team ${team.name} didn't bid enough coins!"
            }

            program.save()
        }

        val soldButton = Button()
        soldButton.text = "sold"
        soldButton.onAction = EventHandler {
            val auction = program.currentAuction
            if (auction == null) {
                errorMessage.text = "no action found when selling"
                return@EventHandler
            }

            if (!auction.hasBid()) {
                errorMessage.text = "auction had no bids"
                return@EventHandler
            }

            val team = program.getTeam(auction.bidder)

            if (team == null) {
                errorMessage.text = "Could not find bidder team!"
                return@EventHandler
            }

            team.buyChore(auction.chore, auction.bid)

            auction.complete()

            createAuctionPage()

            errorMessage.text = "${team.name} won bid"
        }

        val cancelBidsButton = Button()
        cancelBidsButton.text = "Cancel bids"
        cancelBidsButton.onAction = EventHandler {
            val auction = program.currentAuction
            if (auction == null) {
                errorMessage.text = "no action found when selling"
                return@EventHandler
            }
            auction.cancelBid()
            createAuctionPage()
        }


        lowerButtons.children.addAll(cancelBidsButton, bidButton, soldButton)


        vBox.children.add(lowerButtons)


        vBox.children.add(errorMessage)

        val calculator = createCalculator()
        vBox.children.add(calculator)

        createVerticalSeparator()
    }

    private fun createCalculator(): Node {
        val vBox = VBox()
        val hBox = HBox()

        val bidInput = HBox()
        bidInput.alignment = Pos.CENTER
        bidInput.spacing = 20.0

        val (highInput, highHBox) = createBidBox(highCoinName)
        val (midInput, midHBox) = createBidBox(midCoinName)
        val (lowInput, lowHBox) = createBidBox(lowCoinName)
        val output = Label()
        output.text = "output: "

        hBox.children.addAll(highHBox, midHBox, lowHBox, output)

        val calculateButton = Button()
        calculateButton.text = "Calculate"
        calculateButton.onAction = EventHandler {
            val high = highInput.text.toIntOrNull()
            val mid = midInput.text.toIntOrNull()
            val low = lowInput.text.toIntOrNull()
            if (high == null) {
                errorMessage.text = "high not a number!"
                return@EventHandler
            }
            if (mid == null) {
                errorMessage.text = "mid not a number!"
                return@EventHandler
            }

            if (low == null) {
                errorMessage.text = "low not a number!"
                return@EventHandler
            }

            val coins = highWorth * high + mid * midWorth + low
            output.text =
                    "output: $coins"
        }

        vBox.children.addAll(hBox, calculateButton)

        hBox.alignment = Pos.CENTER

        return vBox
    }

    private fun createShowWinners() {
        vBox.children.clear()

        val program = program
        if (program == null) {
            println("should not happen!!")
            return
        }

        val writer = OutputWriter("Auctions.pdf", program.teams)
        writer.writeOutput()


        for (team in program.teams) {
            val vTeamBox = VBox()

            val teamName = Label()
            teamName.text = team.name
            teamName.minWidth = 100.0


            val hBox = HBox()
            for (chore in team.chores) {
                val label = Label()
                label.text = chore
                label.minWidth = 50.0

                hBox.children.add(label)
                hBox.padding = Insets(5.0)

                val separator = Separator()
                separator.orientation = Orientation.VERTICAL
                separator.minWidth = 10.0
                hBox.children.add(separator)
            }

            vTeamBox.children.addAll(teamName, hBox)
            createVerticalSeparator()

            vBox.children.addAll(vTeamBox, hBox)
        }

    }

    private fun createBidBox(name: String): Pair<TextField, HBox> {
        val hBox = HBox()
        hBox.alignment = Pos.CENTER

        val text = Label()
        text.text = name

        val inputField = TextField()

        hBox.children.addAll(text, inputField)

        return Pair(inputField, hBox)
    }

    private fun createStartPage() {
        val errorMessage = Label()
        errorMessage.text = ""

        val currentDirectory = Label()
        currentDirectory.text =
                "Current direction: " + System.getProperty("user.dir")

        val createInputBox = { text: String ->
            val textInputHBox = HBox()
            val textInput = TextField()
            val textInputLabel = Label()
            textInputLabel.text = text
            textInputLabel.minWidth = 100.0
            textInputHBox.children.addAll(textInputLabel, textInput)
            textInputHBox.spacing = 100.0
            textInputHBox.padding = Insets(10.0, 10.0, 10.0, 10.0)
            textInputHBox.alignment = Pos.CENTER
            Pair(textInput, textInputHBox)
        }

        val loadButton = Button()
        loadButton.text = "Load"
        loadButton.onAction = EventHandler {
            val tmpProgram = Program()
            if (tmpProgram.load()) {
                program = tmpProgram
                createAuctionPage()
            }
        }

        val (secretInput, secretHBox) = createInputBox("Secrets")
        val (choresInput, choresHBox) = createInputBox("Chores file")
        val (teamsInput, teamsHBox) = createInputBox("Teams file")
        choresInput.text = "C:\\Users\\mikkel\\Documents\\GitHub\\Tjanseauktion\\Input\\Chores"
        teamsInput.text = "C:\\Users\\mikkel\\Documents\\GitHub\\Tjanseauktion\\Input\\Teams"

        val button = Button()

        button.text = "Ok"

        button.onAction = EventHandler {
            val secretAsText = secretInput.text
            val secret = secretAsText?.toIntOrNull()

            if (secret == null) {
                errorMessage.text = "could not convert $secretAsText to an integer"
                return@EventHandler
            }

            val choresFile = choresInput.text

            var file = File(choresFile)
            if (!file.exists()) {
                errorMessage.text = "could not find choresFile " + choresFile
                errorMessage.isVisible = true
                return@EventHandler
            }

            val choresAsList = file.readLines()

            val teamsFile = teamsInput.text
            file = File(teamsFile)
            if (!file.exists()) {
                errorMessage.text = "could not find teamsFile: " + teamsFile
                errorMessage.isVisible = true
                return@EventHandler
            }

            errorMessage.text = ""

            val teamsAsList = file.readLines()

            program = Program()
            program?.start(choresAsList, teamsAsList, secret)

            createAuctionPage()

            println("done")
        }




        vBox.children.clear()
        vBox.children.addAll(loadButton, choresHBox, teamsHBox, secretHBox, button, currentDirectory, errorMessage)
    }


    private fun createVerticalSeparator() {
        vBox.children.add(Separator())
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            Application.launch(Main::class.java)
        }
    }

}


private fun String.toIntOrNull(): Int? {
    var result: Int?
    try {
        result = toInt()
        if (result < 0)
            result = null
    } catch (e: Exception) {
        result = null
    }
    return result
}

