import Trilean.*
import com.googlecode.lanterna.SGR
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import com.googlecode.lanterna.terminal.Terminal
import com.googlecode.lanterna.terminal.swing.AWTTerminalFontConfiguration
import com.googlecode.lanterna.terminal.swing.AWTTerminalFrame
import kotlinx.coroutines.experimental.launch
import java.awt.Font
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import java.io.PrintStream
import java.lang.System
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.fixedRateTimer
import kotlin.properties.Delegates
import kotlin.system.exitProcess


class KeyProcessor : KeyListener{
    override fun keyTyped(e: KeyEvent?) {
        when(e!!.keyChar){
            'x', 'X' -> {terminal?.exitPrivateMode();System.exit(0)}
        }
    }

    override fun keyPressed(e: KeyEvent?) = Unit

    override fun keyReleased(e: KeyEvent?) = Unit

}

class windowListen : WindowAdapter(){

    override fun windowClosing(e: WindowEvent?) {
        super.windowClosing(e)
    }

    override fun windowClosed(e: WindowEvent?) {
        super.windowClosed(e)
        terminal!!.exitPrivateMode()
        exitProcess(0)
    }
}


fun main(args: Array<String>) {
    System.setOut(PrintStream(BufferedOutputStream(FileOutputStream("output.txt")), true))

    initConfig()
    readConfig()

    if (config.varMap["${entries.token}"] == ""){
        AuthHandler().authorize()
        println(config.varMap["${entries.token}"])
    }
    saveConfig()
    tailerfr.delay
    tailerrc.delay
    color = Pair(TextColor.ANSI.CYAN, color.second)
    terminal = DefaultTerminalFactory()
            .setTerminalEmulatorTitle("JAFRDB: Just another FuelRats dispatch board")
            .setForceAWTOverSwing(true)
            .setTerminalEmulatorFontConfiguration(AWTTerminalFontConfiguration.newInstance(Font(Font.MONOSPACED, Font.PLAIN, config.varMap["${entries.fontSize}"]!!.toInt())))
            .createTerminal()

    initTimers()

    if (terminal is AWTTerminalFrame){
        (terminal as AWTTerminalFrame).addWindowListener(windowListen())

    }

    launch { while(true) {
        val keyStroke = terminal?.pollInput() ?: continue
        if (keyStroke.keyType != KeyType.Character) continue
        when (keyStroke.character){
            'x', 'X' -> {
                terminal?.exitPrivateMode()
                System.exit(0)
            }
            'r', 'R' -> updateScreen()

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
    //screenupdate = fixedRateTimer("ScreenUpdater", true, 500, (1000.0 * (config.varMap["${entries.fontSize}"]!!.toLong() / 100.0 + 1.0).toLong()).toLong(), ::updateScreen)
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
var lastHash = -1

fun updateScreen(timerTask: TimerTask) {
    updateScreen()
}
fun updateScreen() {
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
    //terminal!!.exitPrivateMode()
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


class Rat(name : String, status : Status = Status(""), val uuid: String){
    var name : String by Delegates.observable(name, ::rescueChanged)
    var status : Status by Delegates.observable(status, ::rescueChanged)

    fun setNameCorrectly() : Rat {
        name.replace(" ", "_").replaceAfter("[", "")
        return this
    }

    operator fun component1(): String {
        return name
    }

    operator fun component2() : Status {
        return status
    }

}

fun noop() = Unit
var rescues : WatchableMutableList<Rescue> = WatchableMutableList(::noop)




