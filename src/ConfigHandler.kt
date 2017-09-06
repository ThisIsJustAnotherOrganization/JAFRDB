import org.apache.commons.io.IOUtils
import java.io.File
import java.io.PrintWriter
import java.nio.charset.Charset
import java.util.*

val configFile = File("config")
val config = Config()

class Config{
        var LogPathFr: String = ""
        var ClientType: String = ""
        var keyword: String = ""
        var beep = false
        var LogPathRc: String = ""

}

fun initConfig(){
    if (!configFile.exists()) configFile.createNewFile()
}

fun readConfig(){
    val lines : MutableList<String> = IOUtils.readLines(configFile.inputStream(), Charset.defaultCharset())
    for (str in lines){
        if (str.trim().toLowerCase().startsWith("logpath: ")){
            config.LogPathFr = str.trim().toLowerCase().split("logpath: ")[1]}

        if (str.trim().toLowerCase().startsWith("logpathfr: ")){
            config.LogPathFr = str.trim().toLowerCase().split("logpathfr: ")[1]}

        if (str.trim().toLowerCase().startsWith("logpathrc: ")){
            config.LogPathRc = str.trim().toLowerCase().split("logpathrc: ")[1]}

        if (str.trim().toLowerCase().startsWith("clienttype: ")){
            config.ClientType = str.trim().toLowerCase().split("clienttype: ")[1] }

        if (str.trim().toLowerCase().startsWith("keyword: ")){
            config.keyword = str.trim().toLowerCase().split("keyword: ")[1]}

        if (str.trim().toLowerCase().startsWith("beep: ")){
            config.beep = str.trim().toLowerCase().split("beep: ")[1] == "true"
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
