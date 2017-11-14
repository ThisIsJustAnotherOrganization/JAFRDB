
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.layout.StackPane
import javafx.scene.text.Text
import javafx.stage.Stage
import org.dmfs.httpessentials.client.HttpRequestExecutor
import org.dmfs.httpessentials.httpurlconnection.HttpUrlConnectionExecutor
import org.dmfs.oauth2.client.BasicOAuth2AuthorizationProvider
import org.dmfs.oauth2.client.BasicOAuth2Client
import org.dmfs.oauth2.client.BasicOAuth2ClientCredentials
import org.dmfs.oauth2.client.OAuth2AccessToken
import org.dmfs.oauth2.client.grants.AuthorizationCodeGrant
import org.dmfs.oauth2.client.scope.BasicScope
import org.dmfs.rfc3986.encoding.Encoded
import org.dmfs.rfc3986.uris.LazyUri
import org.dmfs.rfc5545.Duration
import java.net.URI


/**
 * Created by beepbeat/holladiewal on 08.11.2017.
 */

class AuthHandler{
    companion object {
        lateinit var url : URI
        var redirectUrl : String = ""
    }

    var executor: HttpRequestExecutor = HttpUrlConnectionExecutor()
    lateinit var token : OAuth2AccessToken


    fun authorize(){
        val provider = BasicOAuth2AuthorizationProvider(URI.create("https://beta.fuelrats.com/authorize"), URI.create("https://dev.api.fuelrats.com/oauth2/token"), Duration(1,0,3600))
        val credentials = BasicOAuth2ClientCredentials(secret.clientID, secret.clientSecret)
        val client = BasicOAuth2Client(provider, credentials, LazyUri(Encoded("https://localhost:13370")))
        val grant = AuthorizationCodeGrant(client, BasicScope("rescue.read", "rat.read"))
        url = grant.authorizationUrl()!!
        println(url)

        val auth = Auth.Authenticator("")
        Application.launch(UrlDialog().javaClass)
        val result = auth.awaitAccessToken()
        println("$result")
        if (!result){
            throw IllegalStateException("couldnt start Server " + auth.authenticationError?.name + "," + auth.authenticationError?.description)
        }
        while (!auth.hasAccessToken()){
            ;
        }
        //token = grant.withRedirect(client.redirectUri()).accessToken(executor)
    }



}

class UrlDialog : Application() {
    override fun start(stage: Stage?) {
        val url = AuthHandler.url.toString()
        val root = StackPane()
        root.children.add(Text(200.0, 32.0, url))
        val scene = Scene(root, 800.0, 64.0)
        stage!!.scene = scene
        stage.show()
    }
}




