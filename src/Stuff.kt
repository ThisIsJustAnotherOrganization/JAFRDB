import kotlin.system.exitProcess

fun handleWS(msg: String): Unit {
    println(msg)
}

val inpThr : Thread = Thread(fun (){
    println("triggered")
    var line : String
    //while (readLine().isNullOrBlank()){}
    if (readLine().isNullOrBlank()) return
    line = readLine()!!
    if (line == "exit"){ exitProcess(0)}
    println("connecting")
    if (WSinst.isClosed) {println("connecting");WSinst.connect();}
    while (!WSinst.isOpen){}
    println("trying to send")
    WSinst.send(line)
    println("sent: " + line)
})