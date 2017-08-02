import org.apache.commons.io.IOUtils
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.net.URL
import java.net.URLConnection
import java.util.*


fun main(args: Array<String>) {
    //times(5, ::print)
    //WSinst.connect()
    initConfig()
    readConfig()
    saveConfig()

    //always last call
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
var WSinst : WebSocket = WebSocket(URI("wss://api.fuelrats.com:443"), emptyMap())
class WebSocket(serverUri: URI?, httpHeaders : Map<String, String>) : WebSocketClient(serverUri) {
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
        return IOUtils.readLines(urlconnection.getInputStream(), urlconnection.contentEncoding) as MutableList<String>
    }

    fun request(urlToPage : String) : MutableList<String>{
        val urlconnection = URL(urlToPage).openConnection()
        urlconnection.setRequestProperty("Authorization", "Bearer " + config.authkey)
        urlconnection.connect()
        return getResponse(urlconnection)

    }

}


