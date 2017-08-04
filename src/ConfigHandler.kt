import org.apache.commons.io.IOUtils
import java.io.File
import java.io.PrintWriter

val configFile = File("config")
val config = Config()
class Config{
    var authkey : String = ""
    var LogPath : String = ""
    var ClientType : String = ""

}

fun initConfig(){
    if (!configFile.exists()) configFile.createNewFile()
}

fun readConfig(){
    val lines : MutableList<String> = IOUtils.readLines(configFile.inputStream()) as MutableList<String>
    for (str in lines){
        if (str.trim().toLowerCase().startsWith("authkey: ")){
            config.authkey = str.trim().toLowerCase().split("authkey: ")[1]}
        if (str.trim().toLowerCase().startsWith("logpath: ")){
            config.LogPath = str.trim().toLowerCase().split("logpath: ")[1]}
        if (str.trim().toLowerCase().startsWith("cleinttype: ")){
            config.ClientType = str.trim().toLowerCase().split("clienttype: ")[1]}
    }
}

fun saveConfig(){
    PrintWriter(configFile.path).close()
    val tmp = ArrayList<String>()
    val stream = configFile.outputStream()
    tmp.add("authkey: " + config.authkey)
    tmp.add("logpath: " + config.LogPath)
    tmp.add("clienttype: " + config.ClientType)
    IOUtils.writeLines(tmp, null, stream)
    stream.flush()
    stream.close()
}
