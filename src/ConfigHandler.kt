import org.apache.commons.io.IOUtils
import java.io.File
import java.io.PrintWriter
import java.lang.System
import java.nio.charset.Charset
import java.util.*

val configFile = File("config")
public val config = Config()


public class Config{
    val varMap = mutableMapOf<String, String>()

}

enum class entries{
    LogPathFr, ClientType, keyword, beep, LogPathRc, token, fontSize
}

/**
 * @return true if config already existed, false when it has been initialized with defaults
 */
fun initConfig() : Boolean{
    if (!configFile.exists()) {
        configFile.createNewFile()
        //DEFAULT CONFIG
        config.varMap["${entries.LogPathFr}"] = "${System.getenv("appdata")}hexchat/logs/fuelrats/#fuelrats.log"
        config.varMap["${entries.LogPathRc}"] = "${System.getenv("appdata")}hexchat/logs/fuelrats/#ratchat.log"
        config.varMap["${entries.ClientType}"] = "hexchat"
        config.varMap["${entries.keyword}"] = "ratsignal"
        config.varMap["${entries.beep}"] = "true"
        config.varMap["${entries.fontSize}"] = "20"
        config.varMap["${entries.token}"] = ""


    }
    return true
}

fun readConfig(){
    val lines : MutableList<String> = IOUtils.readLines(configFile.inputStream(), Charset.defaultCharset())
    lines
            .filter { it -> it.reduce().toCharArray()[0] != '#' } //allows for comments
            .map { it -> it.reduce()}
            .forEach { msg ->
                entries.values().forEach {
                    if (msg.reduce().startsWith("$it: ")) {
                        config.varMap.put("$it", msg.reduce().split("$it: ")[1].toLowerCase())
                    }
                }
            }

    println("token is: ${config.varMap["${entries.token}"]}")
    if ("${config.varMap["${entries.token}"]}" == "null"){config.varMap["${entries.token}"] = ""}
}

fun saveConfig(){
    PrintWriter(configFile.path).close()
    val tmp = ArrayList<String>()
    val stream = configFile.outputStream()

    entries.values().forEach {
        val value = config.varMap["$it"]
        if ("$it" == "token"){
            tmp.add("#NEVER EVER SHARE THIS TOKEN WITH ANYBODY")
        }

        tmp.add("$it: $value")
    }


    IOUtils.writeLines(tmp, null, stream, Charset.defaultCharset())
    stream.flush()
    stream.close()
}
