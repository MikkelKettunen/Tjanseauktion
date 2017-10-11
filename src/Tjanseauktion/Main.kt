package Tjanseauktion

import javafx.application.Application
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.Background
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.stage.Stage
import java.io.*
import java.util.*

class Program(private val chores: List<String>,
              teamsInput: List<String>,
              secrets: Int) {

    private var num_chores: Int = 0
    var teams = ArrayList<Team>()
    private var auctions = ArrayList<Auction>()
    private var randomGenerator = Random()

    init {
        teamsInput.forEachIndexed { index, teamName ->
            teams.add(Team(teamName, index))
        }
        setupSecrets(secrets)
    }

    private fun setupSecrets(num_secret: Int): Int {
        for (s in chores) {
            auctions.add(Auction(s))
        }

        for (i in 0..num_secret - 1) {
            var index = randomGenerator.nextInt(auctions.size)
            while (auctions[index].isSecret) {
                index = randomGenerator.nextInt(auctions.size)
            }

            auctions[index].setSecret()
        }

        // Calculate number of chores per team
        num_chores = Math.ceil(chores.size.toDouble() / teams.size).toInt()
        // Calculate number of free chores
        val num_free_chores = num_chores * teams.size % chores.size

        //Insert free chores
        (0 until num_free_chores).forEach { i ->
            val a = Auction("Fritjans")
            a.setSecret()
            auctions.add(a)
        }
        Collections.shuffle(auctions)

        return num_free_chores
    }
}


class Main : Application() {
    private var program: Program? = null

    private val root = StackPane()
    private val vBox = VBox()

    init {
        vBox.alignment = Pos.CENTER
        vBox.padding = Insets(10.0)
        vBox.spacing = 8.0
    }

    override fun start(primaryStage: Stage?) {
        if (primaryStage == null) return

        //program.run("/Input/Chores.txt", "/Input/Secrets.txt")

        createStartPage()

        root.children.addAll(vBox)
        root.alignment = Pos.CENTER

        primaryStage.title = "Tjanseauktion"
        primaryStage.scene = Scene(root, 1000.0, 1000.0)
        primaryStage.show()
    }

    private fun createTeam(team: Team): HBox {
        val hBox = HBox()
        val nameLabel = Label()
        nameLabel.text = team.name
        nameLabel.maxWidth = 200.0
        nameLabel.minWidth = 200.0

        val moneyLabel = Label()
        moneyLabel.text = team.coins.toString()


        hBox.alignment = Pos.CENTER
        hBox.children.addAll(nameLabel, moneyLabel)
        return hBox
    }

    private fun createAuctionPage() {
        val program = program ?: return // something went very wrong if we did this

        vBox.children.clear()

        for (team in program.teams) {
            vBox.children.add(createTeam(team))
        }


        //vBox.children.addAll()
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

        val (secretInput, secretHBox) = createInputBox("Secrets")
        val (choresInput, choresHBox) = createInputBox("Chores file")
        val (teamsInput, teamsHBox) = createInputBox("Teams file")

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

            program = Program(choresAsList, teamsAsList, secret)

            createAuctionPage()

            println("done")
        }




        vBox.children.clear()
        vBox.children.addAll(choresHBox, teamsHBox, secretHBox, button, currentDirectory, errorMessage)
    }


    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            Application.launch(Main::class.java)
        }
    }

}


private fun String.toIntOrNull(): Int? {
    return try {
        toInt()
    } catch (e: Exception) {
        null
    }
}

