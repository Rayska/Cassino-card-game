package Cassino

import scala.collection.mutable.Buffer
import scala.language.implicitConversions

case class COM(val playerNumber: Int, val playerName: String, var gameOption: Option[Game] = None) extends Player {

  private var playerScore: Int          = 0

  val cards:               Buffer[Card] = Buffer()
  val pile:                Buffer[Card] = Buffer()
  var sweeps:              Int          = 0

  implicit val placingOrdering: Ordering[Card] = Ordering.by(k => (k.handValue, k.suit))  // Sorting first by handValue and then by suit. This way 2 of Spades will be the last of the 2s to be placed

  private val takeTresholdSweep = 2
  private val abosluteTakeTreshold = 4
  private val smallestAcceptableSweep = 7

  def makePlay(): Unit =  //Here the COM will decide what to play
    var usedForTaking: Option[Card] = None
    var toBeTaken = Buffer[Card]()
    if gameOption.nonEmpty then
      val game = gameOption.get
      if game.returnTableCards.nonEmpty then
        val allPossibleTakes = findAllTakes(game)
        if allPossibleTakes.forall(k => k._2.isEmpty) then //Have to place something since can't take anything
          game.playTurn(placing(), Buffer[Card]())
        else
          val scored: Buffer[(Card, Buffer[(Buffer[Card], Int)])] = findAllTakes(game).map(k => (k._1, k._2.zip(k._2.map(scoreCardBuffer(_) + scoreCardBuffer(Buffer(k._1)))))).sortBy(_._1)  // Gives each possible taking move a score based on how good of a move it is, ADD SWEEPS
          val tableCardsSorted = game.returnTableCards.sorted.toVector
          var potentialPlays: Buffer[(Card, Buffer[(Buffer[Card], Int)])] = cards.sorted.map(k => (k, Buffer[(Buffer[Card], Int)]()))
          for index <- cards.indices do
            scored.update(index, (scored(index)._1, scored(index)._2.sortBy(_._2))) //sort takes by their respective points Int
            for buff <- scored(index)._2.indices do
              scored(index)._2.update(buff, (scored(index)._2(buff)._1.sorted, scored(index)._2(buff)._2)) //sort tableCards in takes using implicit ordering placingOrdering(?)
              if scored(index)._2(buff)._1 == tableCardsSorted then                                           //check if this is a sweep
                scored(index)._2(buff) = scored(index)._2(buff).copy(_2 = scored(index)._2(buff)._2 + 100)      //100 points to sweeps
                potentialPlays.update(index, (potentialPlays(index)._1, scored(index)._2))                        ///add sweeps to potentialPlays. Sort again if needed //SOMETHING WRONG HERE: ADDS TAKES TO WRONG BUFFER
          if !potentialPlays.forall(k => k._2.isEmpty) then //if any sweep is possible with COM's cards
            val bestSweeps = Buffer[(Card, (Buffer[Card], Int))]()
            for i <- potentialPlays do
              if i._2.nonEmpty then
                bestSweeps += ((i._1, i._2.head))
            val bestSweep = bestSweeps.maxBy(_._2._2)
            game.playTurn(Buffer(bestSweep._1), bestSweep._2._1)
          else // Coudn't play a sweep
            val firstSatisfactories = Buffer[(Card, Buffer[Card], Int)]()
            for index <- cards.indices do
              for buff <- scored(index)._2.indices do
                firstSatisfactories += ((scored(index)._1, scored(index)._2(buff)._1, scored(index)._2(buff)._2)) //change format into (Card, Buffer[Card], Int)
            val sortedPlays = firstSatisfactories.sortBy(_._3).reverse
            val highestTake = sortedPlays.headOption
            val playOption = sortedPlays.find(k => k._3 >= takeTresholdSweep && helper(tableCardsSorted, k._2, smallestAcceptableSweep) >= smallestAcceptableSweep) //IF after play, smallest sweep is 8 and points for the take atleast 3 then good
            if highestTake.nonEmpty || playOption.nonEmpty then
              if highestTake.nonEmpty && highestTake.get._3 >= abosluteTakeTreshold then
                game.playTurn(Buffer(highestTake.get._1), highestTake.get._2)
              else if playOption.nonEmpty then
                val play = playOption.get
                game.playTurn(Buffer(play._1), play._2)
              else
                game.playTurn(placing(), Buffer[Card]())
            else
              game.playTurn(placing(), Buffer[Card]())
      else
        game.playTurn(placing(), Buffer[Card]())

  def helper(tableCards: Vector[Card], toBeRemoved: Buffer[Card], smallestSweepAcceptable: Int): Int =
    val leftTableCards = tableCards.filterNot(toBeRemoved.contains(_))
    val tableValues = leftTableCards.map(_.tableValue)
    if tableValues.max >= smallestSweepAcceptable then return smallestSweepAcceptable
    val leftSweeps = Buffer[Int]()
    for i <- tableValues.max until smallestSweepAcceptable do
      if gameOption.get.partitionCheck(tableValues.toBuffer, tableValues.sum / i) then
        leftSweeps += i
    if leftSweeps.nonEmpty then
      return leftSweeps.min
    -1

  def placing(): Buffer[Card] = //Determine which card would be best for placing,  blocking sweeps / least value / for own taking later. In that order
    var placings = cards.filterNot(k => k.handValue > 13 || k.suit == 3).sortBy(_.handValue).reverse
    var valuables = cards.filter(k => k.handValue > 13 || k.suit == 3).sorted
    if placings.nonEmpty then
      val tableCards = gameOption.get.returnTableCards
      if tableCards.map(_.tableValue).sum % 14 > 10 then
        val sweepBreaker: Option[Card] = placings.find(k => (tableCards.map(_.tableValue).sum + k.tableValue) % 14 < 10)
        if sweepBreaker.nonEmpty then
          return Buffer(sweepBreaker.get)
      Buffer(placings.maxBy(_.tableValue))
    else if valuables.nonEmpty then
      Buffer(valuables.min)
    else
      Buffer[Card](new Card(52))

  def scoreCardBuffer(cards: Buffer[Card]): Int = //4 for d10, 3 for s2 and aces, 1 for spades. And for being a card, 1
    var collector = 0
    for card <- cards do
      if card.handValue == 16 then                              //D10
          collector += 4
      else if card.handValue == 14 || card.handValue == 15 then //Aces and S2
        collector += 3
      if card.suit == 3 then                                    //Spades
        collector += 1
      collector += 1                                            //Cards
    collector

  def findAllTakes(game: Game): Buffer[(Card, Buffer[Buffer[Card]])] =
    val allTakes: Buffer[(Card, Buffer[Buffer[Card]])] = this.cards.zip(Buffer.tabulate(cards.size)(k => Buffer(Buffer[Card]()).empty))
    for i <- allTakes do
      val takes = Buffer[Buffer[Card]]()
      allTakes(allTakes.indexOf(i))._2 ++= canBeTaken(game.returnTableCards, i._1.handValue, Buffer[Card](), i._1.handValue)
      def canBeTaken(tableCards: Buffer[Card], handValue: Int, collector: Buffer[Card], incHandValue: Int): Buffer[Buffer[Card]] = //Given a handValue returns which sets of tableCards can be taken tableCards and handValue stay the same over iterations, collector and incHandValue change
        if tableCards.nonEmpty && incHandValue != 0 then
          for card <- tableCards do
            canBeTaken(tableCards.filter(_ != card), handValue, collector :+ card, incHandValue - card.tableValue)
        else if collector.map(_.tableValue).sum == handValue && !takes.exists(collector.map(_.cardID).sorted == _.map(_.cardID).sorted) then
           takes += collector
           takes.filter(k => k.forall(l => !collector.contains(l))).foreach(takes += _ ++ collector)
        takes
    allTakes

  def returnRoundScore: Int = // Round score calculated here from this.cards
    var roundScore = playerScore
    playerScore = 0
    pile.foreach(k => if k.handValue == 14 || k.cardID == 39 then roundScore += 1 else if k.cardID == 21 then roundScore += 2) //For aces and 2 of Spades you get 1 point, for 10 of Diamonds you get 2 points
    roundScore += sweeps
    roundScore

  def clearHand(): Unit = cards.clear //Clears player's hand for a new round formerly .drop(cards.size)

  def clearPile(): Unit = pile.clear

  def clearSweeps(): Unit = sweeps = 0

  def addCardToPile(card: Card) = pile += card

  def addCardToPlayer(cardOption: Option[Card]) = if cardOption.nonEmpty then cards += cardOption.get

  def removeCardFromPlayer(card: Card): Unit = cards -= card

  def addPoints(add: Int): Unit = playerScore += add

  def addSweep(): Unit = sweeps += 1

  def returnPileSize: Int = pile.size

  def returnSpadesSize: Int = pile.count(_.suit == 3)

  def returnAcesSize: Int = pile.count(_.tableValue == 1)

  def returnSweeps: Int = sweeps

  def changeGame(newgame: Game): Unit = gameOption = Some(newgame)

  def returnGame: Game = gameOption.get

  def returnCards: Buffer[Card] = cards

  def returnPile: Buffer[Card] = pile

  override def toString: String =
    playerName + ", COM" + ", Player Number: " + playerNumber

}


