package repositories

import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import scala.concurrent.Future

trait Repository[T] {
    val dbConfigProvider: DatabaseConfigProvider
    lazy val dbConfig = dbConfigProvider.get[JdbcProfile]
    import dbConfig._
    import profile.api._
    protected val db = dbConfig.db
} 