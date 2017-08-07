import jcurses.system.Toolkit
import org.apache.commons.io.input.Tailer
import org.apache.commons.io.input.TailerListenerAdapter
import java.io.File
import java.lang.Exception
import java.util.regex.Pattern

var LogFile : File = File(config.LogPath)
val listen = listener()
val tailer = Tailer.create(LogFile, listen, 20, true)

enum class supportedClients{hexchat}


var tailerStopped = false


class listener : TailerListenerAdapter(){
    override fun init(tailer: Tailer?) {
    }

    override fun handle(l: String?) {
        var valid : Boolean = false
        supportedClients.values().forEach {if (it.toString() == config.ClientType){valid = true} }
        if (config.ClientType.isNullOrBlank()) throw IllegalStateException("empty clienttype")
        if (!valid) throw IllegalStateException("clientType not supported")
        this.javaClass.getMethod(config.ClientType.toLowerCase(), String::class.java).invoke(this, l)
    }

    fun hexchat(l : String?){
        var line: String = l!!//.replace("\t", " ", true)
        line = line.replace("\t", " ")
        val nick = line.split(Pattern.compile(" "), 5)[3].replace("<", "").replace(">", "").replace("+", "").replace("%", "").replace("@", "").replace("~", "").replace("&", "") // strip: +%@~&
        line = line.split(Pattern.compile(" "), 5)[4].trim()
        handleMessage(nick, line)
    }



    fun handleMessage(nick : String, message: String){
        @Suppress("NAME_SHADOWING")
        var message = message
        try{
        if (message.toCharArray()[0] == '!'){
            //handle Mecha comm
            when(message.split(Pattern.compile(" "), 2)[0]){
                "!go", "!assign" -> {
                    var number = message.split(" ")[1]
                    val rt = message.split(Pattern.compile(" "), 3)[2]
                    var rats = ArrayList<Rat>()
                    rt.split(" ").mapTo(rats) { Rat(it, Status("")) }
                    if (number.contains("#") || number.toIntOrNull() != null){
                        number = number.replace("#", "")
                        rescues.filter { it.number == number.toInt() }.forEach { it.rats.addAll(rats)}
                    }
                    else{
                        rescues.filter { it.client == number }.forEach { it.rats.addAll(rats) }
                    }
                }

                "!unassign" -> {
                    var number = message.split(" ")[1]
                    val rt = message.split(Pattern.compile(" "), 3)[2]
                    var rats = ArrayList<Rat>()
                    rt.split(" ").mapTo(rats) { Rat(it, Status("")) }
                    if (number.contains("#") || number.toIntOrNull() != null){
                        number = number.replace("#", "")

                        rescues.filter { it.number == number.toInt() }.forEach { it.rats.removeAll(rats)}
                    }
                    else{
                        rescues.filter { it.client == number }.forEach { it.rats.removeAll(rats) }
                    }
                }

                "!clear", "!close", "!md" -> {
                    var number = message.split(" ")[1]
                    if (number.contains("#") || number.toIntOrNull() != null){
                        number = number.replace("#", "")
                        rescues.filter { it.number == number.toInt() }.forEach { rescues.remove(it) }
                    }
                    else{
                        rescues.filter { it.client == number }.forEach { rescues.remove(it) }
                    }
                }

                "!cr" -> {
                    var number = message.split(" ")[1]
                    if (number.contains("#") || number.toIntOrNull() != null){
                        number = number.replace("#", "")
                        rescues.filter { it.number == number.toInt() }.forEach { it.cr = !it.cr }
                    }
                    else{
                        rescues.filter { it.client == number }.forEach { it.cr = !it.cr }
                    }

                }

                "!sys" -> {
                    var number = message.split(" ")[1]
                    val sys = message.split(Pattern.compile(" "), 3)[2]
                    if (number.contains("#") || number.toIntOrNull() != null){
                        number = number.replace("#", "")
                        rescues.filter { it.number == number.toInt() }.forEach { it.clientSystem.name = sys }
                    }
                    else{
                        rescues.filter { it.client == number }.forEach { it.clientSystem.name = sys }
                    }
                }
                "!cmdr" -> {
                    var number = message.split(" ")[1]
                    val cmdr = message.split(Pattern.compile(" "), 3)[2]
                    if (number.contains("#") || number.toIntOrNull() != null){
                        number = number.replace("#", "")
                        rescues.filter { it.number == number.toInt() }.forEach { it.client = cmdr }
                    }
                    else{
                        rescues.filter { it.client == number }.forEach { it.client = cmdr }
                    }
                }

                "!xb", "!ps", "!pc" -> {
                    var number = message.split(" ")[1]
                    val platform = message.split(Pattern.compile(" "), 3)[0].replace("!", "")
                    if (number.contains("#") || number.toIntOrNull() != null){
                        number = number.replace("#", "")
                        rescues.filter { it.number == number.toInt() }.forEach { it.platform = platform}
                    }
                    else{
                        rescues.filter { it.client == number }.forEach { it.platform = platform }
                    }
                }

                "!active", "!inactive" -> {
                    var number = message.split(" ")[1]
                    if (number.contains("#") || number.toIntOrNull() != null){
                        number = number.replace("#", "")
                        rescues.filter { it.number == number.toInt() }.forEach { it.active = !it.active }
                    }
                    else{
                        rescues.filter { it.client == number }.forEach { it.active = !it.active }
                    }

                }

            }
        }
        else {
            if (message.startsWith("RATSIGNAL")){
                //RATSIGNAL - CMDR killcrazycarl - System: COL 285 sector GM-V D2-110 (225.32 LY from Sothis) - Platform: XB - O2: OK - Language: English (en-US) (Case #1)
                val parts = message.split(" - ")
                val name : String = parts[1].replace("CMDR ", "")
                val system : String = parts[2].replace("System: ", "").split("(")[0]
                val platform : String = parts[3].replace("Platform: ", "")
                val cr : Boolean = parts[4].replace("O2: ", "") != "OK"
                val lang : String = parts[5].replace("Language: ", "").split(" ")[1].replace("(", "").replace(")", "").split("-")[0]
                val number : Int = parts.last().split(" ").last().replace("(", "").replace(")", "").replace("#", "").toInt()

                rescues.add(Rescue(name, System(system), lang, number, platform, cr))

            }
            else {
                message = message.toLowerCase()
                if (message.contains("fr+")) {
                    getCase(message, nick).rats.filter { it.name == nick }.forEach { it.status.friended = true }
                }

                if (message.contains("wr+")) {
                    getCase(message, nick).rats.filter { it.name == nick }.forEach { it.status.winged = true; it.status.friended = true }
                }

                if (message.contains("beacon+") || message.contains("bc+")) {
                    getCase(message, nick).rats.filter { it.name == nick }.forEach { it.status.beacon = true; it.status.winged = true; it.status.friended = true; it.status.inSys = true }
                }

                if (message.contains("sys+")) {
                    getCase(message, nick).rats.filter { it.name == nick }.forEach { it.status.inSys = true }
                }

                if (message.contains("fuel+")) {
                    getCase(message, nick).rats.filter { it.name == nick }.forEach { it.status.fueled = true }
                }

                if (message.contains("inst-")) {
                    getCase(message, nick).rats.filter { it.name == nick }.forEach { it.status.instancingP = true }
                    Toolkit.beep()
                }

                if (message.contains("inst+")) {
                    getCase(message, nick).rats.filter { it.name == nick }.forEach { it.status.instancingP = false }
                }

                if (message.startsWith("int") || message.contains("interdic")) {
                    getCase(message, nick).rats.filter { it.name == nick }.forEach { it.status.interdicted = true }
                }

                if (message.contains("clear")){
                    getCase(message, nick).rats.filter{it.name == nick}.forEach { it.status.interdicted = false }
                }

                //TODO implement Disconnected

            }
        }
    } catch(e: Exception) {
            e.printStackTrace()
        }
    }

    fun getCase(line: String, nick: String): Rescue {
        val names = ArrayList<String>()
        var number : String = ""
        rescues.forEach { names.add(it.client.toLowerCase()) }
        line.split(" ").forEach { if (names.contains(it) || it.contains("#") || it.toIntOrNull() != null){number = it} }
        if (number.contains("#") || number.toIntOrNull() != null) {
            number = number.replace("#", "")
            val ret = rescues.filter{it.number == number.toInt()}
            if (ret.isEmpty()) return Rescue("", System(""), "", -1, "", false)
            return ret[0]
        }
        else{
            var ret = rescues.filter{it.client == number}.toMutableList()
            if (ret.isEmpty()){
                for (res in rescues) {
                    var valid = false
                    res.rats.forEach { if (it.name == nick){valid = true}}
                    if (valid){ret.add(res); break}
                }
            }
            return ret[0]
        }

    }

    override fun handle(ex: Exception?) {
        tailerStopped = true
        //toPrint.add(ex?.toString()!!)
        ex!!.printStackTrace()
    }
}