/*
 * Copyright (c) 2017. Atguigu Inc. All Rights Reserved.
 * Date: 10/31/17 5:07 PM.
 * Author: wuyufei.
 */

package com.atguigu.commons.model

/**
  * Movie Class 电影类
  *
  * @param mid       电影的ID
  * @param name      电影的名称
  * @param descri    电影的描述
  * @param timelong  电影的时长
  * @param issue     电影的发行时间
  * @param shoot     电影的拍摄时间
  * @param language  电影的语言
  * @param genres    电影的类别
  * @param actors    电影的演员
  * @param directors 电影的导演
  */
case class Movie(val mid: Int, val name: String, val descri: String, val timelong: String, val issue: String,
                 val shoot: String, val language: String, val genres: String, val actors: String, val directors: String)

/**
  * MovieRating Class 电影的评分类
  *
  * @param uid       用户的ID
  * @param mid       电影的ID
  * @param score     用户为该电影的评分
  * @param timestamp 用户为该电影评分的时间
  */
case class MovieRating(val uid: Int, val mid: Int, val score: Double, val timestamp: Int)

/**
  * Tag Class  电影标签类
  *
  * @param uid       用户的ID
  * @param mid       电影的ID
  * @param tag       用户为该电影打的标签
  * @param timestamp 用户为该电影打标签的时间
  */
case class Tag(val uid: Int, val mid: Int, val tag: String, val timestamp: Int)

/**
  * MySQL的连接配置
  * @param url        URL
  * @param user       用户名
  * @param password   密码
  */
case class MySqlConfig(val url: String, val user:String, val password:String)

/**
  * ElasticSearch的连接配置
  *
  * @param httpHosts      Http的主机列表，以，分割
  * @param transportHosts Transport主机列表， 以，分割
  * @param index          需要操作的索引
  * @param clustername    ES集群的名称，
  */
case class ESConfig(val httpHosts: String, val transportHosts: String, val index: String, val clustername: String)

//推荐
case class Recommendation(rid: Int, r: Double){
  override def toString :String = {rid.toString + ":" + r.toString}
}

// 用户的推荐
case class UserRecs(uid: Int, recs: String)

//电影的相似度
case class MovieRecs(mid: Int, recs: String)

/**
  * 电影类别的推荐
  *
  * @param genres 电影的类别
  * @param recs   top10的电影的集合
  */
case class GenresRecommendation(genres: String, recs: String)
