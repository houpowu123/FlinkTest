package com.flink.test

import org.apache.flink.api.common.functions.RichMapFunction
import org.apache.flink.api.scala._
import org.apache.flink.configuration.Configuration

object rebalance {
  def main(args: Array[String]): Unit = {
    val env = ExecutionEnvironment.getExecutionEnvironment
    val value = env.generateSequence(0, 100)
    val rebalanceData = value.filter(x => x > 50)
      //类似于spark的repartiton
      .rebalance()
    rebalanceData.map(new RichMapFunction[Long,(Int,Long)] {
      var taskId = 0
      override def open(parameters: Configuration): Unit ={
        taskId = getRuntimeContext.getIndexOfThisSubtask
      }


      override def map(in: Long): (Int, Long) = {
        (taskId,in)
      }
    }).print()

  }
}
