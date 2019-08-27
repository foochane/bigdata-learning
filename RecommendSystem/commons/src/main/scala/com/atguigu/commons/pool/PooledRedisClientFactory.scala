/*
 * Copyright (c) 2017. Atguigu Inc. All Rights Reserved.
 * Date: 10/31/17 2:46 PM.
 * Author: wuyufei.
 */

package com.atguigu.commons.pool

import com.atguigu.commons.conf.ConfigurationManager
import com.atguigu.commons.constant.Constants
import org.apache.commons.pool2.impl.{DefaultPooledObject, GenericObjectPool, GenericObjectPoolConfig}
import org.apache.commons.pool2.{BasePooledObjectFactory, PooledObject}
import redis.clients.jedis.Jedis

/**
  * Created by wuyufei on 06/09/2017.
  */


/**
  * 继承一个基础的连接池，需要提供池化的对象类型
  *
  */
class PooledRedisClientFactory(redisHost: String, redisPort: Int = 6379) extends BasePooledObjectFactory[Jedis] with Serializable {

  // 用于池来创建对象
  override def create(): Jedis = new Jedis(redisHost, redisPort)

  // 用于池来包装对象
  override def wrap(obj: Jedis): PooledObject[Jedis] = new DefaultPooledObject(obj)

  // 用于池来销毁对象
  override def destroyObject(p: PooledObject[Jedis]): Unit = {
    p.getObject.close()
    super.destroyObject(p)
  }

}

/**
  * 创建Redis池工具类
  */
object CreateRedisPool {

  private var genericObjectPool: GenericObjectPool[Jedis] = null

  // 用于返回真正的对象池GenericObjectPool
  def apply(): GenericObjectPool[Jedis] = {
    if (this.genericObjectPool == null) {
      this.synchronized {
        val redisHost = ConfigurationManager.config.getString(Constants.REDIS_HOST)
        val redisPort = ConfigurationManager.config.getString(Constants.REDIS_PORT).toInt
        val size = ConfigurationManager.config.getInt(Constants.POOL_SIZE)

        val pooledFactory = new PooledRedisClientFactory(redisHost, redisPort)
        val poolConfig = {
          val c = new GenericObjectPoolConfig
          c.setMaxTotal(size)
          c.setMaxIdle(size)
          c
        }
        //返回一个对象池
        this.genericObjectPool = new GenericObjectPool[Jedis](pooledFactory, poolConfig)
      }
    }
    genericObjectPool
  }
}

