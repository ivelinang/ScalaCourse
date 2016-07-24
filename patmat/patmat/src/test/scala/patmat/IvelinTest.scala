package patmat

/**
  * Created by Ivelin on 08/03/2016.
  */
object IvelinTest {
  def occurs(char:Char, count:List[(Char, Int)]):List[(Char,Int)]=count match{
    case List()=>
      List((char,1))
    case _ =>
      if (char==count.head._1) (char,count.head._2+1)::count.tail   //occurs(char,count.tail)
      else count.head::occurs(char, count.tail)
  }

  def main(args: Array[String]) {
    val res=occurs('a', List(('a',1),('b',1),('a',1)))
    println(res)
  }
}
