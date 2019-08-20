/**
  * Created by fucheng on 2019/7/9.
  */
object WriteWords {

  import java.io._

  import scala.io.Source

  def main(args: Array[String]): Unit = {
    val dataSource = Source.fromFile("D:\\data\\test.txt", "UTF-8")
    val data = dataSource.mkString

    val wordSource = Source.fromFile("D:\\data\\train_count.txt", "UTF-8")
    val wordIterator = wordSource.getLines

    var result = data

    for (wordLine <- wordIterator) {
      if(wordLine != ""){
        result = result.replaceAll(wordLine.slice(1, wordLine.length - 3), "/o  "+wordLine.slice(1, wordLine.length - 3) + "/" + wordLine.slice(wordLine.length - 2, wordLine.length - 1)+"  ")
        result = result.replaceAll("\n/o  ","\n")
      }

      println(wordLine)
      println("xx")
      println(result)
      println("xxxxxxxxxxxxxxxxxxxx")
    }

//    println(result)

    dataSource.close
    wordSource.close


    val writer = new PrintWriter(new File("D:\\data\\result.txt" ))

    writer.write(result)
    writer.close()
  }


}
