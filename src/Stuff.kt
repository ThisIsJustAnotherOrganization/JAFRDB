import java.util.regex.Pattern
import kotlin.system.exitProcess

internal var dirty = false

val inpThr : Thread = Thread(fun (){
    while (true) {
        //println("triggered")
        var line: String? = readLine()
        if (line.isNullOrBlank()) continue
        line = line!!

        if (line == "exit") {
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
    val tmp1 = this.trim().split(" ").toMutableList()
    tmp1.removeAll { it.trim() == "" }
    val sb = StringBuilder()
    tmp1.forEach{ sb.append(it + " ") }
    return sb.toString().trim()
}

fun ArrayList<Rescue>.asStringArray(): ArrayList<String> {
    var retV = ArrayList<String>()
    this.mapTo(retV) { "${it.number}|${it.client}|${it.language}|${it.clientSystem.name}|${it.platform}|${it.active}" }
    return retV;
}


