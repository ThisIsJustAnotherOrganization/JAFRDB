/**
 * Created by beepbeat/holladiewal on 04.11.2017.
 */

import com.google.gson.*
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import org.java_websocket.drafts.Draft_6455
import org.java_websocket.handshake.ServerHandshake
import java.lang.Exception
import java.net.URI
import java.util.*
import javax.net.ssl.SSLContext


val parser = JsonParser()
class WebSocket{
    lateinit var client: WebSocketClient

    companion object {
        var inst : WebSocket? = null
        fun instance(): WebSocket {
            if (inst  == null){
                inst = WebSocket()
            }
            return inst!!
        }
    }


    fun init(){
        //WebSocketImpl.DEBUG = true
        client = WebSocketClient(URI("wss://api.fuelrats.com/"), "${config.varMap["${entries.token}"]}")
        println("TESTING REDIRECT, DEBUG MODE ACTIVATED")

        val sslcontext = SSLContext.getInstance("TLS")
        sslcontext.init(null, null, null)
        client.socket = sslcontext.socketFactory.createSocket()

        var i = 1
        var result = client.connectBlocking()
        while (!result){
            toPrint.add("WebSocket Connection failed! Tries: $i")
            i++
            result = client.connectBlocking()
        }

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
        if ("$controller:$action" == "rescues:read"){
            val status = JsonObject()
            status.add("\$not", JsonPrimitive("closed"))
            root.add("status", status)
        }
        client.send(root.toString())

    }


}

class WebSocketClient(uri: URI, token: String) : org.java_websocket.client.WebSocketClient(uri, Draft_6455(), mapOf(Pair("Authentication", "Bearer $token")), 0){
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
        println("message: " + message)
        handleWSMessage(message)

    }

    override fun onError(ex: Exception?) {
        ex!!.printStackTrace(listenfr.stackFile)
    }

}

fun handleWSMessage(msg: String){
    val origElement = parser.parse(msg)
    println(msg)
    try {
        val meta = origElement.
                asJsonObject.
                get("meta").
                asJsonObject
        val data = origElement.asJsonObject.get("data").asJsonArray
        val included = if (origElement.asJsonObject.has("included") && !origElement.asJsonObject.get("included").isJsonNull) origElement.asJsonObject.get("included").asJsonArray else JsonArray()

        if (meta.asJsonObject.has("event")){
            when(meta.asJsonObject.get("event").asString){
                "rescueUpdated" -> {parseRescueUpdate(meta, data, included); return}
                "rescueCreated" -> {parseRescueRead(meta, data, included); return}
                else -> return
            }
        }



        when (meta.get("action").asString){
            "rescues:read" -> {parseRescueRead(meta, data, included);updateScreen()}
            "rats:read" -> parseRatNameResolution(meta, data)
        }
    } catch (e: Exception) {
        e.printStackTrace(listenfr.stackFile)
    }

}

fun parseRescueRead(meta: JsonObject, data : JsonArray, included: JsonArray){
    data.forEach {
        launch {
            if (it.asJsonObject.get("type").asString != "rescues") {
                /* println("not a rescue: " + it.asJsonObject.get("id").asString.subSequence(0, 7)); */return@launch
            }
            val attributes: JsonObject = it.asJsonObject.get("attributes").asJsonObject
            val name = attributes.get("data").asJsonObject.get("IRCNick").asString
            val cr = attributes.get("codeRed").asBoolean
            val system = if (!attributes.get("system").isJsonNull) System(attributes.get("system").asString) else System("Unknown")
            val lang = attributes.get("data").asJsonObject.get("langID").asString
            val number = attributes.get("data").asJsonObject.get("boardIndex").asInt
            val platform = if (!attributes.get("platform").isJsonNull) attributes.get("platform").asString else "Unknown"
            val uuid = it.asJsonObject.get("id").asString
            val active: Boolean =
                    when (attributes.get("status").asString) {
                        "inactive" -> false
                        "open" -> true
                        else -> {
                            /*println("Rescue state neither inactive or open: " + it.asJsonObject.get("id").asString.subSequence(0, 7)); */return@launch
                        }
                    }
            val resc = Rescue(name, system, lang, number, platform, cr, uuid, active)
            it.asJsonObject
                    .get("relationships").asJsonObject
                    .get("rats").asJsonObject
                    .get("data").asJsonArray
                    .forEach { resc.rats.add(Rat(resolveRatName(it.asJsonObject.get("id").asString, included), Status(""), it.asJsonObject.get("id").asString).setNameCorrectly()) }
            attributes.get("unidentifiedRats").asJsonArray.forEach { resc.rats.add(Rat(it.asString, Status(""), it.asString).setNameCorrectly()) }
            //println("adding rescue. name: $name, cr: $cr, system: $system, lang: $lang, number: $number, platform: $platform")
            rescues.add(resc)

        }
    }
    launch{ delay(255); updateScreen()}
}

fun parseRescueUpdate(meta: JsonObject, data: JsonArray, included: JsonArray){
    return //keep code, but deactivated
    data.forEach {
        launch {
            @Suppress("NAME_SHADOWING", "UnnecessaryVariable")
            val _data = it
            val attributes: JsonObject = it.asJsonObject.get("attributes").asJsonObject
            val name = attributes.get("data").asJsonObject.get("IRCNick").asString
            val cr = attributes.get("codeRed").asBoolean
            val system = if (!attributes.get("system").isJsonNull) System(attributes.get("system").asString) else System("null")
            val number = attributes.get("data").asJsonObject.get("boardIndex").asInt
            val platform = if (!attributes.get("platform").isJsonNull) attributes.get("platform").asString else "Unknown"
            val active: Boolean =
                    when (attributes.get("status").asString) {
                        "inactive" -> false
                        "open" -> true
                        else -> {
                            /*println("Rescue state neither inactive or open: " + it.asJsonObject.get("id").asString.subSequence(0, 7)); */
                            rescues.removeIf{ it.UUID == _data.asJsonObject.get("id").asString }
                            return@launch
                        }
                    }

            if (rescues.none{ it.UUID == _data.asJsonObject.get("id").asString }){parseRescueRead(meta, data, included); updateScreen(); return@launch}
            with(rescues.filter { it.UUID == _data.asJsonObject.get("id").asString }[0]) {
                this.client = name
                this.cr = cr
                this.clientSystem = system
                this.number = number
                this.platform = platform
                this.active = active

                @Suppress("LocalVariableName")
                val _ratsNow = it.asJsonObject.get("relationships").asJsonObject.get("rats").asJsonObject.get("data").asJsonArray

                val ratsNow : MutableList<String> = _ratsNow.map<JsonElement, String> { it.asJsonObject.get("id").asString }.toMutableList()

                ratsNow.forEach {
                    val tmp = it
                    rats.filter { it.uuid == tmp }

                }

                rats.removeIf{!ratsNow.contains(it.uuid)}

/*                rats.addAll(
                    ratsNow.filter{
                        !rats.map {
                            it.uuid }
                                .contains(it)
                    }
                    .map { Rat(resolveRatName(it), Status(""), it)}
                )*/

                ratsNow
                        .filterNot { rat -> rats.map { it.uuid }.contains(rat) }
                        .forEach { rats.add(Rat(resolveRatName(it, included), uuid= it)) }

                attributes.get("unidentifiedRats").asJsonArray.forEach { rats.add(Rat(it.asString, uuid="-1").setNameCorrectly())}

                /*
                The above ratsNow action is equivalent to:

                for (rat in ratsNow){
                    if (!rats.map { it.uuid }.contains(rat)){
                        rats.add(Rat(resolveRatName(rat), uuid=rat))
                    }
                }


                 */


                //.forEach { rats.add(Rat(resolveRatName(it.asJsonObject.get("id").asString), Status("")).setNameCorrectly()) }
            }


        }



    }
    launch{ delay(255); updateScreen()}

}

fun parseRatNameResolution(meta: JsonObject, data : JsonArray){
    val uuid = meta.get("mydata").asJsonObject.get("uuid").asString
    if (resolvMap.get(uuid) != "") return
    resolvMap.put(uuid, data[0].asJsonObject.get("attributes").asJsonObject.get("name").asString)
}

val ratNameMap = mutableMapOf<String, String>()
val resolvMap = mutableMapOf<String, String>()

fun resolveRatName(id: String, included : JsonArray): String {
    if (ratNameMap.containsKey(id)) return ratNameMap[id]!!
    included.forEach {
        if (it.asJsonObject.get("id").asString == id) {
            ratNameMap.put(id, it.asJsonObject.get("attributes").asJsonObject.get("name").asString)
            return ratNameMap[id]!!
        }
    }

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
    ratNameMap.put(id, name)
    return ratNameMap[id]!!
}