import org.pac4j.core.client.Clients
import org.pac4j.oauth.client.TwitterClient
import org.pac4j.play.Config
import play.api._

object Global extends GlobalSettings {
  override def onStart(app: Application) {
    val config = Play.current.configuration
    val twitterClient = new TwitterClient(config.getString("twitter.key").get, config.getString("twitter.secret").get)

    val callbackURL: String = config.getString("twitter.callback").get
    val clients: Clients = new Clients(callbackURL, twitterClient)
    Config.setClients(clients)

    Logger.info("Application has started")
  }
}