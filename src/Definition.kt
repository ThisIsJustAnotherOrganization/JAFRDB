import Trilean.NEUTRAL
import kotlin.properties.Delegates.observable

/**
 * Created by beepbeat/holladiewal on 18.11.2017.
 */

data class System(var name : String)
data class Status(var status : String){
    var friended : Trilean by observable(NEUTRAL, ::rescueChanged)
    var winged : Trilean by observable(NEUTRAL, ::rescueChanged)
    var beacon : Trilean by observable(NEUTRAL, ::rescueChanged)
    var inSys : Trilean by observable(NEUTRAL, ::rescueChanged)
    var fueled : Trilean by observable(NEUTRAL, ::rescueChanged)
    var disconnected : Trilean by observable(NEUTRAL, ::rescueChanged)
    var instancingP: Trilean by observable(NEUTRAL, ::rescueChanged)
    var interdicted: Trilean by observable(NEUTRAL, ::rescueChanged)
}
class Rescue(client : String, clientSystem : System, language : String, number : Int, platform : String, cr : Boolean, uuid: String, active : Boolean = true){

    var client : String by observable(client, ::rescueChanged)
    var clientSystem : System by observable(clientSystem, ::rescueChanged)
    val language : String by observable(language, ::rescueChanged)
    var number : Int by observable(number, ::rescueChanged)
    var platform : String by observable(platform, ::rescueChanged)
    var cr : Boolean by observable(cr, ::rescueChanged)
    var active : Boolean by observable(active, ::rescueChanged)
    val UUID : String = uuid



    var rats : WatchableMutableList<Rat> = WatchableMutableList(::updateScreen)
    var notes : WatchableMutableList<String> = WatchableMutableList(::updateScreen)

    override fun hashCode(): Int {
        return client.hashCode() + clientSystem.name.hashCode() - language.hashCode() + number - platform.hashCode() + "$cr".hashCode() - "$active".hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return this.hashCode() == other!!.hashCode()
    }

}

enum class Trilean{
    TRUE, FALSE, NEUTRAL;
}

fun rescueChanged(prop : Any, old : Any, new : Any){
    //println()
    //if (old != new)
        //updateScreen()
}