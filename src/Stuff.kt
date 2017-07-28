import kotlin.system.exitProcess

fun handleWS(msg: String): Unit {
    println(msg)
}

val inpThr : Thread = Thread(fun (){
    while (true) {
        println("triggered")
        var line: String?
        line = readLine()
        //while (readLine().isNullOrBlank()){}
        if (line.isNullOrBlank()) continue
        line = line!!

        println("line is: " + line)
        if (line == "exit") {
            exitProcess(-1)
        }

       // if (WSinst.isClosed) {
            println("connecting")
            assert(WSinst.connectBlocking())
        //}
        if (WSinst.isClosed) {
            println("still closed")
           // exitProcess(-1)
        }
        println("waiting for connected")
        while (/*WSinst.isConnecting || */!WSinst.isClosed) {}
        println("Connected. trying to send")
        WSinst.send(line)
        println("sent: " + line)
    }
})