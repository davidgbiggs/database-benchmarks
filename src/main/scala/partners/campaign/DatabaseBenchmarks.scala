package partners.campaign

import partners.campaign.arangodb.ArangoDBBenchmark
import partners.campaign.mongodb.MongoDBBenchmark
import partners.campaign.neo4j.Neo4JBenchmark
import profig.{Config, ConfigApplication}

import scala.concurrent.Await
import scala.concurrent.duration._

object DatabaseBenchmarks extends ConfigApplication {
  private val databases: Map[String, DatabaseBenchmark] = List(
    ArangoDBBenchmark,
    MongoDBBenchmark,
    Neo4JBenchmark
  ).map(db => (db.name, db)).toMap

  lazy val config = Config("database")

  override def main(args: Array[String]): Unit = start(args)

  override protected def run(): Unit = {
    config("benchmark").as[Option[String]] match {
      case Some(db) => databases.get(db) match {
        case Some(benchmark) => {
          SimpleData.init()
          Await.result(benchmark.run(), Duration.Inf)
          scribe.info("Cool down...")
          Thread.sleep(5000L)
          sys.exit()
        }
        case None => println(s"`$db` is not a valid database benchmark. Valid options are: ${databases.keySet.mkString(", ")}")
      }
      case None => println(s"The benchmark must be specified with the option --database.benchmark=<database>. Valid options are: ${databases.keySet.mkString(", ")}")
    }
  }
}