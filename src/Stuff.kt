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
        listenfr.handleMessage(nick, line)
    }
})

fun String.strip() : String{
    val sb = StringBuilder()
    var f3 = false
    for (c in this.toCharArray()){
        if (c == 2.toChar()){continue} //STX
        if (c == 3.toChar()){f3 = true; continue} //ETX
        if (f3){
            if (c.toInt() in 48..57 || c == ','){
                continue
            }
            f3 = false
        }
        sb.append(c)
    }
    return sb.toString()
}

fun String.reduce() : String{
    val charr = this.toCharArray().toMutableList()
    val sb = StringBuilder()
    for ((i, c) in charr.withIndex()){
       if (charr.lastIndex < i && c == ' ' && charr[i+1] == ' '){
           charr.removeAt(i+1)
       }
    }
    charr.forEach { sb.append(it) }
    return sb.toString()
}