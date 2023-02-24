package Cassino
import scala.collection.mutable.Buffer

class Round {
  val deck:         Unit          = new Deck
  val tableCards:   Buffer[Card]  = Buffer[Card]()
  var endCondition: Boolean       = false


  def playTurn: Unit =
    ???

}
