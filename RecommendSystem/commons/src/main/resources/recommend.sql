/*
Navicat MySQL Data Transfer

Source Server         : localhost
Source Server Version : 50638
Source Host           : localhost:3306
Source Database       : recommend

Target Server Type    : MYSQL
Target Server Version : 50638
File Encoding         : 65001

Date: 2017-11-23 09:08:49
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for averagemovies
-- ----------------------------
DROP TABLE IF EXISTS `AverageMovies`;
CREATE TABLE `AverageMovies` (
  `mid` int(11) DEFAULT NULL,
  `avg` double DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for genrestopmovies
-- ----------------------------
DROP TABLE IF EXISTS `GenresTopMovies`;
CREATE TABLE `GenresTopMovies` (
  `genres` text,
  `recs` text
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for movie
-- ----------------------------
DROP TABLE IF EXISTS `Movie`;
CREATE TABLE `Movie` (
  `mid` int(11) DEFAULT NULL,
  `name` text,
  `descri` text,
  `timelong` text,
  `issue` text,
  `shoot` text,
  `language` text,
  `genres` text,
  `actors` text,
  `directors` text
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for movierating
-- ----------------------------
DROP TABLE IF EXISTS `MovieRating`;
CREATE TABLE `MovieRating` (
  `uid` int(11) DEFAULT NULL,
  `mid` int(11) DEFAULT NULL,
  `score` double DEFAULT NULL,
  `timestamp` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for movierecs
-- ----------------------------
DROP TABLE IF EXISTS `MovieRecs`;
CREATE TABLE `MovieRecs` (
  `mid` int(11) DEFAULT NULL,
  `recs` text
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for ratemoremovies
-- ----------------------------
DROP TABLE IF EXISTS `RateMoreMovies`;
CREATE TABLE `RateMoreMovies` (
  `mid` int(11) DEFAULT NULL,
  `count` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for ratemorerecentlymovies
-- ----------------------------
DROP TABLE IF EXISTS `RateMoreRecentlyMovies`;
CREATE TABLE `RateMoreRecentlyMovies` (
  `mid` int(11) DEFAULT NULL,
  `count` bigint(20) NOT NULL,
  `yeahmonth` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for streamrecs
-- ----------------------------
DROP TABLE IF EXISTS `StreamRecs`;
CREATE TABLE `StreamRecs` (
  `uid` int(11) DEFAULT NULL,
  `recs` text
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for tag
-- ----------------------------
DROP TABLE IF EXISTS `Tag`;
CREATE TABLE `Tag` (
  `uid` int(11) DEFAULT NULL,
  `mid` int(11) DEFAULT NULL,
  `tag` text,
  `timestamp` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `User`;
CREATE TABLE `User` (
  `uid` int(11) NOT NULL,
  `username` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `first` bit(1) DEFAULT NULL,
  `timestamp` int(11) DEFAULT NULL,
  `prefgenres` text,
  PRIMARY KEY (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for userrecs
-- ----------------------------
DROP TABLE IF EXISTS `UserRecs`;
CREATE TABLE `UserRecs` (
  `uid` int(11) DEFAULT NULL,
  `recs` text
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
