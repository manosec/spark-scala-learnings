package repositories

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import model.{PremiseDept, PremiseDeptTable}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.MySQLProfile.api._

class PremiseDeptRepository @Inject()(
    val dbConfigProvider: DatabaseConfigProvider
)(implicit ec: ExecutionContext) extends Repository[PremiseDept] {
    
    private val premiseDeptTable = TableQuery[PremiseDeptTable]

    def findAll(): Future[Seq[PremiseDept]] = 
        db.run(premiseDeptTable.result)

    def findById(id: Int): Future[Option[PremiseDept]] = 
        db.run(premiseDeptTable.filter(_.id === id).result.headOption)

    def create(premiseDept: PremiseDept): Future[PremiseDept] = 
        db.run((premiseDeptTable returning premiseDeptTable.map(_.id)) += premiseDept)
          .map(id => premiseDept.copy(id = id))

    def update(premiseDept: PremiseDept): Future[Int] = {
        println(s"Updating department: $premiseDept")
        
        val updateQuery = premiseDeptTable
            .filter(_.id === premiseDept.id)
            .map(dept => (dept.name, dept.email, dept.mobile, dept.role))
            .update((premiseDept.name, premiseDept.email, premiseDept.mobile, premiseDept.role))
            
        db.run(updateQuery)
    }

    def delete(id: Int): Future[Int] = 
        db.run(premiseDeptTable.filter(_.id === id).delete)
}