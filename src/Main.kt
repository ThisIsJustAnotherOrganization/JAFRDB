import Trilean.*
import jcurses.system.CharColor
import jcurses.system.Toolkit
import kotlinx.coroutines.experimental.launch
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.fixedRateTimer


fun main(args: Array<String>) {
    initConfig()
    readConfig()
    saveConfig()
    tailerfr.delay
    tailerrc.delay
    Toolkit.init()
    Toolkit.clearScreen(color)
    color.foreground = CharColor.CYAN

    //always last call
    launch{inpThr.run()}
    launch{
        WebSocket.instance().init()
        WebSocket.instance().request("rescues", "read")
    }

    while(true);

}
val screenupdate = fixedRateTimer("ScreenUpdater", false, 500, 1000, ::updateScreen)
val tailerdog = fixedRateTimer("TailerWatchdog", true, 500, 1000, ::checkTailer)


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
    Toolkit.clearScreen(blackwhite)
    var linecount = 0
    //toPrint.add(Random().nextDouble().toString())
    Toolkit.printString("Welcome to JAFRDB", Toolkit.getScreenWidth() / 2 - 17 /* welcome string length*/, 0, color)
    Toolkit.printString("Cases: ", 2, 1, blackwhite)
    linecount = printRescues()

//    for (str in toPrint){
  //      Toolkit.printString(str, 0, linecount + 2, blackwhite)
    //    linecount++
    //}

}

fun printRescues() : Int{
    var linecount = 0
    var colors = CharColor(CharColor.BLACK, CharColor.WHITE)
    try {
        for (res in rescues){
                var charCount = 0
                if (res.cr) {colors.foreground = CharColor.RED}
                if (!res.active) {colors.colorAttribute = CharColor.REVERSE}

                Toolkit.printString(res.number.toString() + " |", charCount + 2, linecount + 2, colors)
                charCount += res.number.toString().length + 2
                Toolkit.printString(" " + res.client + " |", charCount + 2, linecount + 2, colors)
                charCount += res.client.length + 3
                Toolkit.printString(" " + res.language + " |", charCount + 2, linecount + 2, colors)
                charCount += res.language.length + 3
                Toolkit.printString(" " + res.platform + " |", charCount + 2, linecount + 2, colors)
                charCount += res.platform.length + 3
                Toolkit.printString(" " + res.clientSystem.name, charCount + 2, linecount + 2, colors)
                charCount += res.clientSystem.name.length + 1
                linecount = printStatus(res, linecount)
                //printNotes

                colors = CharColor(CharColor.BLACK, CharColor.WHITE)
        }
    } catch(e: Exception) {
    }
    return linecount
}

fun printStatus(res: Rescue, lCount : Int) : Int{
    var lineCount = lCount + 1
    for ((name, status) in res.rats) {
        var charCount = 0
        Toolkit.printString(name + ": ", charCount + 3, lineCount + 2, blackwhite)
        charCount += name.length + 2

        if (status.friended == TRUE) Toolkit.printString("FR+", charCount + 3, lineCount + 2, CharColor(CharColor.MAGENTA, CharColor.WHITE))
        if (status.friended == NEUTRAL) {Toolkit.printString("FR-", charCount + 3, lineCount + 2, CharColor(CharColor.BLACK, CharColor.WHITE))}
        if (status.friended == FALSE) {Toolkit.printString("FR-", charCount + 3, lineCount + 2, CharColor(CharColor.RED, CharColor.WHITE))}
        charCount += 3 + 1

        if (status.winged == TRUE) Toolkit.printString("WR+", charCount + 3, lineCount + 2, CharColor(CharColor.CYAN, CharColor.WHITE))
        if (status.winged == NEUTRAL) Toolkit.printString("WR-", charCount + 3, lineCount + 2, CharColor(CharColor.BLACK, CharColor.WHITE))
        if (status.winged == FALSE) Toolkit.printString("WR-", charCount + 3, lineCount + 2, CharColor(CharColor.RED, CharColor.WHITE))
        charCount += 3 + 1

        if (status.beacon == TRUE) Toolkit.printString("Beacon+", charCount + 3, lineCount + 2, CharColor(CharColor.BLUE, CharColor.WHITE))
        if (status.beacon == NEUTRAL) Toolkit.printString("Beacon-", charCount + 3, lineCount + 2, CharColor(CharColor.BLACK, CharColor.WHITE))
        if (status.beacon == FALSE) Toolkit.printString("Beacon-", charCount + 3, lineCount + 2, CharColor(CharColor.RED, CharColor.WHITE))
        charCount += 7 + 1

        if (status.inSys == TRUE) Toolkit.printString("Sys+", charCount + 3, lineCount + 2, CharColor(CharColor.YELLOW, CharColor.WHITE))
        if (status.inSys == NEUTRAL) Toolkit.printString("Sys-", charCount + 3, lineCount + 2, CharColor(CharColor.BLACK, CharColor.WHITE))
        charCount += 4 + 1

        if (status.fueled == TRUE) Toolkit.printString("Fuel+", charCount + 3, lineCount + 2, CharColor(CharColor.GREEN, CharColor.WHITE))
        else Toolkit.printString("", charCount + 3, lineCount + 2, CharColor(CharColor.BLACK, CharColor.WHITE))
        charCount += 5 + 1

        if (status.disconnected == TRUE) Toolkit.printString("DC", charCount + 3, lineCount + 2, CharColor(CharColor.RED, CharColor.WHITE))
        else Toolkit.printString("", charCount + 3, lineCount + 2, CharColor(CharColor.BLACK, CharColor.WHITE))
        charCount += 2 + 1

        if (status.instancingP == TRUE) Toolkit.printString("Inst-", charCount + 3, lineCount + 2, CharColor(CharColor.RED, CharColor.WHITE))
        if (status.instancingP == NEUTRAL) Toolkit.printString("", charCount + 3, lineCount + 2, CharColor(CharColor.BLACK, CharColor.WHITE))
        if (status.instancingP == FALSE) Toolkit.printString("", charCount + 3, lineCount + 2, CharColor(CharColor.BLACK, CharColor.GREEN))
        charCount += 5 + 1

        if (status.interdicted == TRUE) Toolkit.printString("INT", charCount + 3, lineCount + 2, CharColor(CharColor.RED, CharColor.WHITE))
        else Toolkit.printString("", charCount + 3, lineCount + 2, CharColor(CharColor.BLACK, CharColor.WHITE))
        charCount += 3 + 1

        lineCount++
    }
    return lineCount
}
val color = CharColor(CharColor.BLACK, CharColor.WHITE)
var blackwhite = CharColor(CharColor.BLACK, CharColor.WHITE)
fun times(i : Int, function: (i : Int) -> Unit){
    var x : Int = 0
    while (x < i){
        function(x + 1)
        x++
    }


}
fun beep(){ print(7.toChar())}
enum class Rank{none, recruit, rat, overseer, techrat, op, netadmin, admin}


data class Rat(var name : String, var status : Status)
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



