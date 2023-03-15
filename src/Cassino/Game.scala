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
      this.updateScores
      roundsPlayed += 1
      endCondition = !scores.forall(_._2 < 16)
    println(s"Game has ended with ${scores.maxBy(_._2)._1} as the Winner!")

  def updateScores: Unit =
    for score <- scores do
      scores.update(scores.indexOf(score), (score._1, score._2 + score._1.returnRoundScore))

  def returnTopScore: (Player, Int) = scores.maxBy(_._2)

}