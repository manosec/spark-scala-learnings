package repositories

import javax.inject.{Inject, Singleton}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import scala.concurrent.{ExecutionContext, Future}
import model.{ReceptionistEmployee, ReceptionistEmployeeTable}

@Singleton
class ReceptionistRepository @Inject()(val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) 
    extends HasDatabaseConfigProvider[JdbcProfile] {
    
    import profile.api._
    
    private val receptionistEmployeeTable = TableQuery[ReceptionistEmployeeTable]

    def findByEmailAndPassword(email: String, password: String): Future[Option[ReceptionistEmployee]] = {
        db.run(receptionistEmployeeTable
            .filter(p => p.email === email && p.password === password)
            .result.headOption)
    }

    def findByEmail(email: String): Future[Option[ReceptionistEmployee]] = {
        db.run(receptionistEmployeeTable.filter(_.email === email).result.headOption)
    }

    def create(receptionist: ReceptionistEmployee): Future[ReceptionistEmployee] = {
        db.run((receptionistEmployeeTable returning receptionistEmployeeTable.map(_.id))
            += receptionist)
            .map(id => receptionist.copy(id = id))
    }
} 