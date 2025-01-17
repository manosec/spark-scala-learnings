import org.apache.spark.sql.SparkSession
import config.config.serviceAccountPath
import org.apache.spark.storage.StorageLevel
import utils.genericDataOps
import utils.ratingInsightOps
import utils.windowOps
import utils.pivotAndAnomalyCalculation
import utils.clusteringOfUserByRatings
import utils.generateVisualReport

object movieLensDataAnalysis {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder().appName("Movie Lens Data Exploration and Analysis").master("local[*]")
      .config("spark.driver.bindAddress","0.0.0.0")
      .config("spark.hadoop.fs.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFileSystem")
      .config("spark.hadoop.fs.AbstractFileSystem.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFS")
      .config("spark.hadoop.google.cloud.auth.service.account.enable", "true")
      .config("spark.hadoop.google.cloud.auth.service.account.json.keyfile", serviceAccountPath)
      .getOrCreate()

    val sprk_ctx = spark.sparkContext

    val movieDataPath = "gs://artifacts_spark/de-casestudy/usecase-3/source_data/movie.csv"
    val ratingsDataPath = "gs://artifacts_spark/de-casestudy/usecase-3/source_data/rating.csv"
    val tagsDataPath = "gs://artifacts_spark/de-casestudy/usecase-3/source_data/tag.csv"
    val linksDataPath = "gs://artifacts_spark/de-casestudy/usecase-3/source_data/link.csv"

    try{

      /* 1 Data Handling and Preprocessing*/

      // Load data
      val movieDf = genericDataOps.loadData(movieDataPath, spark)
      val ratingsDf = genericDataOps.loadData(ratingsDataPath, spark)
      val tagsDf = genericDataOps.loadData(tagsDataPath, spark)
      val linksDf = genericDataOps.loadData(linksDataPath, spark)

      // Handle Missing and invalid values
      val cleanedMovieDf = genericDataOps.cleanData(movieDf, "movie")
      val cleanedRatingsDf = genericDataOps.cleanData(ratingsDf, "ratings")
      val cleanedTagsDf = genericDataOps.cleanData(tagsDf, "tags")

      //Normalize Data
      val normalizedMoviesDf = genericDataOps.explodeColumn(cleanedMovieDf, "genres", "\\|")

      /* 2 Data Enrichment*/

      val enrichedData = genericDataOps.enrichData(normalizedMoviesDf, cleanedRatingsDf, cleanedTagsDf, linksDf)

      enrichedData.persist(StorageLevel.MEMORY_AND_DISK)

      /* 3 Advanced Aggregations*/

      val aggOps = new ratingInsightOps(enrichedData, cleanedMovieDf)

      aggOps.calculateAvgHighRating()
      aggOps.topMoviesWithHighCountRatings()
      aggOps.aggregateByGenre()
      aggOps.calculateAvgRatingPerYear()
      aggOps.calculateActiveUser()


      /* 4 Window Calculate Operations */
      val windowOps = new windowOps(enrichedData, cleanedMovieDf, cleanedRatingsDf)

      windowOps.rankMoviesByGenre()

      windowOps.rollingAvg30()

      windowOps.userActivityTrendAnalyse()

      /* 5 Pivoting and Complex Transformations */
      val pivOps = new  pivotAndAnomalyCalculation(enrichedData)

      pivOps.showAverageRatingsPerGenreByYear()
      pivOps.detectAnomaliesForGenreByRating()
      pivOps.ratingPercentageByGenre()

      /* 6 User Behavior Analysis */

      val userBehaveAnalysis = new clusteringOfUserByRatings(enrichedData)

      userBehaveAnalysis.clusterUser()

      userBehaveAnalysis.mostTaggedMovies()

      /* Storing the Enriched Dataset Paritioned By Genre */

      enrichedData.coalesce(1).write.mode("overwrite").partitionBy("genres").parquet("src/main/datasets/parquet/data")

      /* 8. Visualization and Reporting */

      val genVisual = new generateVisualReport(enrichedData, spark)
      genVisual.createTheReport()
      genVisual.generateTheVisual()

    enrichedData.unpersist()
    } finally {

      spark.stop()
    }
  }
}
