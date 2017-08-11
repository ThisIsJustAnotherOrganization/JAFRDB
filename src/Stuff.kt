import jdk.nashorn.internal.runtime.ScriptingFunctions.readLine
import java.util.regex.Pattern
import kotlin.system.exitProcess

val inpThr : Thread = Thread(fun (){
    while (true) {
        //println("triggered")
        var line: String? = readLine()
        if (line.isNullOrBlank()) continue
        line = line!!

        if (line == "exit") {
            exitProcess(-1)
        }
        var nick : String
        if (line.contains("nick: ")){
            nick = line.replace("nick: ", "").split(Pattern.compile(" "), 2).first()
            line.replace("nick: " + nick, "").trim()
        }
        else{listen.handleMessage("", line)}
    }
})