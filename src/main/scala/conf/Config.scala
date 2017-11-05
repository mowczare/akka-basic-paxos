package conf

import com.typesafe.config.ConfigFactory

object Config {
  val config = ConfigFactory.load()
}
