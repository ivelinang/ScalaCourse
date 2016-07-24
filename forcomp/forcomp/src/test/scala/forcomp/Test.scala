package forcomp

import Anagrams._
/**
  * Created by Ivelin on 27/03/2016.
  */
object Test extends App{
  val wordlist=Anagrams.dictionary
  println(wordlist(0))
  val occ=Anagrams.wordOccurrences(wordlist(0))
  println(occ)
  val all=Anagrams.sentenceOccurrences(wordlist)
  println(all)
  //println(Anagrams.dictionaryByOccurrences)
  val wordlist2=List("eat", "ate", "tea")
  val tryw=wordlist2.groupBy(x=>wordOccurrences(x))//.map(a=>a._1->a._2._2)
  println(tryw)
  val occur=wordOccurrences(wordlist2(0))
  println(combinations(occur))
  val comblist=List(('a', 2), ('b', 2))
  println(combinations(comblist))
  println(subtract(occur,occur))
  val x = List(('a', 1), ('d', 1), ('l', 1), ('r', 1))
  val y = List(('r', 1))
  println(subtract(x,y))

  val sentence=List( "man")
  println(sentenceAnagrams(sentence))



}
