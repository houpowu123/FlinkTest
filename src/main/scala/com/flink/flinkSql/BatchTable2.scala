package com.flink.flinkSql

import org.apache.flink.api.scala._
import org.apache.flink.api.scala.typeutils.Types
import org.apache.flink.core.fs.FileSystem.WriteMode
import org.apache.flink.table.api.Table
import org.apache.flink.table.api.scala._
import org.apache.flink.table.sources.CsvTableSource

object BatchTable2 {
  def main(args: Array[String]): Unit = {
    val fbEnv = ExecutionEnvironment.getExecutionEnvironment
    val fbTableEnv = BatchTableEnvironment.create(fbEnv)
    val student = fbEnv.readTextFile("D:\\test\\sql_students.txt")
    val score = fbEnv.readTextFile("D:\\test\\sql_scores.txt")
    //获取数据源
    val studentSource = CsvTableSource.builder()
      .path("D:\\test\\sql_students.txt")
      .field("id", Types.INT) //第一列数据
      .field("name", Types.STRING) //第二列数据
      .field("classname", Types.STRING) //第三列数据
      .fieldDelimiter(",") //列分隔符，默认是"，"
      .lineDelimiter("\n") //换行符
      //.ignoreFirstLine() //忽略第一行
      .ignoreParseErrors() //忽略解析错误
      .build()
    val scoreSource: CsvTableSource = CsvTableSource.builder()
      .path("D:\\test\\sql_scores.txt")
      .field("id", Types.INT) //第一列数据
      .field("chinese", Types.INT) //第二列数据
      .field("math", Types.INT) //第三列数据
      .field("english", Types.INT) //第四列数据
      .fieldDelimiter(",") //列分隔符，默认是"，"
      .lineDelimiter("\n") //换行符
      //.ignoreFirstLine() //忽略第一行
      .ignoreParseErrors() //忽略解析错误
      .build()
    //注册为表

    fbTableEnv.registerTableSource("t_student", studentSource)
    fbTableEnv.registerTableSource("t_score", scoreSource)
    val table = fbTableEnv.sqlQuery("select name,classname,chinese,math,english from t_student t1 join t_score t2 on t1.id = t2.id" )
    fbTableEnv.registerTable("t_student_score",table)

    val resultTable: Table = fbTableEnv.sqlQuery(
      """
        |select
        |classname,
        |avg(chinese) as chinese,
        |avg(math) as math,
        |avg(english) as english,
        |avg(chinese+math+english) as total
        |from t_student_score
        |group by classname
        |order by chinese
        |""".stripMargin)
    val fbTable: DataSet[Info] = fbTableEnv.toDataSet[Info](resultTable)
    fbTable.writeAsText("D:\\test\\result1.txt", WriteMode.OVERWRITE).setParallelism(1)
    fbEnv.execute("Flinksql")
  }
}
