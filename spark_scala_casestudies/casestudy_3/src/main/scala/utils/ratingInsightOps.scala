package utils

import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, SparkSession}

class ratingInsightOps(val enrichedDf: DataFrame, val normalizedDf: DataFrame) {

    def calculateAvgHighRating(): DataFrame = {
      val movieRatingsStatsDF = enrichedDf
        .groupBy("movieId")
        .agg(
          avg("rating").as("averageRating"),
          count("rating").as("totalRatings"),
          variance("rating").as("ratingVariance")
        )
      val finalDf = movieRatingsStatsDF
        .join(normalizedDf, Seq("movieId"), "inner")

      finalDf.coalesce(1).write.mode("overwrite").json("src/main/datasets/json_format/avgRating")
      finalDf

    }


    def topMoviesWithHighCountRatings(): DataFrame = {
      val movieStatsDF = enrichedDf
        .groupBy("movieId")
        .agg(
          avg("rating").as("averageRating"),
          count("rating").as("totalRatings")
        )
        .filter(col("totalRatings") >= 50)


      val enrichedMovieStatsDF = movieStatsDF
        .join(normalizedDf, Seq("movieId"), "inner")
        .select("movieId", "title", "averageRating", "totalRatings")

      val top10MoviesDF = enrichedMovieStatsDF
        .orderBy(col("averageRating").desc)
        .limit(10)

      top10MoviesDF.coalesce(1).write.mode("overwrite").json("src/main/datasets/json_format/top10Movies")
      top10MoviesDF
    }


    def aggregateByGenre(): DataFrame = {
      val genreStatsDF = enrichedDf
        .groupBy("genres")
        .agg(
          count("rating").as("totalRatings"),
          avg("rating").as("averageRating"),
          count("userId").as("totalUsers")
        )
      val mostPopularGenreDF = genreStatsDF
        .orderBy(col("totalRatings").desc)
        .limit(1)

      mostPopularGenreDF.coalesce(1).write.mode("overwrite").json("src/main/datasets/json_format/mostPopularGenre")
      mostPopularGenreDF
    }

    def calculateAvgRatingPerYear(): DataFrame = {
      val avgRatingPerYearDF = enrichedDf
        .withColumn("year", col("ratings_timestamp").substr(0, 4))
        .groupBy("year")
        .agg(avg("rating").as("averageRating"))

      avgRatingPerYearDF.coalesce(1).write.mode("overwrite").json("src/main/datasets/json_format/avgRatingPerYear")
      avgRatingPerYearDF
    }

    def calculateActiveUser():DataFrame = {
      val activeUsersDF = enrichedDf
        .groupBy("userId")
        .agg(
          count("rating").as("numRatings")
        )
        .orderBy(desc("numRatings"))

      activeUsersDF.coalesce(1).write.mode("overwrite").json("src/main/datasets/json_format/activeUsers")
      activeUsersDF
    }
}
