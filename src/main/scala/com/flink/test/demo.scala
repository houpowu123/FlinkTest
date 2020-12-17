package com.flink.test
import org.apache.flink.api.scala._
object demo {

  def main(args: Array[String]): Unit = {
    val env: ExecutionEnvironment = ExecutionEnvironment.getExecutionEnvironment
    val source: DataSet[(String, Int)] = env.fromElements(("java", 1), ("scala", 1), ("java", 1)   )
    //3.数据转换mapPartition
    val values: DataSet[(String, Int)] = source.mapPartition(line => {
      line.map(x => (x._1, x._2))
    })
    //4.打印，触发执行
    values.print()

    val value = env.fromElements("java", "scala", "python")
    value.filter(v => v.contains("scala"))
        .print()

  }

}
