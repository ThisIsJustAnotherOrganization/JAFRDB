import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.util.*

fun print(inp : Any){
    println(inp)
}

fun main(args: Array<String>) {
    //times(5, ::print)
    inpThr.run()

}

fun times(i : Int, function: (i : Int) -> Unit){
    var x : Int = 0
    while (x < i){
        function(x + 1)
        x++
    }


}
enum class Rank{none, recruit, rat, overseer, techrat, op, netadmin, admin}

data class User(var name : String, val rank: Rank)
data class System(var name : String)
data class Status(var status : String){
    var friended : Boolean = false
    var winged : Boolean = false
    var beacon : Boolean = false
    var fueled : Boolean = false
    var disconnected : Boolean = false
    var instanced : Boolean = false
    var closed : Boolean = false
}
data class Rescue(val client : User, var clientSystem : System, val language : String){
    var status : Status = Status("")
    var rats : MutableList<User> = ArrayList()

}

class WebSocket(serverUri: URI?) : WebSocketClient(serverUri) {
    var errorCount: Int = 0
    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        errorCount++
        println(reason)
        if (errorCount <= 5) connect()
    }

    override fun onMessage(message: String?) {
        if (message == null) return
        handleWS(message)
    }

    override fun onError(ex: Exception?) {
        println(ex.toString())
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

var WSinst : WebSocket = WebSocket(URI("wss://api.fuelrats.com:443"))


