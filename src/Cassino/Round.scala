package Cassino
import scala.collection.mutable.Buffer

class Round(val players: Vector[Player]) {
  val deck:         Deck          = new Deck
  val tableCards:   Buffer[Card]  = Buffer[Card]()
  var endCondition: Boolean       = false


  def playTurn: Unit =
    ???

  def checkLegitimacy: Boolean =
    ???

  def dealCards: Unit =
    for player <- players do //for players
      val hand = Buffer.tabulate(4)(k => deck.draw)
      for card <- hand do
        player.addCardToPlayer(card)
    for card <- Buffer.tabulate(4)(k => deck.draw) do
      tableCards += card

}
