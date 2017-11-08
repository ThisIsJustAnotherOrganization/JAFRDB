import com.google.api.client.auth.oauth2.AuthorizationCodeFlow
import com.google.api.client.auth.oauth2.BearerToken
import com.google.api.client.auth.oauth2.ClientParametersAuthentication
import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.http.GenericUrl
import com.google.api.client.http.apache.ApacheHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.JsonGenerator
import com.google.api.client.json.JsonParser
import com.google.api.client.testing.json.MockJsonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import java.io.*
import java.nio.charset.Charset

/**
 * Created by beepbeat/holladiewal on 08.11.2017.
 */

class AuthHandler{

    var token = BearerToken()

    fun authorize(userId : String): Credential {
        var dataStorage = FileDataStoreFactory(File("dataStore"))
        var flow = AuthorizationCodeFlow.Builder(BearerToken.authorizationHeaderAccessMethod(), ApacheHttpTransport(), MockJsonFactory(), GenericUrl(""), ClientParametersAuthentication(secret.clientID, secret.clientSecret ), secret.clientID, "https://dev.api.fuelrats.com/oauth2/login").setScopes(listOf("*")).setDataStoreFactory(dataStorage).build()
        var receiver = LocalServerReceiver.Builder().setHost("localhost").setPort(12558).build()
        return AuthorizationCodeInstalledApp(flow, receiver).authorize(userId)
    }
}

class JsonFac : JsonFactory() {
    override fun createJsonGenerator(out: OutputStream?, enc: Charset?): JsonGenerator {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createJsonGenerator(writer: Writer?): JsonGenerator {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createJsonParser(`in`: InputStream?): JsonParser {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createJsonParser(`in`: InputStream?, charset: Charset?): JsonParser {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createJsonParser(value: String?): JsonParser {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createJsonParser(reader: Reader?): JsonParser {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}

