/**
 * Created by beepbeat/holladiewal on 04.11.2017.
 */

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.java_websocket.handshake.ServerHandshake
import java.lang.Exception
import java.net.URI


val parser = JsonParser()
class WebSocket{
    lateinit var client: WebSocketClient

    //lateinit var writer : BufferedWriter
    companion object {
        var inst : WebSocket? = null
        fun instance(): WebSocket {
            if (inst  == null){
                inst = WebSocket()
            }
            return inst!!
        }
    }


    suspend fun init(){
        client = WebSocketClient(URI("wss://dev.api.fuelrats.com?bearer=d9dd8c0d59feadf41abad9dd643c90b77d091d7cbc596f92ef11421127d99dc6"))
        println("Connecting blocking")
        client.connectBlocking()
        println("Connected")

        // delay(2000)
        //while (!client.isOpen){}
        // client.send("{\"action\":[\"rescues\",\"read\"],\"meta\":{\"action\":\"rescues:read\"},\"status\":{\"\$not\":\"closed\"}}")
        // writer = client.socket.getOutputStream().bufferedWriter()
    }

    fun send(msg: String){
        client.send(msg)
    }


}

class WebSocketClient(uri: URI) : org.java_websocket.client.WebSocketClient(uri){
    override fun onOpen(handshakedata: ServerHandshake?) {
        println("Opening Connection")
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        println("Closing Connection, reason: $reason , $code")
    }

    override fun onMessage(message: String?) {
        if (message == null){
            println("message was null")
            return
        }
        @Suppress("NAME_SHADOWING")
        var message : String = message
        println("message: " + message)
    }

    override fun onError(ex: Exception?) {
        ex!!.printStackTrace()
    }

}

fun handleWSMessage(msg: String){
    val origElement = parser.parse(msg)
    var meta = origElement.asJsonObject.get("meta").asJsonObject
    var data = origElement.asJsonObject.get("data").asJsonArray

    when (meta.get("action").asString){
        "rescues:read" -> { parseRescueRead(meta, data) }

    }

}

fun parseRescueRead(meta: JsonObject, data : JsonArray){
    data.forEach {
        if (it.asJsonObject.get("type").asString != "rescues") return@forEach
        val attributes : JsonObject = it.asJsonObject.get("attributes").asJsonObject
        val name = attributes.get("IRCNick").asString
        val cr = attributes.get("codeRed").asBoolean
        val system = System(attributes.get("system").asString)
        val lang = attributes.get("data").asJsonObject.get("langID").asString
        val number = attributes.get("data").asJsonObject.get("boardIndex").asInt
        val platform = attributes.get("platform").asString
        val active : Boolean =
            when (attributes.get("status").asString){
                "inactive" -> false
                "open" -> true
                else -> return@forEach
            }

        rescues.add(Rescue(name, system, lang, number, platform, cr, active))
    }
}