package Cassino

import scala.collection.mutable.Buffer

trait Player {

  val cards:        Buffer[Card]
  val playerNumber: Int

  def returnRoundScore: Int

  def addCardToPile(card: Card):   Unit

  def addCardToPlayer(card: Card): Unit

  def addPoints(add: Int): Unit

  def returnPileSize: Int

  def returnSpadesSize: Int
}
