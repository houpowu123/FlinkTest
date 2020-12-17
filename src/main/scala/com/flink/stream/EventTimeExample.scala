package com.flink.stream

import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.time.Time


//1527911168000,boos2,pc2,1600.0
object EventTimeExample {
  def main(args: Array[String]): Unit = {
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    val value: DataStream[String] = env.socketTextStream("localhost", 9000)
    value.map(line => {
      var data = line.split(",")
      SalePrice(data(0).toLong, data(1), data(2), data(3).toDouble)
    }) //提取消息中的事件时间
      .assignAscendingTimestamps(_.time)
      .keyBy(_.productName)
      .timeWindow(Time.seconds(3))
      .max(3)
      .print()

    //触发执行
    env.execute()
  }
}
case class SalePrice(time: Long, boosName: String, productName: String, price: Double)
