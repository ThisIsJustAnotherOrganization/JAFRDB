/**
 * Created by beepbeat/holladiewal on 04.11.2017.
 */

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import kotlinx.coroutines.experimental.launch
import org.java_websocket.handshake.ServerHandshake
import java.lang.Exception
import java.net.URI
import java.util.*


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
        client = WebSocketClient(URI("wss://dev.api.fuelrats.com"))
        //println("Connecting blocking")
        client.connectBlocking()
        //println("Connected")

        // delay(2000)
        //while (!client.isOpen){}
        // client.send("{\"action\":[\"rescues\",\"read\"],\"meta\":{\"action\":\"rescues:read\"},\"status\":{\"\$not\":\"closed\"}}")
        // writer = client.socket.getOutputStream().bufferedWriter()
    }

    fun send(msg: String){
        client.send(msg)
    }

    fun request(controller : String, action : String, params : Map<String, String> = emptyMap(), metaPar : Map<String, String> = emptyMap()){
        val root = JsonObject()
        var actionArr = JsonArray()
        actionArr.add(controller)
        actionArr.add(action)
        root.add("action", actionArr)
        params.forEach { t, u -> root.add(t, JsonPrimitive(u)) }
        val meta = JsonObject()
        meta.add("action", JsonPrimitive("$controller:$action"))
        val myData = JsonObject()
        metaPar.forEach { t, u -> myData.add(t, JsonPrimitive(u)) }
        meta.add("mydata", myData)
        root.add("meta", meta)
        client.send(root.toString())
    }


}

class WebSocketClient(uri: URI) : org.java_websocket.client.WebSocketClient(uri){
    override fun onOpen(handshakedata: ServerHandshake?) {
        //println("Opening Connection")
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        //println("Closing Connection, reason: $reason , $code")
    }

    override fun onMessage(message: String?) {
        if (message == null){
            //println("message was null")
            return
        }
        @Suppress("NAME_SHADOWING")
        var message : String = message
        //println("message: " + message)
        handleWSMessage(message)
    }

    override fun onError(ex: Exception?) {
        ex!!.printStackTrace()
    }

}

fun handleWSMessage(msg: String){
    val origElement = parser.parse(msg)
    var meta = origElement.
            asJsonObject.
            get("meta").
            asJsonObject

    if (meta.asJsonObject.has("event")) return

    var data = origElement.asJsonObject.get("data").asJsonArray


    when (meta.get("action").asString){
        "rescues:read" -> parseRescueRead(meta, data)
        "rats:read" -> parseRatNameResolution(meta, data)
    }

}

fun parseRescueRead(meta: JsonObject, data : JsonArray){
    data.forEach {
        launch {
            if (it.asJsonObject.get("type").asString != "rescues") {
                /* println("not a rescue: " + it.asJsonObject.get("id").asString.subSequence(0, 7)); */return@launch
            }
            val attributes: JsonObject = it.asJsonObject.get("attributes").asJsonObject
            val name = attributes.get("data").asJsonObject.get("IRCNick").asString
            val cr = attributes.get("codeRed").asBoolean
            val system = if (!attributes.get("system").isJsonNull) System(attributes.get("system").asString) else System("null")
            val lang = attributes.get("data").asJsonObject.get("langID").asString
            val number = attributes.get("data").asJsonObject.get("boardIndex").asInt
            val platform = attributes.get("platform").asString
            val active: Boolean =
                    when (attributes.get("status").asString) {
                        "inactive" -> false
                        "open" -> true
                        else -> {
                            /*println("Rescue state neither inactive or open: " + it.asJsonObject.get("id").asString.subSequence(0, 7)); */return@launch
                        }
                    }
            val resc = Rescue(name, system, lang, number, platform, cr, active)
            it.asJsonObject
                    .get("relationships").asJsonObject
                    .get("rats").asJsonObject
                    .get("data").asJsonArray
                    .forEach { resc.rats.add(Rat(resolveRatName(""), Status("")).setNameCorrectly(it.asJsonObject.get("id").asString)) }
            attributes.get("unidentifiedRats").asJsonArray.forEach { resc.rats.add(Rat("", Status("")).setNameCorrectly(it.asString)) }
            //println("adding rescue. name: $name, cr: $cr, system: $system, lang: $lang, number: $number, platform: $platform")
            rescues.add(resc)

        }
    }
}

fun parseRatNameResolution(meta: JsonObject, data : JsonArray){
    val uuid = meta.get("mydata").asJsonObject.get("uuid").asString
    if (resolvMap.get(uuid) != "") return
    resolvMap.put(uuid, data[0].asJsonObject.get("attributes").asJsonObject.get("name").asString)
}


val resolvMap = mutableMapOf<String, String>()

fun resolveRatName(id: String): String {
    val args = mutableMapOf<String, String>()
    val metaArgs = mutableMapOf<String, String>()
    var ranId = UUID.randomUUID().toString()
    while (resolvMap.contains(ranId)) {
        ranId = UUID.randomUUID().toString()
    }
    metaArgs.put("uuid", ranId)
    resolvMap.put(ranId, "")
    args.put("id", id)
    WebSocket.instance().request("rats", "read", args, metaArgs)
    while (resolvMap[ranId].isNullOrBlank()){}
    val name = resolvMap[ranId]!!
    resolvMap.remove(ranId)
    //println("resolved $id to $name")
    return name
}