package Cassino

import scala.collection.mutable.Buffer

trait Player {

  val playerNumber: Int

  def returnRoundScore: Int
}
