import org.apache.commons.io.IOUtils
import java.io.File
import java.io.PrintWriter
import java.nio.charset.Charset
import java.util.*

val configFile = File("config")
public val config = Config()


public class Config{
    val varMap = mutableMapOf<String, String>()

}

enum class entries{
    LogPathFr, ClientType, keyword, beep, LogPathRc, token
}

fun initConfig(){
    if (!configFile.exists()) configFile.createNewFile()
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
    if (config.varMap["${entries.token}"] == ""){
        AuthHandler().authorize()
        println(config.varMap["${entries.token}"])
    }
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
