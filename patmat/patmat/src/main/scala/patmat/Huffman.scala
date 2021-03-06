package patmat

import common._
import patmat.Huffman.{Leaf, Fork}

/**
 * Assignment 4: Huffman coding
 *
 */
object Huffman {

  /**
    * A huffman code is represented by a binary tree.
    *
    * Every `Leaf` node of the tree represents one character of the alphabet that the tree can encode.
    * The weight of a `Leaf` is the frequency of appearance of the character.
    *
    * The branches of the huffman tree, the `Fork` nodes, represent a set containing all the characters
    * present in the leaves below it. The weight of a `Fork` node is the sum of the weights of these
    * leaves.
    */
  abstract class CodeTree

  case class Fork(left: CodeTree, right: CodeTree, chars: List[Char], weight: Int) extends CodeTree

  case class Leaf(char: Char, weight: Int) extends CodeTree


  // Part 1: Basics
  def weight(tree: CodeTree): Int = tree match {
    case Leaf(char, w) => w
    case Fork(left, right, chars, w) => weight(right) + weight(left)
  }

  def chars(tree: CodeTree): List[Char] = tree match {
    case Leaf(c, w) => List(c)
    case Fork(left, right, c, w) => c
  }

  def makeCodeTree(left: CodeTree, right: CodeTree) =
    Fork(left, right, chars(left) ::: chars(right), weight(left) + weight(right))


  // Part 2: Generating Huffman trees

  /**
    * In this assignment, we are working with lists of characters. This function allows
    * you to easily create a character list from a given string.
    */
  def string2Chars(str: String): List[Char] = str.toList

  /**
    * This function computes for each unique character in the list `chars` the number of
    * times it occurs. For example, the invocation
    *
    * times(List('a', 'b', 'a'))
    *
    * should return the following (the order of the resulting list is not important):
    *
    * List(('a', 2), ('b', 1))
    *
    * The type `List[(Char, Int)]` denotes a list of pairs, where each pair consists of a
    * character and an integer. Pairs can be constructed easily using parentheses:
    *
    * val pair: (Char, Int) = ('c', 1)
    *
    * In order to access the two elements of a pair, you can use the accessors `_1` and `_2`:
    *
    * val theChar = pair._1
    * val theInt  = pair._2
    *
    * Another way to deconstruct a pair is using pattern matching:
    *
    * pair match {
    * case (theChar, theInt) =>
    * println("character is: "+ theChar)
    * println("integer is  : "+ theInt)
    * }
    */
  def times(chars: List[Char]): List[(Char, Int)] = {
    def occurs(char: Char, count: List[(Char, Int)]): List[(Char, Int)] = count match {
      case List() =>
        List((char, 1))
      case _ =>
        if (char == count.head._1) (char, count.head._2 + 1) :: count.tail //occurs(char,count.tail)
        else count.head :: occurs(char, count.tail)
    }
    if (chars.isEmpty)
      List[(Char, Int)]()
    else
      occurs(chars.head, times(chars.tail))

  }

  /**
    * Returns a list of `Leaf` nodes for a given frequency table `freqs`.
    *
    * The returned list should be ordered by ascending weights (i.e. the
    * head of the list should have the smallest weight), where the weight
    * of a leaf is the frequency of the character.
    */
  def makeOrderedLeafList(freqs: List[(Char, Int)]): List[Leaf] = {
    def makeLeafs(freqs: List[(Char, Int)]): List[Leaf] = freqs match {
      case List() =>
        List[Leaf]()
      case _ =>
        Leaf(freqs.head._1, freqs.head._2) :: makeOrderedLeafList(freqs.tail)
    }
    makeLeafs(freqs.sortWith((a, b) => a._2 < b._2))
  }

  /**
    * Checks whether the list `trees` contains only one single code tree.
    */
  def singleton(trees: List[CodeTree]): Boolean = trees match {
    case x :: Nil =>
      true
    case _ =>
      false
  }

  /**
    * The parameter `trees` of this function is a list of code trees ordered
    * by ascending weights.
    *
    * This function takes the first two elements of the list `trees` and combines
    * them into a single `Fork` node. This node is then added back into the
    * remaining elements of `trees` at a position such that the ordering by weights
    * is preserved.
    *
    * If `trees` is a list of less than two elements, that list should be returned
    * unchanged.
    */
  def combine(trees: List[CodeTree]): List[CodeTree] = trees match {
    case List() =>
      List[CodeTree]()
    case x :: Nil =>
      List(x)
    case _ =>
      val leaf1 = trees.head
      val leaf2 = trees.tail.head
      val comFork = Fork(leaf1, leaf2, chars(leaf1) ::: chars(leaf2), weight(leaf1) + weight(leaf2))
      (comFork :: combine(trees.tail.tail)).sortWith((a, b) => weight(a) < weight(b))
  }

  /**
    * This function will be called in the following way:
    *
    * until(singleton, combine)(trees)
    *
    * where `trees` is of type `List[CodeTree]`, `singleton` and `combine` refer to
    * the two functions defined above.
    *
    * In such an invocation, `until` should call the two functions until the list of
    * code trees contains only one single tree, and then return that singleton list.
    *
    * Hint: before writing the implementation,
    * - start by defining the parameter types such that the above example invocation
    * is valid. The parameter types of `until` should match the argument types of
    * the example invocation. Also define the return type of the `until` function.
    * - try to find sensible parameter names for `xxx`, `yyy` and `zzz`.
    */
  def until(f: List[CodeTree] => Boolean, g: List[CodeTree] => List[CodeTree])(trees: List[CodeTree]): CodeTree = {
    if (f(trees)) trees.head
    else until(f, g)(g(trees))
  }

  /**
    * This function creates a code tree which is optimal to encode the text `chars`.
    *
    * The parameter `chars` is an arbitrary text. This function extracts the character
    * frequencies from that text and creates a code tree based on them.
    */
  def createCodeTree(chars: List[Char]): CodeTree = {
    until(singleton, combine)(makeOrderedLeafList(times(chars)))
  }


  // Part 3: Decoding

  type Bit = Int

  /**
    * This function decodes the bit sequence `bits` using the code tree `tree` and returns
    * the resulting list of characters.
    */
  def decode(tree: CodeTree, bits: List[Bit]): List[Char] = {
    def help(tree: CodeTree, bits: List[Bit]): (List[Char], List[Bit]) = {
      if (bits.head == 0)
        tree match {
          case Leaf(char, weight) => throw new Error("error 0 leaf")
          case Fork(left, right, char, weight) => left match {
            case Leaf(ch, weight) => (List(ch), bits.tail)
            case Fork(l, r, char, weight) => help(left, bits.tail)
          }
        }
      else
        tree match {
          case Leaf(char, weight) => throw new Error("wrong 1 Tree/Bits")
          case Fork(left, right, char, weight) => right match {
            case Leaf(ch, w) => (List(ch), bits.tail)
            case Fork(l, r, char, weight) => help(right, bits.tail)
          }
        }
    }
    def codeUntil(tree: CodeTree, bits: List[Bit], word: List[Char]): List[Char] = {
      if (bits.isEmpty) word
      else codeUntil(tree, help(tree, bits)._2, (help(tree, bits)._1 ::: word.reverse).reverse)
    }
    codeUntil(tree, bits, List())
  }

  /**
    * A Huffman coding tree for the French language.
    * Generated from the data given at
    * http://fr.wikipedia.org/wiki/Fr%C3%A9quence_d%27apparition_des_lettres_en_fran%C3%A7ais
    */
  val frenchCode: CodeTree = Fork(Fork(Fork(Leaf('s', 121895), Fork(Leaf('d', 56269), Fork(Fork(Fork(Leaf('x', 5928), Leaf('j', 8351), List('x', 'j'), 14279), Leaf('f', 16351), List('x', 'j', 'f'), 30630), Fork(Fork(Fork(Fork(Leaf('z', 2093), Fork(Leaf('k', 745), Leaf('w', 1747), List('k', 'w'), 2492), List('z', 'k', 'w'), 4585), Leaf('y', 4725), List('z', 'k', 'w', 'y'), 9310), Leaf('h', 11298), List('z', 'k', 'w', 'y', 'h'), 20608), Leaf('q', 20889), List('z', 'k', 'w', 'y', 'h', 'q'), 41497), List('x', 'j', 'f', 'z', 'k', 'w', 'y', 'h', 'q'), 72127), List('d', 'x', 'j', 'f', 'z', 'k', 'w', 'y', 'h', 'q'), 128396), List('s', 'd', 'x', 'j', 'f', 'z', 'k', 'w', 'y', 'h', 'q'), 250291), Fork(Fork(Leaf('o', 82762), Leaf('l', 83668), List('o', 'l'), 166430), Fork(Fork(Leaf('m', 45521), Leaf('p', 46335), List('m', 'p'), 91856), Leaf('u', 96785), List('m', 'p', 'u'), 188641), List('o', 'l', 'm', 'p', 'u'), 355071), List('s', 'd', 'x', 'j', 'f', 'z', 'k', 'w', 'y', 'h', 'q', 'o', 'l', 'm', 'p', 'u'), 605362), Fork(Fork(Fork(Leaf('r', 100500), Fork(Leaf('c', 50003), Fork(Leaf('v', 24975), Fork(Leaf('g', 13288), Leaf('b', 13822), List('g', 'b'), 27110), List('v', 'g', 'b'), 52085), List('c', 'v', 'g', 'b'), 102088), List('r', 'c', 'v', 'g', 'b'), 202588), Fork(Leaf('n', 108812), Leaf('t', 111103), List('n', 't'), 219915), List('r', 'c', 'v', 'g', 'b', 'n', 't'), 422503), Fork(Leaf('e', 225947), Fork(Leaf('i', 115465), Leaf('a', 117110), List('i', 'a'), 232575), List('e', 'i', 'a'), 458522), List('r', 'c', 'v', 'g', 'b', 'n', 't', 'e', 'i', 'a'), 881025), List('s', 'd', 'x', 'j', 'f', 'z', 'k', 'w', 'y', 'h', 'q', 'o', 'l', 'm', 'p', 'u', 'r', 'c', 'v', 'g', 'b', 'n', 't', 'e', 'i', 'a'), 1486387)

  /**
    * What does the secret message say? Can you decode it?
    * For the decoding use the frenchCode' Huffman tree defined above.
    */
  val secret: List[Bit] = List(0, 0, 1, 1, 1, 0, 1, 0, 1, 1, 1, 0, 0, 1, 1, 0, 1, 0, 0, 1, 1, 0, 1, 0, 1, 1, 0, 0, 1, 1, 1, 1, 1, 0, 1, 0, 1, 1, 0, 0, 0, 0, 1, 0, 1, 1, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 1)

  /**
    * Write a function that returns the decoded secret
    */
  def decodedSecret: List[Char] = decode(frenchCode, secret)


  // Part 4a: Encoding using Huffman tree

  /**
    * This function encodes `text` using the code tree `tree`
    * into a sequence of bits.
    */
  def encode(tree: CodeTree)(text: List[Char]): List[Bit] = {
    def help(tree: CodeTree, text: Char, code: List[Bit]): List[Bit] = tree match {
      case Leaf(char, weight) => if (char == text) code else code //a bit redundant I know
      case Fork(left, right, char, weight) =>
        left match {
          case Leaf(ch, weight) =>
            if (ch == text) code:::(List(0):::Nil)
            else help(right, text, code ::: (List(1) ::: Nil))
          case Fork(l, r, char, weight) =>
            if (chars(left).contains(text)) help(left, text, code ::: (List(0) ::: Nil))
            else help(right, text, code ::: (List(1) ::: Nil))
        }

    }
    def untilEncode(tree: CodeTree, text: List[Char], code: List[Bit]): List[Bit] = {
      if (text.isEmpty) code
      else untilEncode(tree, text.tail, help(tree, text.head, code))
    }
    untilEncode(tree, text, List())
  }


  // Part 4b: Encoding using code table

  type CodeTable = List[(Char, List[Bit])]

  /**
    * This function returns the bit sequence that represents the character `char` in
    * the code table `table`.
    */
  def codeBits(table: CodeTable)(char: Char): List[Bit] =  {
    if (table.head._1==char) table.head._2
    else codeBits(table.tail)(char)
  }

  /**
    * Given a code tree, create a code table which contains, for every character in the
    * code tree, the sequence of bits representing that character.
    *
    * Hint: think of a recursive solution: every sub-tree of the code tree `tree` is itself
    * a valid code tree that can be represented as a code table. Using the code tables of the
    * sub-trees, think of how to build the code table for the entire tree.
    */
  def convert(tree: CodeTree): CodeTable = {
    def help(text:Char, tree: CodeTree):CodeTable={
      List((text, encode(tree)(List(text))))
    }

    def merge(tree:CodeTree, table:CodeTable, lschars:List[Char]):CodeTable=  tree match{
      case Leaf(char, w)=>
        help(char, tree)
      case Fork(left, right, chars, w)=>
        if (lschars.isEmpty)
          table
        else
          mergeCodeTables(table, merge(tree, help(lschars.head, tree), lschars.tail))
    }

    merge(tree, List[(Char, List[Bit])](), chars(tree))
  }

  def convert2(tree: CodeTree):CodeTable={

    def help(tree:CodeTree, char:Char):CodeTable=tree match{
      case Fork(left, right, chrs, weight)=>
        if (chars(left).contains(char)) List((char, List(0)))
        else List((char, List(1)))
    }

    def subtree(tree:CodeTree, chars:List[Char], table:CodeTable):CodeTable=tree match{
      case Leaf(char, weight)=> table
      case Fork(left, right, chrs, weight)=>
        if (chars.isEmpty) table
        else subtree(tree,chars.tail, help(tree, chars.head):::table)
    }

    def createTable(tree:CodeTree, table:CodeTable):CodeTable=tree match {
      case Leaf(char, weight)=> List( (char, List()) )
      case Fork(left, right, chrs, weight)=> mergeCodeTables(mergeCodeTables(createTable(left, subtree(tree, chars(tree), table)),
        createTable(right, subtree(tree, chars(tree), table))), subtree(tree, chars(tree), table))
    }

    createTable(tree,List[(Char, List[Bit])]() )
  }

  /**
    * This function takes two code tables and merges them into one. Depending on how you
    * use it in the `convert` method above, this merge method might also do some transformations
    * on the two parameter code tables.
    */
  def mergeCodeTables(a: CodeTable, b: CodeTable): CodeTable = {
    def help(a:CodeTable, b: CodeTable):CodeTable={
      if (b.isEmpty) a
      else
        if ((for (x<-a) yield x._1).contains(b.head._1))
          help(merge(a, b.head, List[(Char, List[Bit])]()),b.tail)
        else
          help(a:::b.head::Nil,b.tail)
    }

    def merge(a:CodeTable,elem:(Char, List[Bit]), b:CodeTable):CodeTable={
      if (a.isEmpty) b
      else
        if (a.head._1==elem._1) merge(a.tail,elem, (a.head._1,a.head._2:::elem._2)::b)
        else merge(a.tail, elem, b:::a.head::Nil)
    }

    help(a,b)
  }

  /**
    * This function encodes `text` according to the code tree `tree`.
    *
    * To speed up the encoding process, it first converts the code tree to a code table
    * and then uses it to perform the actual encoding.
    */
  def quickEncode(tree: CodeTree)(text: List[Char]): List[Bit] = {
    val table =convert2(tree) //it is more efficient if calculated just once

    def help(text:List[Char], code:List[Bit]):List[Bit]={
      if (text.isEmpty) code
      else help(text.tail, code:::codeBits(table)(text.head))
    }

    help(text, List[Bit]())
  }
}

object Main{
  def main(args: Array[String]) {
    val tree=Huffman.makeCodeTree(Huffman.makeCodeTree(Huffman.Leaf('x', 1), Huffman.Leaf('e', 1)),
      Huffman.Leaf('t', 2)
    )
    println(tree.chars)
    println(tree.weight)

    val lst=Huffman.times(List('a', 'b', 'a', 'c','c','c'))
    println(lst)

    val orderL=Huffman.makeOrderedLeafList(lst)
    println(orderL)

    val fork=Huffman.combine(orderL)
    println(fork)

    val untilF=Huffman.until(Huffman.singleton,Huffman.combine)(orderL)
    println(untilF)

    val creatC=Huffman.createCodeTree(List('a', 'b', 'a', 'c','c','c'))
    println(creatC)

    val frenchCode: Huffman.CodeTree = Fork(Leaf('a',1), Fork(Leaf('b',1),Fork(Leaf('c',1),Leaf('d',1),List('c','d'),2),List('b','c','d'),3),List('a','b','c','d'),4)
    /**
      * What does the secret message say? Can you decode it?
      * For the decoding use the frenchCode' Huffman tree defined above.
      */
    type Bit = Int
    val secret: List[Bit] = List(1,0,0,0,1,0,1,0)
    val decodedS=Huffman.decode(frenchCode, secret)
    println(decodedS)

    val trylst=List(1,2,3):::(List(4):::Nil)
    println(trylst)

    val Chars=List('b','a','a','b','b')
    val encodeS=Huffman.encode(frenchCode)(Chars)
    println(encodeS)

    val frenchCode2: Huffman.CodeTree = Fork(Leaf('a',1), Fork(Fork(Leaf('b',1),Fork(Leaf('c',1),Leaf('d',1),List('c','d'),2),List('b','c','d'),3),Fork(Leaf('e',1), Leaf('f',1), List('e','f'), 2),List('b','c','d','e','f'),5),List('a','b','c','d','e','f'),6)
    val charEn=List('b','a','c','e','f')
    val encodeS2=Huffman.encode(frenchCode2)(charEn)
    println(encodeS2)

    val mergeTable=Huffman.convert2(frenchCode2)
    println(mergeTable)

    val mergeTable2=Huffman.convert(frenchCode2)
    println(mergeTable2)

    val mergEx=List(('g',List(1,0)))
    val tryMerge=Huffman.mergeCodeTables(mergeTable, mergEx)
    println(tryMerge)

    val quickEn=Huffman.quickEncode(frenchCode2)(charEn)
    println(quickEn)

    val secretT=Huffman.decodedSecret
    println(secretT)

  }


}