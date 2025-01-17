package utils

import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions._
import org.apache.spark.sql.expressions.Window

class pivotAndAnomalyCalculation(val enrichedDf: DataFrame){

  def showAverageRatingsPerGenreByYear(): DataFrame = {
    val ratingsWithYear = enrichedDf
      .withColumn("year", year(to_date(col("ratings_timestamp"))))
    val avgRatingsPerGenreByYear = ratingsWithYear
      .withColumn("genres", split(col("genres"), "\\|"))
      .selectExpr("explode(genres) as genre", "year", "rating")
      .groupBy("genre", "year")
      .agg(avg("rating").as("average_rating"))

    // Says that for each genre we get this avg_rating for each year
    val pivotTable = avgRatingsPerGenreByYear
      .groupBy("genre")
      .pivot("year")
      .agg(round(avg("average_rating"), 2))

    pivotTable.coalesce(1).write.mode("overwrite").json("src/main/datasets/json_format/pivotTable")
    pivotTable

  }

  //Calculating Anomalies with z-score
  def detectAnomaliesForGenreByRating() ={
    val genreStatsDF = enrichedDf
      .groupBy("genres")
      .agg(
        avg("rating").as("mean_rating"),
        stddev("rating").as("stddev_rating")
      )

    val ratingsWithStatsDF = enrichedDf
      .join(genreStatsDF, "genres")

    val ratingsWithZScoreDF = ratingsWithStatsDF
      .withColumn(
        "z_score",
        (col("rating") - col("mean_rating")) / col("stddev_rating"))


    val anomaliesDF = ratingsWithZScoreDF.filter(abs(col("z_score")) > 3)
      .select("movieId", "title", "genres", "rating", "z_score")

    anomaliesDF.show()
    anomaliesDF.coalesce(1).write.mode("overwrite").json("src/main/datasets/json_format/anomaliesDF")

    // Previous & Next 7 days time window for calculating anomalies
    val windowSpec = Window.partitionBy("genres").orderBy("ratings_timestamp").rangeBetween(-7 * 24 * 3600, 7 * 24 * 3600)

    val timeBasedAnomalies = enrichedDf
      .withColumn("rolling_avg", avg("rating").over(windowSpec))
      .withColumn("rolling_stddev", stddev("rating").over(windowSpec))
      .withColumn("z_score", (col("rating") - col("rolling_avg")) / col("rolling_stddev"))
      .filter(abs(col("z_score")) > 3)
    timeBasedAnomalies.coalesce(1).write.mode("overwrite").json("src/main/datasets/json_format/timeBasedAnomalies")
    timeBasedAnomalies.show()
  }


  //Calculate rating distributions for each genre
  def ratingPercentageByGenre()={
    val genreRatingCounts = enrichedDf
      .groupBy("genres", "rating")
      .agg(count("*").as("rating_count"))

    val totalRatingsPerGenre = enrichedDf
      .groupBy("genres")
      .agg(count("ratings").as("total_ratings"))

    val ratingDistribution = genreRatingCounts
      .join(totalRatingsPerGenre, "genres")
      .withColumn(
        "percentage",
        (col("rating_count") / col("total_ratings")) * 100
      )
      .orderBy("genres", "rating")

    ratingDistribution.show()
    ratingDistribution.coalesce(1).write.mode("overwrite").json("src/main/datasets/json_format/ratingDistributionDF")

  }
}
