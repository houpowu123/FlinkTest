package com.flink.stream

import java.lang
import java.text.SimpleDateFormat

import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.{AssignerWithPeriodicWatermarks, AssignerWithPunctuatedWatermarks}
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.watermark.Watermark
import org.apache.flink.streaming.api.windowing.time.Time


object WaterMark {
  def main(args: Array[String]): Unit = {
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    //置为使用EventTime
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
    val value: DataStream[String] = env.socketTextStream("localhost", 9000)
    val dst1: DataStream[SalePrice] = value.map(line => {
      val data = line.split(",")
      SalePrice(data(0).toLong, data(1), data(2), data(3).toDouble)
    })
    //todo 水印时间assignTimestampsAndWatermarks
    val timestamps_data = dst1.assignTimestampsAndWatermarks(new AssignerWithPeriodicWatermarks[SalePrice] {
      var currentMaxTimestamp: Long = 0
      val maxOutOfOrderness = 2000L //最大允许的乱序时间是2s
      var wm: Watermark = null
      val format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")

      //后执行 更新水位线
      override def getCurrentWatermark: Watermark = {
        val wm = new Watermark(currentMaxTimestamp - maxOutOfOrderness)
        wm
      }

      //先执行 获取事件时间
      override def extractTimestamp(t: SalePrice, l: Long): Long = {
        val timestamp = t.time
        currentMaxTimestamp = Math.max(timestamp, currentMaxTimestamp)
        currentMaxTimestamp
      }
    })
    timestamps_data.keyBy(_.productName)
      .timeWindow(Time.seconds(3))
      .maxBy(3)
      //.apply(new windowFun)
      .print()

  }
}
//case class SalePrice(time: Long, boosName: String, productName: String, price: Double)

//class windowFun extends WindowFunction[SalePrice, SalePrice, String, TimeWindow] {
//  override def apply(key: String, w: TimeWindow, iterable: lang.Iterable[SalePrice], collector: Collector[SalePrice]): Unit = {
//
//  }
//}