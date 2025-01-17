package utils

import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions._
import java.io.File
import plotly.layout.{Axis, BarMode, Layout}
import plotly.{Bar, Plotly, Scatter}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.expressions.Window



class generateVisualReport(val enrichedDf: DataFrame, val spark: SparkSession){
  import spark.implicits._

  def createTheReport() = {

    val genreTrendsData = enrichedDf
      .groupBy("genres")
      .agg(
        avg("rating").alias("avg_rating"),
        countDistinct("userId").alias("unique_users"),
        count("rating").alias("total_ratings")
      )
      .orderBy(desc("total_ratings"))

    genreTrendsData.show()
    genreTrendsData.coalesce(1).write.mode("overwrite").json("src/main/reports/visualizationAndReporting")


    val userActivity = enrichedDf
      .groupBy(col("userId")) // Group data by userId
      .agg(
        count(col("movieId")).alias("movies_rated"),
        avg(col("rating")).alias("avg_rating"),
        stddev(col("rating")).alias("rating_stddev")
      )
      .orderBy(desc("movies_rated"))

    userActivity.show()
    userActivity.coalesce(1).write.mode("overwrite").json("src/main/datasets/json_format/userActivity")

    val topRatedMovies = enrichedDf
      .groupBy("movieId")
      .agg(
        avg("rating").alias("avg_rating"),
        count("rating").alias("num_ratings")
      )
      .filter(col("num_ratings") > 100)
      .orderBy(desc("avg_rating"))

    topRatedMovies.show()
    topRatedMovies.coalesce(1).write.mode("overwrite").json("src/main/datasets/json_format/topRatedMovies")


  }

  def generateTheVisual(): Unit = {
    // Calculate average ratings per year
    calculateAverageRatingsByYear()

    // Calculate rolling average ratings
    calulateRollingAverage()
  }


  def calculateAverageRatingsByYear() = {
    val ratingsWithYear = enrichedDf.withColumn("year", year(col("ratings_timestamp")))
    val avgRatingsPerYear = ratingsWithYear
      .groupBy("year")
      .agg(avg("rating").alias("avg_rating"))
      .orderBy("year")

    import spark.implicits._

    val years = avgRatingsPerYear.select("year").as[Int].collect()
    val avgRatings = avgRatingsPerYear.select("avg_rating").as[Double].collect()
    val yearlyPlot = Bar(
      x = years.map(_.toString).toSeq, // Ensure proper Seq type
      y = avgRatings.toSeq,            // Ensure proper Seq type
      name = "Average Ratings"
    )
    val layout = Layout(
      title = "Ratings Analysis",
      xaxis = Axis(title = "Time"),
      yaxis = Axis(title = "Average Ratings"),
      barmode = BarMode.Group
    )
    Plotly.plot("src/main/reports/avgRatingByYear.html", Seq(yearlyPlot), layout)

    // Show the results
    val outputDir = "src/main/reports/avgRatingsByYear"

    // Save the results
    avgRatingsPerYear
      .coalesce(1)
      .write
      .mode("Overwrite")
      .csv(outputDir)

  }

  def calulateRollingAverage(): Unit = {

    val windowSpec = Window.orderBy("ratings_timestamp").rowsBetween(-365, 0)
    val rollingAvgRatings = enrichedDf
      .withColumn("rolling_avg", avg("rating").over(windowSpec))

    rollingAvgRatings.show()

    // Extract year
    val plotDataFrame = rollingAvgRatings
      .withColumn("year", year($"ratings_timestamp")) // Extract the year
      .groupBy("year")
      .agg(avg("rolling_avg").alias("avg_rating")) // Calculate average ratings per year
      .orderBy("year")

    // Collect data for visualization
    val years = plotDataFrame.select("year").as[Int].collect()
    val avgRatings = plotDataFrame.select("avg_rating").as[Double].collect()

    // Rolling Average By Year plot
    val yearlyPlot = Bar(
      x = years.map(_.toString).toSeq,
      y = avgRatings.toSeq,
      name = "Rolling Average Ratings"
    )

    // Layout for the plot
    val layout = Layout(
      title = "Yearly Average Ratings",
      xaxis = Axis(title = "Year"),
      yaxis = Axis(title = "Rolling Average Ratings"),
      barmode = BarMode.Group
    )


    Plotly.plot("src/main/reports/rollingAvgRating.html", Seq(yearlyPlot), layout)

    // Rolling average data
    rollingAvgRatings
      .select("ratings_timestamp", "rolling_avg")
      .coalesce(1)
      .write
      .mode("Overwrite")
      .csv("src/main/reports/RollingAverage")
  }

}
