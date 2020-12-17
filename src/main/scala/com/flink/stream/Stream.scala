package com.flink.stream

import org.apache.flink.streaming.api.scala._
object Stream {
  def main(args: Array[String]): Unit = {
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    val value: DataStream[String] = env.socketTextStream("localhost", 9000)

    value.flatMap(line => line.toLowerCase.split("\\W+"))
      .filter(line => line.nonEmpty)
      .map(x => (x,1))
      .keyBy(0)
      .sum(1)
      .print()
    env.execute("Streaming WordCount")
  }
}
