import org.apache.commons.io.IOUtils
import java.io.File

val configFile = File("config")
class Config{
    lateinit var authkey : String
    lateinit var LogPath : String

}

fun init(){
    if (!configFile.exists()) configFile.createNewFile()
}

fun read(){
    var lines = IOUtils.readLines(configFile.inputStream())
    for (str in lines){

    }
}
