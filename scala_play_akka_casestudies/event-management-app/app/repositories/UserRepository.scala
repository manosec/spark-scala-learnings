package repositories

import models.{User, UserTable}
import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.MySQLProfile
import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.MySQLProfile.api._

@Singleton
class UserRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
    extends HasDatabaseConfigProvider[MySQLProfile] {

    private val users = TableQuery[UserTable]


    def create(user: User): Future[User] = {
        db.run((users returning users.map(_.id)
            into ((user, id) => user.copy(id = Some(id)))
        ) += user)
    }

    def getByEmail(email: String): Future[Option[User]] = 
        db.run(users.filter(_.email === email).result.headOption)

    def validateUser(email: String, password: String): Future[Option[User]] = 
        db.run(users.filter(user => user.email === email && user.password === password).result.headOption)
    def findByEmail(email: String): Future[Option[User]] =
        db.run(users.filter(_.email === email).result.headOption)
}
