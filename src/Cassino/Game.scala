package Cassino

import scala.collection.mutable.Buffer
import scala.swing.*

class Game(val players: Vector[Player]){

  val scores:       Buffer[(Player, Int)] = players.zip(Vector.fill(players.size)(0)).toBuffer
  val endCondition: Boolean = false

  def playRound: Unit =
    new Round

  def addScore(score: Int, player: Player): Unit =
    scores(player.playerNumber) = (player, score)

}