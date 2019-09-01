/*
 * Copyright (c) 2017. Atguigu Inc. All Rights Reserved.
 * Date: 10/26/17 11:24 PM.
 * Author: wuyufei.
 */

package com.atguigu.commons.constant

/**
 * 常量接口
 * @author wuyufei
 *
 */
object Constants {
	//****************  属性信息  *******************
	val POOL_SIZE = "pool.size"
	val JDBC_URL = "jdbc.url"
	val JDBC_USER = "jdbc.user"
	val JDBC_PASSWORD = "jdbc.password"

	val ES_HTTPHOSTS = "es.httpHosts"
	val ES_TRANSPORTHOSTS = "es.transportHosts"
	val ES_CLUSTER_NAME = "es.cluster.name"
	val ES_INDEX_NAME = "es.index.name"

	val REDIS_HOST = "redis.host"
	val REDIS_PORT = "redis.port"

	val KAFKA_BROKERS = "kafka.brokers"
	val KAFKA_ZOOKEEPER = "kafka.zookeeper"
	val KAFKA_FROM_TOPIC = "kafka.from.topic"
	val KAFKA_TO_TOPIC = "kafka.to.topic"


	//*****************  MySQL中的表名  ***************

	//电影表名
	val DB_MOVIE = "Movie"

	//电影评分的表名
	val DB_RATING = "MovieRating"

	//电影标签的表名
	val DB_TAG = "Tag"

	//用户表
	val DB_USER = "User"

	//电影的平均评分表
	val DB_AVERAGE_MOVIES = "AverageMovies"

	//电影类别TOp10表
	val DB_GENRES_TOP_MOVIES = "GenresTopMovies"

	//优质电影表
	val DB_RATE_MORE_MOVIES = "RateMoreMovies"

	//最热电影表
	val DB_RATE_MORE_RECENTLY_MOVIES = "RateMoreRecentlyMovies"

	//用户的推荐矩阵
	val DB_USER_RECS= "UserRecs"

	//电影的相似度矩阵
	val DB_MOVIE_RECS = "MovieRecs"

	//实时推荐电影表
	val DB_STREAM_RECS = "StreamRecs"


	//***************  ES ******************

	//使用的index
	val ES_INDEX = "recommender"

	//使用的Type
	val ES_TYPE = "Movie"

	//**************  Redis ****************
	val USER_RATING_QUEUE_SIZE = 20

	//**************  LOG ****************

	val USER_RATING_LOG_PREFIX = "USER_RATING_LOG_PREFIX:"

	//**************** Driver Class ***************

	val ES_DRIVER_CLASS = "org.elasticsearch.spark.sql"

	val MAX_RATING = 5.0F

	val MAX_RECOMMENDATIONS = 50
	
}
