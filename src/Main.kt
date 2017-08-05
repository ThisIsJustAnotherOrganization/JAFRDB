import jcurses.system.CharColor
import jcurses.system.Toolkit
import org.apache.commons.io.IOUtils
import org.apache.commons.io.input.Tailer
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.net.URL
import java.net.URLConnection
import java.util.*
import javax.tools.Tool
import kotlin.collections.ArrayList
import kotlin.concurrent.fixedRateTimer


fun main(args: Array<String>) {
    //times(5, ::print)
    //WSinst.connect()
    initConfig()
    readConfig()
    saveConfig()
    tailer.delay
    Toolkit.init()
    Toolkit.clearScreen(color)
    color.foreground = CharColor.CYAN
    /*rescues.add(Rescue("LASTCHAOSMARINE", System("WEPAI OZ-O E6-7558"), "en-GB", 5, "PS4", true))
    rescues[0].rats.add(Rat("Test rat 1", Status("")))
    rescues[0].rats.add(Rat("Test rat 2", Status("")))

    rescues[0].rats[1].status.friended = true
    rescues[0].rats[1].status.winged = true
    rescues[0].rats[1].status.beacon = true
    rescues[0].rats[1].status.inSys = true
    rescues[0].rats[1].status.interdicted = true
    rescues[0].rats[1].status.fueled = true
    rescues[0].rats[1].status.interdicted = true
    rescues[0].rats[1].status.instancingP = true
    rescues[0].rats[1].status.disconnected = true
    toPrint.add("TESTING!")*/
    //printRescues()

    //always last call
    //inpThr.run()
    //while(true);


}
val screenupdate = fixedRateTimer("ScreenUpdater", false, 500, 1000, ::updateScreen)
val tailerdog = fixedRateTimer("TailerWatchdog", true, 500, 1000, ::checkTailer)


fun checkTailer(task : TimerTask) {
    if (tailerStopped){
        tailerStopped = false
        tailer.run()
    }
}

@Volatile
var toPrint = ArrayList<String>()

fun updateScreen(timerTask: TimerTask) {
    var linecount = 0
    //toPrint.add(Random().nextDouble().toString())
    Toolkit.printString("Welcome to JAFRDB", Toolkit.getScreenWidth() / 2 - 17 /* welcome string length*/, 0, color)
    Toolkit.printString("Cases: ", 2, 1, blackwhite)
    linecount = printRescues()

    for (str in toPrint){
        Toolkit.printString(str, 0, linecount + 2, blackwhite)
        linecount++
    }

}

fun printRescues() : Int{
    var linecount = 0
    var colors = CharColor(CharColor.BLACK, CharColor.WHITE)
        for (res in rescues){
        var charCount = 0
        if (res.cr) {colors.foreground = CharColor.RED}

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
    return linecount
}

fun printStatus(res: Rescue, lCount : Int) : Int{
    var lineCount = lCount + 1
    for ((name, status) in res.rats) {
        var charCount = 0
        Toolkit.printString(name + ": ", charCount + 3, lineCount + 2, blackwhite)
        charCount += name.length + 2

        if (status.friended) Toolkit.printString("FR+", charCount + 3, lineCount + 2, CharColor(CharColor.MAGENTA, CharColor.WHITE))
        else Toolkit.printString("FR-", charCount + 3, lineCount + 2, CharColor(CharColor.BLACK, CharColor.WHITE))
        charCount += 3 + 1

        if (status.winged) Toolkit.printString("WR+", charCount + 3, lineCount + 2, CharColor(CharColor.CYAN, CharColor.WHITE))
        else Toolkit.printString("WR-", charCount + 3, lineCount + 2, CharColor(CharColor.BLACK, CharColor.WHITE))
        charCount += 3 + 1

        if (status.beacon) Toolkit.printString("Beacon+", charCount + 3, lineCount + 2, CharColor(CharColor.BLUE, CharColor.WHITE))
        else Toolkit.printString("Beacon-", charCount + 3, lineCount + 2, CharColor(CharColor.BLACK, CharColor.WHITE))
        charCount += 7 + 1

        if (status.inSys) Toolkit.printString("Sys+", charCount + 3, lineCount + 2, CharColor(CharColor.YELLOW, CharColor.WHITE))
        else Toolkit.printString("Sys-", charCount + 3, lineCount + 2, CharColor(CharColor.BLACK, CharColor.WHITE))
        charCount += 4 + 1

        if (status.fueled) Toolkit.printString("Fuel+", charCount + 3, lineCount + 2, CharColor(CharColor.GREEN, CharColor.WHITE))
        else Toolkit.printString("", charCount + 3, lineCount + 2, CharColor(CharColor.BLACK, CharColor.WHITE))
        charCount += 5 + 1

        if (status.disconnected) Toolkit.printString("DC", charCount + 3, lineCount + 2, CharColor(CharColor.RED, CharColor.WHITE))
        else Toolkit.printString("", charCount + 3, lineCount + 2, CharColor(CharColor.BLACK, CharColor.WHITE))
        charCount += 2 + 1

        if (status.instancingP) Toolkit.printString("Inst-", charCount + 3, lineCount + 2, CharColor(CharColor.RED, CharColor.WHITE))
        else Toolkit.printString("", charCount + 3, lineCount + 2, CharColor(CharColor.BLACK, CharColor.WHITE))
        charCount += 5 + 1

        if (status.interdicted) Toolkit.printString("INT", charCount + 3, lineCount + 2, CharColor(CharColor.RED, CharColor.WHITE))
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
enum class Rank{none, recruit, rat, overseer, techrat, op, netadmin, admin}


data class Rat(var name : String, var status : Status)
data class System(var name : String)
data class Status(var status : String){
    var friended : Boolean = false
    var winged : Boolean = false
    var beacon : Boolean = false
    var inSys : Boolean = false
    var fueled : Boolean = false
    var disconnected : Boolean = false
    var instancingP: Boolean = false
    //var closed : Boolean = false
    var interdicted: Boolean = false
}
data class Rescue(var client : String, var clientSystem : System, val language : String, val number : Int, var platform : String, var cr : Boolean){
    var rats : MutableList<Rat> = ArrayList()
    var notes : MutableList<String> = ArrayList()
    var active : Boolean = true

}
var rescues = ArrayList<Rescue>()
var WSinst : WebSocket = WebSocket(URI("wss://api.fuelrats.com:443"))
class WebSocket(serverUri: URI?) : WebSocketClient(serverUri) {
    var errorCount: Int = 0
    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        errorCount++
        println(reason)
        //if (errorCount <= 5) connect()
    }

    override fun onMessage(message: String?) {
        if (message == null) return
        handleWS(message)
    }

    override fun onError(ex: Exception) {
        //println(ex.toString())
        ex.printStackTrace()
        if (errorCount > 5) {
            close()
            throw Exception("More than 5 errors occured", ex)
        }
    }

    override fun onOpen(handshakedata: ServerHandshake?) {
        println("opening")
        //throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

var WAPIinst = WebAPI()
class WebAPI{ //rescue offset is 13764

    private fun getResponse(urlconnection : URLConnection) : MutableList<String>{
        return IOUtils.readLines(urlconnection.getInputStream(), urlconnection.contentEncoding)
    }

    fun request(urlToPage : String) : MutableList<String>{
        val urlconnection = URL(urlToPage).openConnection()
        urlconnection.setRequestProperty("Authorization", "Bearer " + config.authkey)
        urlconnection.connect()
        return getResponse(urlconnection)

    }

}


