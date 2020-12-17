package com.flink.stream

import org.apache.flink.api.common.restartstrategy.RestartStrategies
import org.apache.flink.runtime.state.StateBackend
import org.apache.flink.runtime.state.filesystem.FsStateBackend
import org.apache.flink.streaming.api.{CheckpointingMode, TimeCharacteristic}
import org.apache.flink.streaming.api.environment.CheckpointConfig
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.time.Time


object Flink {
  def main(args: Array[String]): Unit = {
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    //置为使用EventTime
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
    //设置并行度
    env.setParallelism(1)
    //设置checkpoint间隔
    env.enableCheckpointing(1000)
    //设置Checkpoint-State的状态后端为FsStateBackend
    env.setStateBackend(new FsStateBackend("file:///D:/test/chkTest.txt",true))
    //设置两个checkpoint最少等待间隔
    env.getCheckpointConfig.setMinPauseBetweenCheckpoints(500)
    //设置如果在做Checkpoint过程中出现错误，是否让整体任务失败：true是false不是
    env.getCheckpointConfig.setFailOnCheckpointingErrors(false)
    //设置是否清理检查点,表示Cancel 时是否需要保留当前的Checkpoint，默认Checkpoint会在作业被Cancel时被删除
    //ExternalizedCheckpointCleanup.DELETE_ON_CANCELLATION：true,当作业被取消时，删除外部的checkpoint(默认值)
    //ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION：false,当作业被取消时，保留外部的checkpoint
    env.getCheckpointConfig.enableExternalizedCheckpoints(
      CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)


    //设置checkpoint的执行模式为EXACTLY_ONCE(默认),注意:得需要外部支持,如Source和Sink的支持//设置checkpoint的执行模式为EXACTLY_ONCE(默认),注意:得需要外部支持,如Source和Sink的支持
    env.getCheckpointConfig.setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE)
    //设置checkpoint的超时时间,如果Checkpoint在60s内尚未完成说明该次Checkpoint失败,则丢弃。
    env.getCheckpointConfig.setCheckpointTimeout(60000) //默认10分钟
    //设置同一时间有多少个checkpoint可以同时执行
    env.getCheckpointConfig.setMaxConcurrentCheckpoints(1) //默认为1
    //固定延迟重启策略: 程序出现异常的时候，重启2次，每次延迟3秒钟重启，超过2次，程序退出
    env.setRestartStrategy(RestartStrategies.fixedDelayRestart(2, 3000))

    val value: DataStream[String] = env.socketTextStream("localhost", 9000)

    value.flatMap(line => line.toLowerCase.split("\\W+"))
      .filter(line => line.nonEmpty)
      .map(x => (x,1))
      .keyBy(0)
      //.timeWindow(Time.seconds(3))  报错 因为是状态的 会统计累积的
      .sum(1)
      .print()
    env.execute("Streaming WordCount")
  }
}
