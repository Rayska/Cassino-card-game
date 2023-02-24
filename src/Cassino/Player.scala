package Cassino

import scala.collection.mutable.Buffer

trait Player {

  val cards:        Buffer[Card] = Buffer[Card]()
  val playerNumber: Int

  def returnRoundScore: Int

  def addCardToPlayer(card: Card): Unit
}
