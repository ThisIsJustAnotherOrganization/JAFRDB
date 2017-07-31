import com.google.gson.JsonParser

class WebApiHandler{
    val parser : JsonParser = JsonParser()
    fun getLastRescues(amount : Int): /*List<Rescue>*/ Unit {
        var response : List<String> = WAPIinst.request("https://api.fuelrats.com/rescues")
        var responseStr : String = ""
        for (str in response) {responseStr += str}
        parser.parse(responseStr)
    }
}