package com.flink.stream

import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.redis.RedisSink
import org.apache.flink.streaming.connectors.redis.common.config.{FlinkJedisClusterConfig, FlinkJedisPoolConfig}
import org.apache.flink.streaming.connectors.redis.common.mapper.{RedisCommand, RedisCommandDescription, RedisMapper}

object RedisSink {
  def main(args: Array[String]): Unit = {
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    val value: DataStream[String] = env.socketTextStream("localhost", 9000)
    val jedisconf = new FlinkJedisPoolConfig.Builder().setHost("10.202.253.13")
      .setPort(20729).build()
    //value.addSink(new RedisSink[SalePrice](jedisconf, new redisMapper))
    env.execute()
  }
}

class redisMapper extends RedisMapper[SalePrice] {
  //指定数据类型
  override def getCommandDescription: RedisCommandDescription = {
    new RedisCommandDescription(RedisCommand.HSET, "flinkTest")
  }
  //获取key
  override def getKeyFromData(data: SalePrice): String = {
    data.productName
  }

  //获取value
  override def getValueFromData(data: SalePrice): String = {
    data.price.toString
  }
}
