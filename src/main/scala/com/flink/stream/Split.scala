package com.flink.stream

import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.scala._
//Split 就是将一个DataStream 分成两个或者多个DataStream
//Select 就是获取分流后对应的数据
object Split {
  def main(args: Array[String]): Unit = {

  val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
  //置为使用EventTime
  env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
  env.setParallelism(1)
  val elements: DataStream[Int] = env.fromElements(1, 2, 3, 4, 5, 6)
  //数据分流
  val split_data = elements.split(
    (num: Int) => (num % 2) match {
      case 0 => List("even")
      case 1 => List("odd")
    }
  )
  //获取分流后的数据
  val select: DataStream[Int] = split_data.select("even")
  select.print()
  env.execute()
  }
}
