package com.flink.test

import java.util

import org.apache.flink.api.common.functions.RichMapFunction
import org.apache.flink.api.scala._
import org.apache.flink.configuration.Configuration

import scala.collection.mutable

object broadcast {
  def main(args: Array[String]): Unit = {
    val env = ExecutionEnvironment.getExecutionEnvironment
    val data2 = new mutable.MutableList[(Int, Long, String)]
    data2.+=((1, 1L, "xiaoming"))
    data2.+=((2, 2L, "xiaoli"))
    data2.+=((3, 2L, "xiaoqiang"))
    val ds1 = env.fromCollection(data2)

    val data3 = new mutable.MutableList[(Int, Long, Int, String, Long)]
    data3.+=((1, 1L, 0, "Hallo", 1L))
    data3.+=((2, 2L, 1, "Hallo Welt", 2L))
    data3.+=((2, 3L, 2, "Hallo Welt wie", 1L))
    val ds2 = env.fromCollection(data3)
    ds1.map(new RichMapFunction[(Int,Long,String),(Int,Long,String,String)] {
      var buffer: mutable.Buffer[(Int, Long, Int, String, Long)] = null
      //获取广播变量
      //open先于map之前执行
      import collection.JavaConverters._
      override def open(parameters: Configuration): Unit = {
        //获取广播变量
        val list: util.List[(Int, Long, Int, String, Long)] = getRuntimeContext.getBroadcastVariable[(Int, Long, Int, String, Long)]("ds2")
        buffer = list.asScala
      }
      override def map(in: (Int, Long, String)): (Int, Long, String, String) = {
        var value: (Int, Long, String, String) = null
      for (line <- buffer){
          if (in._2 == line._2){
            value = (in._1, in._2, in._3, line._4)
          }
      }
        value
      }
    }).withBroadcastSet(ds2,"ds2")
      .print()
  }
}
