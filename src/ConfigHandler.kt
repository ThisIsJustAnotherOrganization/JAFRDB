import org.apache.commons.io.IOUtils
import java.io.File
import java.io.PrintWriter
import java.nio.charset.Charset
import java.util.*

val configFile = File("config")
val config = Config()

class Config{
    var LogPathFr = ""
    var ClientType = ""
    var keyword = ""
    var beep = false
    var LogPathRc = ""
    var email = ""
    var password = ""
    var token = ""

}

enum class entries{
    test1, test2, test3
}

fun initConfig(){
    if (!configFile.exists()) configFile.createNewFile()
}

fun readConfig(){
    val lines : MutableList<String> = IOUtils.readLines(configFile.inputStream(), Charset.defaultCharset())
    lines.forEach {

        if (it.reduce().toCharArray()[0] == '#') return@forEach //allows for comments
        val msg = it.reduce().toLowerCase()

        /*if (msg.startsWith("logpath: ")){
            config.LogPathFr = msg.split("logpath: ")[1]}
        if (msg.startsWith("logpathfr: ")){
            config.LogPathFr = msg.split("logpathfr: ")[1]}
        if (msg.startsWith("logpathrc: ")){
            config.LogPathRc = msg.split("logpathrc: ")[1]}
        if (msg.startsWith("clienttype: ")){
            config.ClientType = msg.split("clienttype: ")[1] }
        if (msg.startsWith("keyword: ")){
            config.keyword = msg.split("keyword: ")[1]}
        if (msg.startsWith("beep: ")){
            config.beep = msg.split("beep: ")[1] == "true"}
        if (msg.startsWith("email: ")){
            config.email = msg.split("email: ")[1]}
        if (msg.startsWith("password: ")){
            config.password = msg.split(entries.test1.toString())[1]}
        */
        entries.values().forEach {
            if (config.javaClass.getDeclaredField("$it").type == String::class) {
                if (msg.reduce().toLowerCase().startsWith("$it: ")) {
                    config.javaClass.getDeclaredField("$it").set(Any(), it)
                }
            } else {
                if (msg.reduce().toLowerCase().startsWith("$it: ")) {
                    config.javaClass.getDeclaredField("$it").setBoolean(Any(), "$it" == "true")
                }
            }
        }

    }
}

fun saveConfig(){
    PrintWriter(configFile.path).close()
    val tmp = ArrayList<String>()
    val stream = configFile.outputStream()
    tmp.add("logpathfr: " + config.LogPathFr)
    tmp.add("logpathrc: " + config.LogPathRc)
    tmp.add("clienttype: " + config.ClientType)
    tmp.add("keyword: " + config.keyword)
    tmp.add("beep: " + config.beep.toString())
    IOUtils.writeLines(tmp, null, stream, Charset.defaultCharset())
    stream.flush()
    stream.close()
}
