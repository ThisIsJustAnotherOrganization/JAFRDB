import Trilean.*
import jcurses.system.Toolkit
import org.apache.commons.io.input.Tailer
import org.apache.commons.io.input.TailerListenerAdapter
import java.io.File
import java.lang.Exception
import java.util.regex.Pattern

var LogFile : File = File(config.LogPath)
val listen = listener()
val tailer = Tailer.create(LogFile, listen, 20, true)

enum class supportedClients{hexchat, mirc}


var tailerStopped = false


class listener : TailerListenerAdapter(){
    val ratsigRegex = "RATSIGNAL.*CMDR\s\02?(.*?)\02?\s\-.*System\72\s\02?(.*?)(?:\s[Ss][Yy][Ss][Tt][Ee][Mm])?\02?\s\(.*Platform\72\s\02?(?:\03\d\d)?(\w+)\03?\02?.*O2\72\s((?:NOT\s)?OK).*Language\72\s.*\((..).*\(Case\s#(\d*)\)".toRegex()
    //regex at https://regex101.com/r/Vjtkxk/2
    
    override fun init(tailer: Tailer?) {
    }

    override fun handle(l: String?) {
        try {
            var valid : Boolean = false
            supportedClients.values().forEach {if (it.toString() == config.ClientType){valid = true} }
            if (config.ClientType.isNullOrBlank()) config.ClientType = "hexchat" //throw IllegalStateException("empty clienttype")
            if (!valid) throw IllegalStateException("clientType not supported")
            this.javaClass.getMethod(config.ClientType.toLowerCase(), String::class.java).invoke(this, l)
        } catch(e: Exception) {
            toPrint.add(e.toString())
            e.printStackTrace()
            while (true);
        }
    }

    fun hexchat(l : String?){
        var line: String = l!!//.replace("\t", " ", true)
        line = line.replace("\t", " ")
        val nick = line.split(Pattern.compile(" "), 5)[3].replace("<", "").replace(">", "").replace("+", "").replace("%", "").replace("@", "").replace("~", "").replace("&", "") // strip: +%@~&
        line = line.split(Pattern.compile(" "), 5)[4].trim()
        handleMessage(nick, line)
    }

    fun mirc(l : String?){
        var line : String = l!!.replace("\t", " ")
        val nick = line.split(Pattern.compile(" "), 3)[1].replace("<", "").replace(">", "").replace("+", "").replace("%", "").replace("@", "").replace("~", "").replace("&", "") // strip: +%@~&
        line = line.split(Pattern.compile(" "), 3)[2].trim().replace("[\\x02\\x1F\\x0F\\x16]|\\x03(\\d\\d?(,\\d\\d?)?)?".toRegex(), "")
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
                    if (message.split(Pattern.compile(" "), 3).size < 3) return
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

                "!sys", "!system" -> {
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
                "!cmdr", "!commander" -> {
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
                val matches : MatchGroupCollection = ratsigRegex.matchEntire(message)?.groups
                val name : String = matches.get(1)?.value
                val system : String = matches.get(2)?.value
                val platform : String = matches.get(3)?.value
                val cr : Boolean = matches.get(4)?.value != "OK"
                val lang : String = matches.get(5)?.value
                val number : Int = matches.get(6)?.value.toInt()

                rescues.add(Rescue(name, System(system), lang, number, platform, cr))

            }
            else {
                message = message.toLowerCase()
                if (message.contains("fr+")) {
                    getCase(message, nick).rats.filter { it.name == nick }.forEach { it.status.friended = TRUE }
                }

                if (message.contains("wr+")) {
                    getCase(message, nick).rats.filter { it.name == nick }.forEach { it.status.winged = TRUE; it.status.friended = TRUE }
                }

                if (message.contains("beacon+") || message.contains("bc+")) {
                    getCase(message, nick).rats.filter { it.name == nick }.forEach { it.status.beacon = TRUE; it.status.winged = TRUE; it.status.friended = TRUE; it.status.inSys = TRUE }
                }

                if (message.contains("sys+")) {
                    getCase(message, nick).rats.filter { it.name == nick }.forEach { it.status.inSys = TRUE }
                }

                if (message.contains("fuel+")) {
                    getCase(message, nick).rats.filter { it.name == nick }.forEach { it.status.fueled = TRUE }
                }

                if (message.contains("inst-")) {
                    getCase(message, nick).rats.filter { it.name == nick }.forEach { it.status.instancingP = TRUE }
                    Toolkit.beep()
                }

                if (message.contains("inst+")) {
                    getCase(message, nick).rats.filter { it.name == nick }.forEach { it.status.instancingP = FALSE }
                }

                if (message.startsWith("int") || message.contains("interdic")) {
                    getCase(message, nick).rats.filter { it.name == nick }.forEach { it.status.interdicted = TRUE }
                }

                if (message.contains("clear")){
                    getCase(message, nick).rats.filter{it.name == nick}.forEach { it.status.interdicted = TRUE }
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
        if (number.replace("#", "").toIntOrNull() != null) {
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
                    if (valid && res.active){ret.add(res); break}
                }
            }
            if (ret.isEmpty()){
                return Rescue("", System(""), "", -1, "", false)
            }
            return ret[0]
        }

    }

    override fun handle(ex: Exception?) {
        tailerStopped = true
        //toPrint.add(ex?.toString()!!)
        ex!!.printStackTrace()
    }

    override fun fileNotFound() {
        toPrint.add("File Not Found")
        tailer.stop()
    }
}
