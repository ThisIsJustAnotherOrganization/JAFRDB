import jcurses.system.Toolkit
import java.util.regex.Pattern
import kotlin.system.exitProcess

val inpThr : Thread = Thread(fun (){
    while (true) {
        //println("triggered")
        var line: String? = readLine()
        if (line.isNullOrBlank()) continue
        line = line!!

        if (line == "exit") {
            Toolkit.shutdown()
            exitProcess(0)
        }
        var nick : String = ""
        if (line.contains("nick: ")){
            nick = line.replace("nick: ", "").split(Pattern.compile(" "), 2).first()
            line.replace("nick: " + nick, "").trim()
        }
        listen.handleMessage(nick, line)
    }
})

//Thanks to RosettaCode.org for this code fragment
fun String.stripOld(extendedChars : Boolean = false) : String{
    val sb = StringBuilder()
    for (c in this) {
        val i = c.toInt()
        if (i in 32..126 || (!extendedChars && i >= 128)) sb.append(c)
    }
    return sb.toString()
}

fun String.strip() : String{
    val sb = StringBuilder()
    var f3 = false
    for (c in this.toCharArray()){
        if (c == 2.toChar()){continue} //STX
        if (c == 3.toChar()){f3 = true; continue} //ETX
        if (f3){
            if (c.toInt() in 48..57){
                continue
            }
            f3 = false
        }
        sb.append(c)
    }
    return sb.toString()
}