import org.apache.commons.io.input.Tailer
import org.apache.commons.io.input.TailerListenerAdapter
import java.io.File
import java.lang.Exception
import java.util.regex.Pattern

var LogFile : File = File(config.LogPath)
val listen = listener()
val tailer = Tailer.create(LogFile, listen, 20, true)


var tailerStopped = false


class listener : TailerListenerAdapter(){
    override fun init(tailer: Tailer?) {
        toPrint.add("Tailer started")
    }

    override fun fileNotFound() {
        toPrint.add("Log not found!")
    }
    override fun handle(l: String?) {
        var line = l!!
        var nick = line.split(Pattern.compile(" "), 5)[3].replace("<", "").replace(">", "")
        line = line.split(Pattern.compile(" "), 5)[4].trim()
        if (line.toCharArray()[0] == '!'){
            //handle Mecha comm
            when(line.split(Pattern.compile(" "), 2)[0]){
                "!go", "!assign" -> {
                    var number = line.split(" ")[1]
                    val rt = line.split(Pattern.compile(" "), 3)[2]
                    var rats = ArrayList<Rat>()
                    rt.split(" ").mapTo(rats) { Rat(it, Status("")) }
                    if (number.contains("#") || number.toInt().toString() == number){
                        number = number.replace("#", "")

                        rescues.filter { it.number == number.toInt() }.forEach { it.rats.addAll(rats)}
                    }
                    else{
                        rescues.filter { it.client == number }.forEach { it.rats.addAll(rats) }
                    }
                }

                "!unassign" -> {
                    var number = line.split(" ")[1]
                    val rt = line.split(Pattern.compile(" "), 3)[2]
                    var rats = ArrayList<Rat>()
                    rt.split(" ").mapTo(rats) { Rat(it, Status("")) }
                    if (number.contains("#") || number.toInt().toString() == number){
                        number = number.replace("#", "")

                        rescues.filter { it.number == number.toInt() }.forEach { it.rats.removeAll(rats)}
                    }
                    else{
                        rescues.filter { it.client == number }.forEach { it.rats.removeAll(rats) }
                    }
                }

                "!clear", "!close", "!md" -> {
                    var number = line.split(" ")[1]
                    if (number.contains("#") || number.toInt().toString() == number){
                        number = number.replace("#", "")
                        rescues.filter { it.number == number.toInt() }.forEach { rescues.remove(it) }
                    }
                    else{
                        rescues.filter { it.client == number }.forEach { rescues.remove(it) }
                    }
                }

                "!cr" -> {
                    var number = line.split(" ")[1]
                    if (number.contains("#") || number.toInt().toString() == number){
                        number = number.replace("#", "")
                        rescues.filter { it.number == number.toInt() }.forEach { it.cr = !it.cr }
                    }
                    else{
                        rescues.filter { it.client == number }.forEach { it.cr = !it.cr }
                }

                }

                "!sys" -> {
                    var number = line.split(" ")[1]
                    val sys = line.split(Pattern.compile(" "), 3)[2]
                    if (number.contains("#") || number.toInt().toString() == number){
                        number = number.replace("#", "")
                        rescues.filter { it.number == number.toInt() }.forEach { it.clientSystem.name = sys }
                    }
                    else{
                        rescues.filter { it.client == number }.forEach { it.clientSystem.name = sys }
                    }
                }
                "!cmdr" -> {
                    var number = line.split(" ")[1]
                    val cmdr = line.split(Pattern.compile(" "), 3)[2]
                    if (number.contains("#") || number.toInt().toString() == number){
                        number = number.replace("#", "")
                        rescues.filter { it.number == number.toInt() }.forEach { it.client = cmdr }
                    }
                    else{
                        rescues.filter { it.client == number }.forEach { it.client = cmdr }
                    }
                }

                "!xb", "!ps", "!pc" -> {
                    var number = line.split(" ")[1]
                    val platform = line.split(Pattern.compile(" "), 3)[0].replace("!", "")
                    if (number.contains("#") || number.toInt().toString() == number){
                        number = number.replace("#", "")
                        rescues.filter { it.number == number.toInt() }.forEach { it.platform = platform}
                    }
                    else{
                        rescues.filter { it.client == number }.forEach { it.platform = platform }
                    }
                }

                "!active", "!inactive" -> {
                    var number = line.split(" ")[1]
                    if (number.contains("#") || number.toInt().toString() == number){
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
            if (line.startsWith("\tRATSIGNAL")){
                //RATSIGNAL - CMDR killcrazycarl - System: COL 285 sector GM-V D2-110 (225.32 LY from Sothis) - Platform: XB - O2: OK - Language: English (en-US) (Case #1)
                val parts = line.split(" - ")
                val name : String = parts[1].replace("CMDR ", "")
                val system : String = parts[2].replace("System: ", "").replace(Regex.fromLiteral("/\\([\\w\\d\\s.]*\\)/i"), "").trim()
                val platform : String = parts[3].replace("Platform: ", "")
                val cr : Boolean = parts[4].replace("O2: ", "") != "OK"
                val lang : String = parts[5].replace("Language: ", "").split(" ")[1].replace("(", "").replace(")", "").split("-")[0]
                val number : Int = parts[5].replace("Language: ", "").split(" ")[2].replace("(", "").replace(")", "").replace("Case #", "").toInt()

                rescues.add(Rescue(name, System(system), lang, number, platform, cr))

            }
            else{
                if (line.contains("fr+")) {
                    var number = line.split(" ")[1]
                    if (number.contains("#") || number.toInt().toString() == number){
                        number = number.replace("#", "")
                        rescues.filter { it.number == number.toInt() }.forEach { it.rats.filter { it.name == nick }.forEach { it.status.friended = true }}
                    }
                    else{
                        rescues.filter { it.number == number.toInt() }.forEach { it.rats.filter { it.name == nick }.forEach { it.status.friended = true }}
                    }
                }

                if (line.contains("wr+")) {
                    var number = line.split(" ")[1]
                    if (number.contains("#") || number.toInt().toString() == number){
                        number = number.replace("#", "")
                        rescues.filter { it.number == number.toInt() }.forEach { it.rats.filter { it.name == nick }.forEach { it.status.winged = true }}
                    }
                    else{
                        rescues.filter { it.number == number.toInt() }.forEach { it.rats.filter { it.name == nick }.forEach { it.status.winged = true }}
                    }
                }

                if (line.contains("beacon+") || line.contains("bc+")) {
                    var number = line.split(" ")[1]
                    if (number.contains("#") || number.toInt().toString() == number){
                        number = number.replace("#", "")
                        rescues.filter { it.number == number.toInt() }.forEach { it.rats.filter { it.name == nick }.forEach { it.status.beacon = true }}
                    }
                    else{
                        rescues.filter { it.number == number.toInt() }.forEach { it.rats.filter { it.name == nick }.forEach { it.status.beacon = true }}
                    }
                }

                if (line.contains("sys+")) {
                    var number = line.split(" ")[1]
                    if (number.contains("#") || number.toInt().toString() == number){
                        number = number.replace("#", "")
                        rescues.filter { it.number == number.toInt() }.forEach { it.rats.filter { it.name == nick }.forEach { it.status.inSys = true }}
                    }
                    else{
                        rescues.filter { it.number == number.toInt() }.forEach { it.rats.filter { it.name == nick }.forEach { it.status.inSys = true }}
                    }
                }

                if (line.contains("fuel+")) {
                    var number = line.split(" ")[1]
                    if (number.contains("#") || number.toInt().toString() == number){
                        number = number.replace("#", "")
                        rescues.filter { it.number == number.toInt() }.forEach { it.rats.filter { it.name == nick }.forEach { it.status.fueled = true }}
                    }
                    else{
                        rescues.filter { it.number == number.toInt() }.forEach { it.rats.filter { it.name == nick }.forEach { it.status.fueled = true }}
                    }
                }

                if (line.contains("inst-")) {
                    var number = line.split(" ")[1]
                    if (number.contains("#") || number.toInt().toString() == number){
                        number = number.replace("#", "")
                        rescues.filter { it.number == number.toInt() }.forEach { it.rats.filter { it.name == nick }.forEach { it.status.instancingP = true }}
                    }
                    else{
                        rescues.filter { it.number == number.toInt() }.forEach { it.rats.filter { it.name == nick }.forEach { it.status.instancingP = true }}
                    }
                }

                if (line.contains("int") || line.contains("inter") || line.contains("interdicted")) {
                    var number = line.split(" ")[1]
                    if (number.contains("#") || number.toInt().toString() == number){
                        number = number.replace("#", "")
                        rescues.filter { it.number == number.toInt() }.forEach { it.rats.filter { it.name == nick }.forEach { it.status.friended = true }}
                    }
                    else{
                        rescues.filter { it.number == number.toInt() }.forEach { it.rats.filter { it.name == nick }.forEach { it.status.friended = true }}
                    }
                }

                //TODO implement Disconnected

            }
        }
    }

    override fun handle(ex: Exception?) {
        tailerStopped = true
        toPrint.add(ex?.toString()!!)
    }
}