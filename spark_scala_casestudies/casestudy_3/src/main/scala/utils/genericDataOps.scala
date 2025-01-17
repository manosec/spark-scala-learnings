package utils
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions._

object genericDataOps {

  //Load csv data from GCS
  def loadData(gcsPath: String, spark: SparkSession): DataFrame = {
    spark.read.format("csv").option("header", true).option("inferSchema", true).load(gcsPath)
  }

  //Remove row that contains null values
  def cleanData(df: DataFrame, datasetName: String): DataFrame = {
    var finalDf: DataFrame = null
    if (datasetName == "movie"){
      finalDf  = df.na.fill(Map("genres" -> "unknown"))
      finalDf  = df.na.fill(Map("title" -> "unknown"))
    } else if(datasetName == "ratings")
    {
      finalDf = df.filter(col("rating").between(0, 5))
    } else if(datasetName == "tags")
    {
      finalDf = df.na.fill(Map("tag" -> "No Tag"))
    }
    finalDf.na.drop()
  }

  def explodeColumn(df: DataFrame, column: String, pattern: String): DataFrame = {
    df.withColumn(column, explode(split(col(column), pattern)))
  }



  def enrichData(movieDf: DataFrame, ratingsDf: DataFrame, tagsDf: DataFrame, linksDf:DataFrame): DataFrame = {
    val movieRatingDf = movieDf.join(ratingsDf, Seq("movieId"), "inner")

    val tagDf = tagsDf.withColumnRenamed("timestamp", "tags_timestamp")
    val tagCombinedDf = movieRatingDf.join(tagDf, Seq("movieId", "userId"), "left")

    val finalEnrichedDf = tagCombinedDf.join(linksDf, Seq("movieId"), "inner")

    val finalDf = finalEnrichedDf.select(
      col("userId"),
      col("movieId"),
      col("title"),
      col("genres"),
      col("rating"),
      col("timestamp").as("ratings_timestamp"),
      col("tag"),
      col("tags_timestamp"),
      col("imdbId"),
      col("tmdbId")
    )

    finalDf

  }
}
