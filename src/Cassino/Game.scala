package Cassino

import scala.collection.mutable.Buffer
import scala.swing.*

class Game(val players: Vector[Player]){

  val scores:       Buffer[(Player, Int)] = players.zip(Vector.fill(players.size)(0)).toBuffer
  var endCondition: Boolean               = false
  var roundsPlayed: Int                   = 0

  def playGame: Unit =
    while !endCondition do
      new Round(players, roundsPlayed).playRound

      for score <- scores do
        scores.update(scores.indexOf(score), (score._1, score._2 + score._1.returnRoundScore))
      roundsPlayed += 1
      endCondition = scores.forall(_._2 < 16)


  def addScore(score: Int, player: Player): Unit =
    scores(player.playerNumber) = (player, score)

  def returnTopScore: (Player, Int) = scores.maxBy(_._2)

}