package com.flink.flinkSql

import java.text.SimpleDateFormat

import com.flink.stream.SalePrice
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.AssignerWithPeriodicWatermarks
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.watermark.Watermark
import org.apache.flink.table.api.{Table, Tumble}
import org.apache.flink.table.api.scala.StreamTableEnvironment
import org.apache.flink.types.Row

object FlinkTable {
  def main(args: Array[String]): Unit = {
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    val senv = StreamTableEnvironment.create(env)
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)//使用事件时间

    val value: DataStream[String] = env.socketTextStream("localhost", 9000)

    val dst1: DataStream[SalePrice] = value.map(line => {
      val data = line.split(",")
      SalePrice(data(0).toLong, data(1), data(2), data(3).toDouble)
    })
    //todo 水印时间assignTimestampsAndWatermarks
    val timestamps_data: DataStream[SalePrice] = dst1.assignTimestampsAndWatermarks(new AssignerWithPeriodicWatermarks[SalePrice] {
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
    senv.createTemporaryView[SalePrice]("test",timestamps_data)
    // tumble 窗口
    /**
     *  Tumble，滚动窗口，窗口数据有固定的大小，窗口数据无叠加；
     *  Hop，滑动窗口，窗口数据有固定大小，并且有固定的窗口重建频率，窗口数据有叠加；
     *  Session，会话窗口，根据窗口数据活跃程度划分窗口，窗口数据无叠加。
     * SELECT
     * [gk],
     * [TUMBLE_START(timeCol, size)],
     * [TUMBLE_END(timeCol, size)],
     * agg1(col1),
     * ...
     * aggn(colN)
     * FROM Tab1
     * GROUP BY [gk], TUMBLE(timeCol, size)
     */
    val table: Table = senv.sqlQuery(
      """
        |select
        |max(price) as price
        |from test
        |group by TUMBLE(time,INTERVAL ‘5’ second),productName
        |""".stripMargin)

    //打印schema
    senv.from("test").printSchema()


    //转换为dataStream
    //toAppendStream → 将计算后的数据append到结果DataStream中去
    //toRetractStream → 将计算后的新的数据在DataStream原数据的基础上更新true或是删除false



    val valueStream: DataStream[(Boolean, Row)] = senv.toRetractStream[Row](table)
    valueStream.print()
    senv.execute("StreamSql")
  }
}
