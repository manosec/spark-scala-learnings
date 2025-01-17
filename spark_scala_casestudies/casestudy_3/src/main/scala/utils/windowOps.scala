package utils

import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions._

class windowOps (enrichedDf: DataFrame, cleanedMovieDf: DataFrame, cleanedRatingsDf: DataFrame) {

  def rankMoviesByGenre() :DataFrame= {
    val movieGenreRatingsDF = enrichedDf
      .groupBy("movieId", "title", "genres") // Group by movieId, title, and genres
      .agg(
        count("rating").as("rating_count"),  // Total number of ratings
        avg("rating").as("average_rating")  // Average rating
      )

    val genreWindow = Window.partitionBy("genres").orderBy(desc("rating_count"), desc("average_rating"))

    val rankedMoviesDF = movieGenreRatingsDF
      .withColumn("rank", rank().over(genreWindow))
      .orderBy("genres", "rank")
    rankedMoviesDF.coalesce(1).write.mode("overwrite").json("src/main/datasets/json_format/rankedMoviesDF")
    rankedMoviesDF

  }

  def rollingAvg30() :DataFrame= {
    val ratingsWithUnixTimeDF = cleanedMovieDf.join(cleanedRatingsDf, Seq("movieId"), "inner")
      .withColumn("timestamp", unix_timestamp((col("timestamp"))))

    val rollingWindowSpec = Window
      .partitionBy("movieId")
      .orderBy("timestamp")
      .rangeBetween(-2592000, 0) // Represents the 30days in seconds

    val rollingAvgRatingsDF = ratingsWithUnixTimeDF
      .withColumn("rolling_avg_rating", avg("rating").over(rollingWindowSpec))
      .orderBy("movieId", "timestamp")
    rollingAvgRatingsDF.coalesce(1).write.mode("overwrite").json("src/main/datasets/json_format/rollingAvgRatingsDF")
    rollingAvgRatingsDF
  }

  def userActivityTrendAnalyse(): Unit={
    val dailyActivityDF = enrichedDf
      .withColumn("ratings_timestamp_date", to_date(col("ratings_timestamp")))
      .groupBy("userId","ratings_timestamp_date")
      .agg(count("userId").as("total_ratings"))
      .orderBy("ratings_timestamp_date")
    dailyActivityDF.coalesce(1).write.mode("overwrite").json("src/main/datasets/json_format/dailyActivityDF")
    dailyActivityDF.show()

    val monthlyActivityDF = enrichedDf
      .withColumn("month", month(to_date(col("ratings_timestamp"))))
      .groupBy("userId", "month")
      .agg(count("userId").as("total_ratings"))
      .orderBy("month")
    monthlyActivityDF.coalesce(1).write.mode("overwrite").json("src/main/datasets/json_format/monthlyActivityDF")
    monthlyActivityDF.show()
  }

}
