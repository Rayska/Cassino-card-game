package Cassino

class Card(val handValue: Int) {
  val suit = handValue / 13 // 0 for Clubs, 1 for Diamonds, 2 for Hearts and 3 for Spades
}
