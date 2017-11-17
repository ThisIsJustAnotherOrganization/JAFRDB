import Trilean.*
import org.apache.commons.io.FileUtils
import org.apache.commons.io.input.Tailer
import org.apache.commons.io.input.TailerListenerAdapter
import java.io.File
import java.io.PrintStream
import java.io.PrintWriter
import java.lang.Exception
import java.util.*
import java.util.regex.Pattern

var LogFilefr = File(config.varMap["${entries.LogPathFr}"])
var LogFileRc = File(config.varMap["${entries.LogPathRc}"])
val listenfr  = listener()
val listenrc  = rclistener()
val tailerfr  = Tailer.create(LogFilefr, listenfr, 20, true)
val tailerrc  = Tailer.create(LogFileRc, listenrc, 20, true)

enum class supportedClients{hexchat, mirc, ii}


var frtailerStopped = false
var rctailerStopped = false


open class listener : TailerListenerAdapter(){
    val ratsigRegex = (config.varMap["${entries.keyword}"]!!.toUpperCase() + """.*CMDR\s\02?(.*?)\02?\s\-.*System\72\s\02?(.*?)(?:\s[Ss][Yy][Ss][Tt][Ee][Mm])?\02?\s\(.*Platform\72\s\02?(?:\03\d\d)?(\w+).*O2\72\s\02?(?:\03\d\d)?((?:NOT\s)?OK).*Language\72\s.*\((..).*\(Case\s#(\d*)\)""").toRegex()
    //regex at https://regex101.com/r/Vjtkxk/4

    var stackFile = PrintStream(File("stacktrace.log"))
    override fun init(tailer: Tailer?) {
    }

    override fun handle(l: String?) {
        try {
            if (l == null || l.isEmpty()){return}
            var valid = false
            supportedClients.values().forEach {if (it.toString() == config.varMap["${entries.ClientType}"]){valid = true} }
            if (config.varMap["${entries.ClientType}"]!!.isBlank()) throw IllegalStateException("empty clienttype") //config.ClientType = "hexchat"
            if (!valid) throw IllegalStateException("clientType not supported")
            this.javaClass.getMethod(config.varMap["${entries.ClientType}"]?.toLowerCase(), String::class.java).invoke(this, l)
        } catch(e: Exception) {
            toPrint.add(e.toString())
            e.printStackTrace(stackFile)
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
        line = line.split(Pattern.compile(" "), 3)[2].trim()//.replace("[\\x02\\x1F\\x0F\\x16]|\\x03(\\d\\d?(,\\d\\d?)?)?".toRegex(), "")
        handleMessage(nick, line)
    }

    fun ii(l : String?){
        var line : String = l!!.replace("\t", " ")
        val nick = line.split(Pattern.compile(" "), 4)[2].replace("<", "").replace(">", "").replace("+", "").replace("%", "").replace("@", "").replace("~", "").replace("&", "") // strip: +%@~&
        if (nick == "-!-") return
        line = line.split(Pattern.compile(" "), 4)[3].trim()//.replace("[\\x02\\x1F\\x0F\\x16]|\\x03(\\d\\d?(,\\d\\d?)?)?".toRegex(), "")
        handleMessage(nick, line)
    }

    fun getNumber(l : String): String {
        val VALID = setOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
        val number = l.filter{ VALID.contains(it) }
        return number
    }

    fun containsNumber(l : String): Boolean {
        val VALID = setOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
        val isNumber = l.any{ VALID.contains(it) }
        return isNumber
    }



    fun handleMessage(nick : String, message: String){
        @Suppress("NAME_SHADOWING")
        var message = message.strip().reduce()
        try {

            if (message == "DEBUG") {
                val file = File("debug.log")
                if (!file.exists()) file.createNewFile()
                PrintWriter(file.path).close()
                FileUtils.writeLines(file, rescues.asStringArray())
            }

            if (nick.toLowerCase() == "internal") {
                if (message.contains("quit", true)){

                }

                return
            }
            else {
                if (message.toCharArray()[0] == '!') {
                    //handle Mecha comm
                    when (message.split(" ", limit = 2)[0]) {
                        "!go", "!assign" -> {
                            var number = message.split(" ")[1]
                            if (message.split(" ", limit = 3).size < 3) return
                            val rt = message.split(" ", limit = 3)[2]
                            var rats = ArrayList<Rat>()
                            rt.split(" ").mapTo(rats) { Rat(it, Status("")).setNameCorrectly() }
                            if (number.contains("#").and(containsNumber(number)) || number.toIntOrNull() != null) {
                                number = getNumber(number)
                                rescues.filter { it.number == number.toInt() }.forEach { val tmp = it.rats; it.rats.addAll(rats.filter { !tmp.contains(it) && it.name.isNotBlank() }) }
                            } else {
                                rescues.filter { it.number == number.toInt() }.forEach { val tmp = it.rats; it.rats.addAll(rats.filter { !tmp.contains(it) && it.name.isNotBlank() }) }
                            }
                        }

                        "!unassign" -> {
                            var number = message.split(" ")[1]
                            val rt = message.split(" ", limit = 3)[2]
                            var rats = ArrayList<Rat>()
                            rt.split(" ").mapTo(rats) { Rat(it, Status("")).setNameCorrectly() }
                            if (number.contains("#").and(containsNumber(number)) || number.toIntOrNull() != null) {
                                number = getNumber(number)

                                rescues.filter { it.number == number.toInt() }.forEach { it.rats.removeAll(rats) }
                            } else {
                                rescues.filter { it.client == number }.forEach { it.rats.removeAll(rats) }
                            }
                        }

                        "!clear", "!close", "!md" -> {
                            var number = message.split(" ")[1]
                            if (number.contains("#").and(containsNumber(number)) || number.toIntOrNull() != null) {
                                number = getNumber(number)
                                rescues.filter { it.number == number.toInt() }.forEach { rescues.remove(it) }
                            } else {
                                rescues.filter { it.client == number }.forEach { rescues.remove(it) }
                            }
                        }

                        "!cr" -> {
                            var number = message.split(" ")[1]
                            if (number.contains("#").and(containsNumber(number)) || number.toIntOrNull() != null) {
                                number = getNumber(number)
                                rescues.filter { it.number == number.toInt() }.forEach { it.cr = !it.cr }
                            } else {
                                rescues.filter { it.client == number }.forEach { it.cr = !it.cr }
                            }

                        }

                        "!sys", "!system" -> {
                            var number = message.split(" ")[1]
                            val sys = message.split(" ", limit = 3)[2]
                            if (number.contains("#").and(containsNumber(number)) || number.toIntOrNull() != null) {
                                number = getNumber(number)
                                rescues.filter { it.number == number.toInt() }.forEach { it.clientSystem.name = sys }
                            } else {
                                rescues.filter { it.client == number }.forEach { it.clientSystem.name = sys }
                            }
                        }
                        "!cmdr", "!commander" -> {
                            var number = message.split(" ")[1]
                            val cmdr = message.split(" ", limit = 3)[2]
                            if (number.contains("#").and(containsNumber(number)) || number.toIntOrNull() != null) {
                                number = getNumber(number)
                                rescues.filter { it.number == number.toInt() }.forEach { it.client = cmdr }
                            } else {
                                rescues.filter { it.client == number }.forEach { it.client = cmdr }
                            }
                        }

                        "!xb", "!ps", "!pc" -> {
                            var number = message.split(" ")[1]
                            val platform = message.split(" ", limit = 3)[0].replace("!", "")
                            if (number.contains("#").and(containsNumber(number)) || number.toIntOrNull() != null) {
                                number = getNumber(number)
                                rescues.filter { it.number == number.toInt() }.forEach { it.platform = platform }
                            } else {
                                rescues.filter { it.client == number }.forEach { it.platform = platform }
                            }
                        }

                        "!active", "!inactive" -> {
                            var number = message.split(" ")[1]
                            if (number.contains("#").and(containsNumber(number)) || number.toIntOrNull() != null) {
                                number = getNumber(number)
                                rescues.filter { it.number == number.toInt() }.forEach { it.active = !it.active }
                            } else {
                                rescues.filter { it.client == number }.forEach { it.active = !it.active }
                            }

                        }

                    //actually not in Mecha
                        "!changenumber", "!cn" -> {
                            var number = message.split(" ")[1]
                            if (number.contains("#").and(containsNumber(number)) || number.toIntOrNull() != null) {
                                number = getNumber(number)
                                rescues.filter { it.number == number.toInt() }.forEach { it.number = message.split(" ")[2].toInt() }
                            } else {
                                rescues.filter { it.client == number }.forEach { it.number = message.split(" ")[2].toInt() }
                            }
                        }

                    }
                } else {
                    if (message.startsWith(config.varMap["${entries.keyword}"]!!.toUpperCase())) {
                        //RATSIGNAL - CMDR killcrazycarl - System: COL 285 sector GM-V D2-110 (225.32 LY from Sothis) - Platform: XB - O2: OK - Language: English (en-US) (Case #1)
                        //RATSIGNAL - CMDR test - System: COL 285 sector GM-V D2-110 (225.32 LY from Sothis) - Platform: XB - O2: OK - Language: English (en-US) (Case #1)
                        //RATSIGNAL - CMDR Condor Aybarra - System: MN-t B3-6 Alrai Sector (not in EDDB) - Platform: PC - O2: OK - Language: English (en-US) - IRC Nickname: Condor_Aybarra (Case #3)
                        /* val matches : MatchGroupCollection = ratsigRegex.matchEntire(message)?.groups!!
                val name : String = matches.get(1)?.value ?: ""
                val system : String = matches.get(2)?.value ?: ""
                val platform : String = matches.get(3)?.value ?: ""
                val cr : Boolean = matches.get(4)?.value != "OK"
                val lang : String = matches.get(5)?.value ?: ""
                val number : Int = matches.get(6)?.value?.toInt() ?: -1*/

                        val parts = message.split(" - ")
                        if (parts.size >= 6) {
                            var name: String = parts[1].replace("CMDR ", "")
                            val system: String = parts[2].replace("System: ", "").split("(")[0]
                            val platform: String = parts[3].replace("Platform: ", "")
                            val cr: Boolean = parts[4].replace("O2: ", "") != "OK"
                            val lang: String = parts[5].replace("Language: ", "").split(" ")[1].replace("(", "").replace(")", "").split("-")[0]
                            val number: Int = getNumber(parts.last().split(" ").last()).toIntOrNull() ?: -1

                            if (message.contains("IRC Nickname:")) {
                                name = parts.last().replace("IRC Nickname:", "").trim().split(" ")[0]
                            }


                            rescues.add(Rescue(name, System(system), lang, number, platform, cr))
                        }

                    } else {
                        message = message.toLowerCase()
                        if (message.contains("fr+")) {
                            getCase(message, nick).rats.filter { it.name == nick }.forEach { it.status.friended = TRUE }
                        }

                        if (message.contains("fr-")) {
                            getCase(message, nick).rats.filter { it.name == nick }.forEach { it.status.friended = FALSE; beep() }
                        }

                        if (message.contains("wr+")) {
                            getCase(message, nick).rats.filter { it.name == nick }.forEach { it.status.winged = TRUE }
                        }

                        if (message.contains("wr-")) {
                            getCase(message, nick).rats.filter { it.name == nick }.forEach { it.status.winged = FALSE; it.status.beacon = NEUTRAL; beep() }
                        }

                        if (message.contains("beacon+") || message.contains("bc+")) {
                            getCase(message, nick).rats.filter { it.name == nick }.forEach { it.status.beacon = TRUE; it.status.winged = TRUE; it.status.inSys = TRUE }
                        }

                        if (message.contains("beacon-") || message.contains("bc-")) {
                            getCase(message, nick).rats.filter { it.name == nick }.forEach { it.status.beacon = FALSE; beep() }
                        }

                        if (message.contains("sys+")) {
                            getCase(message, nick).rats.filter { it.name == nick }.forEach { it.status.inSys = TRUE }
                        }

                        if (message.contains("fuel+")) {
                            getCase(message, nick).rats.filter { it.name == nick }.forEach { it.status.fueled = TRUE }
                        }

                        if (message.contains("inst-")) {
                            getCase(message, nick).rats.filter { it.name == nick }.forEach { it.status.instancingP = TRUE; beep() }
                            beep()
                        }

                        if (message.contains("inst+")) {
                            getCase(message, nick).rats.filter { it.name == nick }.forEach { it.status.instancingP = FALSE }
                        }

                        if (message.startsWith("int") || message.contains("interdic")) {
                            getCase(message, nick).rats.filter { it.name == nick }.forEach { it.status.interdicted = TRUE; beep() }
                        }

                        if (message.contains("clear")) {
                            getCase(message, nick).rats.filter { it.name == nick }.forEach { it.status.interdicted = FALSE }
                        }

                        //TODO implement Disconnected

                    }
                }
            }
        } catch(e: Exception) {
            e.printStackTrace(stackFile)
        }
    }

    fun getCase(line: String, nick: String): Rescue {
        val names = ArrayList<String>()
        var number : String = ""
        rescues.forEach { names.add(it.client.toLowerCase()) }
        line.split(" ").forEach { if (names.contains(it) || it.contains("#") || it.toIntOrNull() != null){number = it} }
        if (number.replace("#", "").replace("c", "").toIntOrNull() != null) {
            number = getNumber(number)

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
        frtailerStopped = true
        //toPrint.add(ex?.toString()!!)
        ex!!.printStackTrace(stackFile)
    }

    override fun fileNotFound() {
        toPrint.add("Path to the #Fuelrats log-file is incorrect. Please check your config, make sure you included the file name AND extension and restart")
        tailerfr.stop()
    }

}

class rclistener : listener() {
     override fun handle(ex: Exception?) {
        rctailerStopped = true
        ex!!.printStackTrace(stackFile)
    }

    override fun fileNotFound() {
        toPrint.add("Path to the #Ratchat log-file is incorrect. Please check your config, make sure you included the file name AND extension and restart")
        tailerrc.stop()
    }
}