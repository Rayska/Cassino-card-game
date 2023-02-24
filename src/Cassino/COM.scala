package Cassino

import scala.collection.mutable.Buffer

class COM(val playerNumber: Int, val playerName: String) extends Player {

  private val cards:    Buffer[Card] = Buffer[Card]()

  var playerRoundScore: Int          = 0

  def returnRoundScore: Int = playerRoundScore

  def addCardToPlayer(card: Card) = cards += card
}