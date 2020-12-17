package com.flink.test

import org.apache.flink.api.java.aggregation.Aggregations
import org.apache.flink.api.scala._
import redis.clients.jedis.ZParams.Aggregate

import scala.collection.mutable

object aggreate {
  def main(args: Array[String]): Unit = {
    val env = ExecutionEnvironment.getExecutionEnvironment
    val data = new mutable.MutableList[(Int, String, Double)]
    data.+=((1, "yuwen", 90.0))
    data.+=((2, "shuxue", 20.0))
    data.+=((3, "yingyu", 30.0))
    data.+=((4, "wuli", 40.0))
    data.+=((5, "yuwen", 50.0))
    data.+=((6, "wuli", 60.0))
    data.+=((7, "yuwen", 70.0))
    val value = env.fromCollection(data)
    value.groupBy(1)
      //.minBy(2)//一行数据  3, "yingyu", 30.0
      //.min(2)
      .aggregate(Aggregations.MAX,2)
      .map(x => x._3)
      .print()

    value.distinct(1).print()

  }
}
