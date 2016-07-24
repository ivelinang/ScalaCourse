package recfun
import common._

object Main {
  def main(args: Array[String]) {
    println("Pascal's Triangle")
    for (row <- 0 to 10) {
      for (col <- 0 to row)
        print(pascal(col, row) + " ")
      println()
    }
  }

  /**
   * Exercise 1
   */
  def pascal(c: Int, r: Int): Int = {
    if (c==0)
      1
    else if (c>=r+1)
      0
    else
      pascal(c,r-1)+pascal(c-1,r-1)
  }

  /**
   * Exercise 2
   */
  def balance(chars: List[Char]): Boolean = {
    val inList=List('(',')')

    def filter(chars:List[Char]):List[Char]={
      (for (i<-chars.indices if inList.contains(chars(i))) yield chars(i)).toList
    }


    def loop(chars:List[Char], open:Int):Boolean={
      if (chars.isEmpty)
        open==0
      else
      if (chars.head=='(')
        loop(chars.tail, open+1)
      else
        open>0 && loop(chars.tail, open-1)
    }

    loop(filter(chars),0)
  }

  /**
   * Exercise 3
   */
  def countChange(money: Int, coins: List[Int]): Int = {
    if(money == 0)
      1
    else if(money > 0 && !coins.isEmpty)
      countChange(money - coins.head, coins) + countChange(money, coins.tail)
    else
      0
  }
}
