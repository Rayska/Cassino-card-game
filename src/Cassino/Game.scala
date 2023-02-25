package Cassino

import scala.collection.mutable.Buffer
import scala.swing.*

class Game(val players: Vector[Player]){

  val scores:       Buffer[(Player, Int)] = players.zip(Vector.fill(players.size)(0)).toBuffer
  val endCondition: Boolean               = false
  var roundsPlayed = 0

  def playRound(players: Vector[Player], roundsPlayed: Int): Unit =
    new Round(players, roundsPlayed)

  def addScore(score: Int, player: Player): Unit =
    scores(player.playerNumber) = (player, score)

}