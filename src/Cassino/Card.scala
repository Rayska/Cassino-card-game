package Cassino

class Card(val cardID: Int) {
  val suit: Int = cardID / 13       // 0 for Clubs, 1 for Diamonds, 2 for Hearts and 3 for Spades
  val handValue: Int = {
    if (cardID + 1) % 13 == 0 then  // Aces
      14
    else if cardID == 21 then       // Diamonds 10
      16
    else if cardID == 39 then       // Spades 3
      15
    else
      cardID % 13 + 2
  }
  val tableValue: Int = {
    if (cardID + 1) % 13 == 0 then  // Aces
      1
    else
      cardID % 13 + 2
  }

}
