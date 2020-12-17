package com.flink.test

import org.apache.flink.api.scala._
import org.apache.flink.util.Collector

//1.准备环境-env
//2.准备数据-source
//3.处理数据-transformation
//4.输出结果-sink
//5.触发执行-execute
object wordCount {
  def main(args: Array[String]): Unit = {
    //1.准备环境-env
    val env = ExecutionEnvironment.getExecutionEnvironment

    val value = env.fromCollection(List(1, 2, 10))
    value.print()
    println("==============")
    val dataSet: DataSet[String] = env.readTextFile("D:\\test\\flinkTest.txt")
    dataSet.flatMap(x => x.toLowerCase.split(","))
      .map(x => (x,1))
      .groupBy(0)
      .sum(1)
      .print()
    dataSet.flatMap(x => x.toLowerCase.split(","))
      .map(x => (x,1))
      .groupBy(0)
      //先拉取在聚合
      .reduce((x,y) => (x._1,x._2+y._2))
      .print()

    //是将一个dataSet或一组数据，最终聚合成一条或多条数据
    println("===========")
    dataSet.flatMap(x => x.toLowerCase.split(","))
      .map(x => (x,1))
      .groupBy(0)
      //提前聚合 在拉取
      .reduceGroup{
        (in: Iterator[(String, Int)], out: Collector[(String, Int)]) => {
          val tuple: (String, Int) = in.reduce((x, y) => (x._1, x._2 + y._2))
          out.collect(tuple)
        }
      }.print()

  }


}
