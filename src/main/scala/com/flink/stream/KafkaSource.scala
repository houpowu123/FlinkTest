package com.flink.stream

import java.util.Properties

import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
import org.apache.flink.streaming.util.serialization.SimpleStringSchema

object KafkaSource {
  def main(args: Array[String]): Unit = {
    //1 指定kafka 数据流的相关信息
    val zkCluster = "bigdata001,bigdata002,bigdata003:2181"
    val kafkaCluster = "bigdata001:9092,bigdata001:9092,bigdata001:9092"
    val kafkaTopicName = "test"
    //2.创建流处理环境
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    //开启Checkpoint,每隔2s执行一次
    env.enableCheckpointing(2000)
    //3.创建kafka 数据流
    val properties = new Properties()
    properties.setProperty("bootstrap.servers", kafkaCluster)
    properties.setProperty("zookeeper.connect", zkCluster)
    properties.setProperty("group.id", kafkaTopicName)
    val kafkaSource = new FlinkKafkaConsumer[String](kafkaTopicName,
      new SimpleStringSchema(), properties)
    //4.添加数据源addSource(kafka)
    kafkaSource.setCommitOffsetsOnCheckpoints(true)//提交偏移量是给checkpoint一份
    kafkaSource.setStartFromGroupOffsets()//从记录开始处消费

    val text = env.addSource(kafkaSource).setParallelism(4)

  }
}
