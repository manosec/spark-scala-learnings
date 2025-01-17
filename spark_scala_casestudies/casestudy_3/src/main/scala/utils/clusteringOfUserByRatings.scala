package utils

import org.apache.spark.ml.clustering.KMeans
import org.apache.spark.ml.feature.{StandardScaler, VectorAssembler}
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions._

class clusteringOfUserByRatings(val enrichedDf: DataFrame) {

  def clusterUser() = {

    val userFeaturesDF = enrichedDf
      .groupBy("userId")
      .agg(
        avg("rating").as("avg_rating"),
        stddev("rating").as("rating_variance"),
        count("rating").as("rating_count")
      )
      .na.fill(0, Seq("rating_variance"))

    val assembler = new VectorAssembler()
      .setInputCols(Array("avg_rating", "rating_variance", "rating_count"))
      .setOutputCol("features")

    val featureData = assembler.transform(userFeaturesDF)

    val scaler = new StandardScaler()
      .setInputCol("features")
      .setOutputCol("scaled_features")
      .setWithMean(true)
      .setWithStd(true)

    val scaledData = scaler.fit(featureData).transform(featureData)

    val kmeans = new KMeans()
      .setK(4) // Number of clusters; can be tuned
      .setFeaturesCol("scaled_features")
      .setPredictionCol("cluster")

    val kmeansModel = kmeans.fit(scaledData)
    val clusteredUsers = kmeansModel.transform(scaledData)

    val finalCluster = clusteredUsers.groupBy("cluster")
      .agg(
        avg("avg_rating").as("avg_rating_in_cluster"),
        avg("rating_variance").as("variance_in_cluster"),
        avg("rating_count").as("rating_count_in_cluster"),
        count("*").as("user_count")
      )

    finalCluster.show()
    finalCluster.coalesce(1).write.mode("overwrite").json("src/main/datasets/json_format/UserAnalaysiCluster")
  }

  // Get Most Tagged Movies
  def mostTaggedMovies(): Unit = {
    import org.apache.spark.sql.functions._

    val mostTaggedMovies = enrichedDf
      .groupBy("movieId")
      .agg(count("tag").as("tag_count"))
      .orderBy(col("tag_count").desc)
      .limit(10)

    mostTaggedMovies.show()
    mostTaggedMovies.coalesce(1).write.mode("overwrite").json("src/main/datasets/json_format/mostTaggedMovies")

    val mostCommonTags = enrichedDf
      .groupBy("tag")
      .agg(count("userId").as("tag_count"))
      .orderBy(col("tag_count").desc)
      .limit(10)

    mostCommonTags.show()
    mostCommonTags.coalesce(1).write.mode("overwrite").json("src/main/datasets/json_format/mostCommonTags")
  }
}
