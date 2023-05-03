package Cassino

import scala.collection.mutable.Buffer

class Card(val cardID: Int) {

  val suit: Int = cardID / 13       // 0 for Clubs, 1 for Diamonds, 2 for Hearts and 3 for Spades
  private var selected: Boolean = false

  val handValue: Int =
    if (cardID + 1) % 13 == 0 then  // Aces
      14
    else if cardID == 21 then       // 10 of Diamonds
      16
    else if cardID == 39 then       // 2 of Spades
      15
    else
      cardID % 13 + 2

  val tableValue: Int =
    if (cardID + 1) % 13 == 0 then  // Aces
      1
    else
      cardID % 13 + 2
      
  def select(): Unit = selected = true
  
  def unselect(): Unit = selected = false
  
  def returnSelected(): Boolean = selected

  override def toString: String =
    var ret = Buffer[String]()
    this.tableValue match
      case 1  => ret += "Ace of"
      case 11 => ret += "Jack of"
      case 12 => ret += "Queen of"
      case 13 => ret += "King of"
      case _  => ret += this.tableValue.toString; ret += "of"
    this.suit match
      case 0  =>  ret += "Clubs"
      case 1  =>  ret += "Diamonds"
      case 2  =>  ret += "Hearts"
      case 3  =>  ret += "Spades"
      case _  =>  ret += "Unknown suit"
    ret.mkString(" ")
}
