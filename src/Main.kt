import Trilean.*
import com.googlecode.lanterna.SGR
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import com.googlecode.lanterna.terminal.Terminal
import kotlinx.coroutines.experimental.launch
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.fixedRateTimer


class KeyProcessor : KeyListener{
    override fun keyTyped(e: KeyEvent?) {
        when(e!!.keyChar){
            'x', 'X' -> {terminal?.exitPrivateMode();java.lang.System.exit(0)}
        }
    }

    override fun keyPressed(e: KeyEvent?) = Unit

    override fun keyReleased(e: KeyEvent?) = Unit

}


fun main(args: Array<String>) {
    initConfig()
    readConfig()
    saveConfig()
    tailerfr.delay
    tailerrc.delay
    color = Pair(TextColor.ANSI.CYAN, color.second)
    terminal = DefaultTerminalFactory().createTerminal()
    initTimers()


    launch { while(true) {
        val keyStroke = terminal?.pollInput() ?: continue
        if (keyStroke.keyType != KeyType.Character) continue
        when (keyStroke.character){
            'x', 'X' -> {
                terminal?.exitPrivateMode()
                java.lang.System.exit(0)
            }
        }
    }
    }

    launch{inpThr.run()}
    launch{
        WebSocket.instance().init()
        WebSocket.instance().request("rescues", "read")
    }

    while(true);

}
var screenupdate : Timer? = null
var tailerdog : Timer? = null
@Volatile
var terminal: Terminal? = null

fun initTimers(){
    screenupdate = fixedRateTimer("ScreenUpdater", true, 500, 1000, ::updateScreen)
    tailerdog = fixedRateTimer("TailerWatchdog", true, 500, 1000, ::checkTailer)
}


fun checkTailer(task : TimerTask) {
    if (frtailerStopped){
        frtailerStopped = false
        tailerfr.run()
    }
    if (rctailerStopped){
        rctailerStopped = false
        tailerrc.run()
    }

}

@Volatile
var toPrint = ArrayList<String>()

fun updateScreen(timerTask: TimerTask) {
    with(terminal!!) {
        enterPrivateMode()
        setBackgroundColor(TextColor.ANSI.BLACK)
        setForegroundColor(TextColor.ANSI.WHITE)
        clearScreen()
        setCursorPosition(0, 0)
        enableSGR(SGR.BOLD)
    }
    var linecount = 0
    terminal?.printString("Welcome to JAFRDB", terminal!!.terminalSize.columns / 2 - 17 /* welcome string length*/, 0)
    terminal?.printString("Cases: ", 2, 1)
    linecount = printRescues()

    val iter = toPrint.iterator()
    while(iter.hasNext()){
        val tmp = iter.next()
        terminal?.printString(tmp, 2, linecount)
        linecount++
        //iter.remove()
    }
}

fun Terminal.printString(input : String, x : Int, y : Int, color : Pair<TextColor, TextColor> = Pair(TextColor.ANSI.WHITE, TextColor.ANSI.BLACK), reversed : Boolean = false){
    @Suppress("NAME_SHADOWING")
    val color = if (reversed){Pair(color.second, color.first)} else {color}
    val coords = this.cursorPosition
    this.setCursorPosition(x, y)
    this.setForegroundColor(color.first)
    this.setBackgroundColor(color.second)
    input.toCharArray().forEach {
        this.putCharacter(it)
    }
    flush()
    this.cursorPosition = coords
}

fun printRescues() : Int {
    var linecount = 0
    var colors = Pair(TextColor.ANSI.WHITE, TextColor.ANSI.BLACK)
    try {
        for (res in rescues) {
            var charCount = 0
            if (res.cr) {
                colors = Pair(TextColor.ANSI.RED, colors.second)
            }
            if (!res.active) {
                terminal?.enableSGR(SGR.REVERSE)
            }
            terminal?.printString("${res.number} |", charCount + 2, linecount + 2, colors)
            charCount += res.number.toString().length + 2
            terminal?.printString(" ${res.client} |", charCount + 2, linecount + 2, colors)
            charCount += res.client.length + 3
            terminal?.printString(" ${res.language} |", charCount + 2, linecount + 2, colors)
            charCount += res.language.length + 3
            terminal?.printString(" ${res.platform} |", charCount + 2, linecount + 2, colors)
            charCount += res.platform.length + 3
            terminal?.printString(" ${res.clientSystem.name}", charCount + 2, linecount + 2, colors)
            charCount += res.clientSystem.name.length + 1
            if (!res.active) {
                terminal?.disableSGR(SGR.REVERSE)
            }
            linecount = printStatus(res, linecount)
            //printNotes

            colors = Pair(TextColor.ANSI.WHITE, TextColor.ANSI.BLACK)
        }
    } catch (e: Exception) {
    }
    return linecount
}

fun printStatus(res: Rescue, lCount : Int) : Int{
    var lineCount = lCount + 1
    for ((name, status) in res.rats) {
        var charCount = 0
        terminal?.printString(name + ": ", charCount + 3, lineCount + 2, blackwhite)
        charCount += name.length + 2

        if (status.friended == TRUE) terminal?.printString("FR+", charCount + 3, lineCount + 2, Pair(TextColor.ANSI.MAGENTA, TextColor.ANSI.WHITE), true)
        if (status.friended == NEUTRAL) {terminal?.printString("FR-", charCount + 3, lineCount + 2, Pair(TextColor.ANSI.BLACK, TextColor.ANSI.WHITE), true)}
        if (status.friended == FALSE) {terminal?.printString("FR-", charCount + 3, lineCount + 2, Pair(TextColor.ANSI.RED, TextColor.ANSI.WHITE), true)}
        charCount += 3 + 1

        if (status.winged == TRUE) terminal?.printString("WR+", charCount + 3, lineCount + 2, Pair(TextColor.ANSI.CYAN, TextColor.ANSI.WHITE), true)
        if (status.winged == NEUTRAL) terminal?.printString("WR-", charCount + 3, lineCount + 2, Pair(TextColor.ANSI.BLACK, TextColor.ANSI.WHITE), true)
        if (status.winged == FALSE) terminal?.printString("WR-", charCount + 3, lineCount + 2, Pair(TextColor.ANSI.RED, TextColor.ANSI.WHITE), true)
        charCount += 3 + 1

        if (status.beacon == TRUE) terminal?.printString("Beacon+", charCount + 3, lineCount + 2, Pair(TextColor.ANSI.BLUE, TextColor.ANSI.WHITE), true)
        if (status.beacon == NEUTRAL) terminal?.printString("Beacon-", charCount + 3, lineCount + 2, Pair(TextColor.ANSI.BLACK, TextColor.ANSI.WHITE), true)
        if (status.beacon == FALSE) terminal?.printString("Beacon-", charCount + 3, lineCount + 2, Pair(TextColor.ANSI.RED, TextColor.ANSI.WHITE), true)
        charCount += 7 + 1

        if (status.inSys == TRUE) terminal?.printString("Sys+", charCount + 3, lineCount + 2, Pair(TextColor.ANSI.YELLOW, TextColor.ANSI.WHITE), true)
        if (status.inSys == NEUTRAL) terminal?.printString("Sys-", charCount + 3, lineCount + 2, Pair(TextColor.ANSI.BLACK, TextColor.ANSI.WHITE), true)
        charCount += 4 + 1

        if (status.fueled == TRUE) terminal?.printString("Fuel+", charCount + 3, lineCount + 2, Pair(TextColor.ANSI.GREEN, TextColor.ANSI.WHITE), true)
        else terminal?.printString("", charCount + 3, lineCount + 2, Pair(TextColor.ANSI.BLACK, TextColor.ANSI.WHITE), true)
        charCount += 5 + 1

        if (status.disconnected == TRUE) terminal?.printString("DC", charCount + 3, lineCount + 2, Pair(TextColor.ANSI.RED, TextColor.ANSI.WHITE), true)
        else terminal?.printString("", charCount + 3, lineCount + 2, Pair(TextColor.ANSI.BLACK, TextColor.ANSI.WHITE), true)
        charCount += 2 + 1

        if (status.instancingP == TRUE) terminal?.printString("Inst-", charCount + 3, lineCount + 2, Pair(TextColor.ANSI.RED, TextColor.ANSI.WHITE), true)
        if (status.instancingP == NEUTRAL) terminal?.printString("", charCount + 3, lineCount + 2, Pair(TextColor.ANSI.BLACK, TextColor.ANSI.WHITE), true)
        if (status.instancingP == FALSE) terminal?.printString("", charCount + 3, lineCount + 2, Pair(TextColor.ANSI.BLACK, TextColor.ANSI.GREEN), true)
        charCount += 5 + 1

        if (status.interdicted == TRUE) terminal?.printString("INT", charCount + 3, lineCount + 2, Pair(TextColor.ANSI.RED, TextColor.ANSI.WHITE), true)
        else terminal?.printString("", charCount + 3, lineCount + 2, Pair(TextColor.ANSI.BLACK, TextColor.ANSI.WHITE), true)
        charCount += 3 + 1

        lineCount++
    }
    return lineCount
}
var color = Pair(TextColor.ANSI.WHITE, TextColor.ANSI.BLACK)
@Volatile
var blackwhite = Pair(TextColor.ANSI.WHITE, TextColor.ANSI.BLACK)
fun times(i : Int, function: (i : Int) -> Unit){
    var x : Int = 0
    while (x < i){
        function(x + 1)
        x++
    }


}
fun beep(){
    if ("${config.varMap["${entries.beep}"]}" == "true")
    terminal?.bell()
}


data class Rat( var name : String, var status : Status){
    fun setNameCorrectly() : Rat {
        name.replace(" ", "_").replaceAfter("[", "")
        return this
    }

}


data class System(var name : String)
data class Status(var status : String){
    var friended : Trilean = NEUTRAL
    var winged : Trilean = NEUTRAL
    var beacon : Trilean = NEUTRAL
    var inSys : Trilean = NEUTRAL
    var fueled : Trilean = NEUTRAL
    var disconnected : Trilean = NEUTRAL
    var instancingP: Trilean = NEUTRAL
    var interdicted: Trilean = NEUTRAL
}
data class Rescue(var client : String, var clientSystem : System, val language : String, var number : Int, var platform : String, var cr : Boolean, var active : Boolean = true){
    var rats : MutableList<Rat> = ArrayList()
    var notes : MutableList<String> = ArrayList()
   // var active : Boolean = true

}

enum class Trilean{
    TRUE, FALSE, NEUTRAL;
}
var rescues = ArrayList<Rescue>()



