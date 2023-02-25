package Cassino
import scala.collection.mutable.Buffer

class Round(val players: Vector[Player], val dealer: Int) {

  private var turn: Int           = (dealer + 1) % players.size // which players turn it is, starting from the player next to the dealer. Starts from 0

  val deck:         Deck          = new Deck
  val tableCards:   Buffer[Card]  = Buffer[Card]()
  var endCondition: Boolean       = false



  def playTurn: Vector[String] =
    ???
    turn += 1
    Vector[String]()

  def checkLegitimacy(move: Vector[String]): Boolean =
    ???

  def dealCards(players: Vector[Player]): Unit =
    for player <- players do //to the players
      val hand = Buffer.tabulate(4)(k => deck.draw)
      for card <- hand do
        player.addCardToPlayer(card)
    for card <- Buffer.tabulate(4)(k => deck.draw) do //to the table
      tableCards += card

}
