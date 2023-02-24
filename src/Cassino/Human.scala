package Cassino

import scala.collection.mutable.Buffer

class Human(val playerNumber: Int, val playerName: String) extends Player {

  private val cards     = Buffer[Card]()
  var playerRoundScore  = 0

  def returnRoundScore: Int = playerRoundScore

}
