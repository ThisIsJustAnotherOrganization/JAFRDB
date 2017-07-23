import java.util.*

fun print(inp : Any){
    println(inp)
}

fun main(args: Array<String>) {
    times(5, ::print)
}

fun times(i : Int, function: (i : Int) -> Unit){
    var x : Int = 0
    while (x < i){
        function(x + 1)
        x++
    }


}
enum class Rank{none, recruit, rat, overseer, techrat, admin, op}

data class User(var name : String, val rank: Rank)
data class System(var name : String)
data class Status(var status : String){
    var friended : Boolean = false
    var winged : Boolean = false
    var beacon : Boolean = false
    var fueled : Boolean = false
    var disconnected : Boolean = false
    var instanced : Boolean = false
    var closed : Boolean = false
}
data class Rescue(val client : User, var clientSystem : System, val language : String){
    var status : Status = Status("")
    var rats : MutableList<User> = ArrayList()

}


