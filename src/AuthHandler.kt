
import javafx.application.Application
import javafx.stage.Stage
import kotlinx.coroutines.experimental.launch
import org.dmfs.oauth2.client.BasicOAuth2AuthorizationProvider
import org.dmfs.oauth2.client.BasicOAuth2Client
import org.dmfs.oauth2.client.BasicOAuth2ClientCredentials
import org.dmfs.oauth2.client.grants.AuthorizationCodeGrant
import org.dmfs.oauth2.client.scope.BasicScope
import org.dmfs.rfc3986.encoding.Precoded
import org.dmfs.rfc3986.uris.LazyUri
import org.dmfs.rfc5545.Duration
import org.swingplus.JHyperlink
import java.awt.Rectangle
import java.net.URI
import javax.swing.JFrame


/**
 * Created by beepbeat/holladiewal on 08.11.2017.
 */

class AuthHandler{
    companion object {
        lateinit var url : URI
        var redirectUrl : String = ""
    }

    fun authorize(){
        val provider = BasicOAuth2AuthorizationProvider(URI.create("https://fuelrats.com/authorize"), URI.create("https://api.fuelrats.com/oauth2/token"), Duration(1,0,3600))
        val credentials = BasicOAuth2ClientCredentials(secret.clientID, secret.clientSecret)
        val client = BasicOAuth2Client(provider, credentials, LazyUri(Precoded("https://localhost:13370")))
        val grant = AuthorizationCodeGrant(client, BasicScope("*"))
        url = grant.authorizationUrl()!!
        println(url)

        val auth = Auth.Authenticator("")
        launch{Application.launch(UrlDialog().javaClass)}
        val result = auth.awaitAccessToken()
        UrlDialog.frame.dispose()
        println("$result")
        if (!result){
            throw IllegalStateException("couldnt start Server " + auth.authenticationError?.name + "," + auth.authenticationError?.description)
        }
        while (!auth.hasAccessToken()){
            ;
        }
    }



}

class UrlDialog : Application() {
    companion object {
        val frame = JFrame()
    }



    override fun start(stage: Stage?) {
        val url = AuthHandler.url.toString()
        frame.bounds = Rectangle(800, 600)
        frame.add(JHyperlink("Click here!", url))
        frame.isVisible = true
    }
}




