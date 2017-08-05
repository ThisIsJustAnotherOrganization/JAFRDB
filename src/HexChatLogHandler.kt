import org.apache.commons.io.input.Tailer
import org.apache.commons.io.input.TailerListenerAdapter
import java.io.File
import java.lang.Exception
import java.util.regex.Pattern

var LogFile : File = File(config.LogPath + "\\#ratchat.log")
val listen = listener()
val tailer = Tailer.create(LogFile, listen, 20, true)


class listener : TailerListenerAdapter(){
    override fun init(tailer: Tailer?) {
        toPrint.add("Tailer started")
    }

    override fun fileNotFound() {
        toPrint.add("Log not found!")
    }
    override fun handle(l: String?) {
        var line = l!!
        line = line.split(Pattern.compile(" "), 4)[3]
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
                        rescues.filter { it.client.name == number }.forEach { it.rats.addAll(rats) }
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
                        rescues.filter { it.client.name == number }.forEach { it.rats.removeAll(rats) }
                    }
                }

                "!clear", "!close", "!md" -> {
                    var number = line.split(" ")[1]
                    if (number.contains("#") || number.toInt().toString() == number){
                        number = number.replace("#", "")
                        rescues.filter { it.number == number.toInt() }.forEach { rescues.remove(it) }
                    }
                    else{
                        rescues.filter { it.client.name == number }.forEach { rescues.remove(it) }
                    }
                }

                "!cr" -> {
                    var number = line.split(" ")[1]
                    if (number.contains("#") || number.toInt().toString() == number){
                        number = number.replace("#", "")
                        rescues.filter { it.number == number.toInt() }.forEach { it.cr = !it.cr }
                    }
                    else{
                        rescues.filter { it.client.name == number }.forEach { it.cr = !it.cr }
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
                        rescues.filter { it.client.name == number }.forEach { it.clientSystem.name = sys }
                    }
                }
                "!cmdr" -> {
                    var number = line.split(" ")[1]
                    val cmdr = line.split(Pattern.compile(" "), 3)[2]
                    if (number.contains("#") || number.toInt().toString() == number){
                        number = number.replace("#", "")
                        rescues.filter { it.number == number.toInt() }.forEach { it.client.name = cmdr }
                    }
                    else{
                        rescues.filter { it.client.name == number }.forEach { it.client.name = cmdr }
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
                        rescues.filter { it.client.name == number }.forEach { it.platform = platform }
                    }
                }

                "!active", "!inactive" -> {
                    var number = line.split(" ")[1]
                    if (number.contains("#") || number.toInt().toString() == number){
                        number = number.replace("#", "")
                        rescues.filter { it.number == number.toInt() }.forEach { it.active = !it.active }
                    }
                    else{
                        rescues.filter { it.client.name == number }.forEach { it.active = !it.active }
                    }

                }

            }
        }
        else {
            if (line.contains("fr")) {
                if (line.contains("fr+")) {
                }
            }
        }
    }

    override fun handle(ex: Exception?) {
        toPrint.add(ex?.toString()!!)
    }
}