package com.flink.stream

import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.time.Time

object Window {
  def main(args: Array[String]): Unit = {
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    val value: DataStream[String] = env.socketTextStream("localhost", 9000)
    value.map(line =>{
      Car(line.split(",")(0).toInt,line.split(",")(1).toInt)
    }).keyBy(0)
      //时间窗口无重叠的时间窗口
      //.timeWindow(Time.seconds(3))
      //第一个参数为窗口大小 第二个参数为移动间隔
      //.timeWindow(Time.seconds(6),Time.seconds(3))
      //.countWindow(3)
      .countWindow(6,3)
      .sum(1)
      .print()
    env.execute("window")
  }
}
//样例类
case class Car(id: Int, count: Int)
