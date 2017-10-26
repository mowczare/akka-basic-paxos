package conf

import com.typesafe.config.{Config, ConfigFactory}

object Config {
  val config: Config = ConfigFactory.load()
}
