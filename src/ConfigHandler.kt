import org.apache.commons.io.IOUtils
import java.io.File
import java.io.PrintWriter
import java.nio.charset.Charset

val configFile = File("config")
val config = Config()

class Config{
        var authkey: String = ""
        var LogPath: String = ""
        var ClientType: String = ""
        var keyword: String = ""

}

fun initConfig(){
    if (!configFile.exists()) configFile.createNewFile()
}

fun readConfig(){
    val lines : MutableList<String> = IOUtils.readLines(configFile.inputStream(), Charset.defaultCharset())
    for (str in lines){
        if (str.trim().toLowerCase().startsWith("authkey: ")){
            config.authkey = str.trim().toLowerCase().split("authkey: ")[1]}
        if (str.trim().toLowerCase().startsWith("logpath: ")){
            config.LogPath = str.trim().toLowerCase().split("logpath: ")[1]}
        if (str.trim().toLowerCase().startsWith("clienttype: ")){
            config.ClientType = str.trim().toLowerCase().split("clienttype: ")[1] }
        if (str.trim().toLowerCase().startsWith("keyword: ")){
            config.keyword = str.trim().toLowerCase().split("keyword: ")[1]
        }
    }
}

fun saveConfig(){
    PrintWriter(configFile.path).close()
    val tmp = ArrayList<String>()
    val stream = configFile.outputStream()
    tmp.add("authkey: " + config.authkey)
    tmp.add("logpath: " + config.LogPath)
    tmp.add("clienttype: " + config.ClientType)
    tmp.add("keyword: " + config.keyword)
    IOUtils.writeLines(tmp, null, stream, Charset.defaultCharset())
    stream.flush()
    stream.close()
}
