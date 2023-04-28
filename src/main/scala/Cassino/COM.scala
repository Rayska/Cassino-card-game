package Cassino

import scala.collection.mutable.Buffer
import scala.language.implicitConversions

case class COM(val playerNumber: Int, val playerName: String, var gameOption: Option[Game] = None) extends Player {

  private var playerScore: Int          = 0

  val cards:               Buffer[Card] = Buffer()
  val pile:                Buffer[Card] = Buffer()
  var sweeps:              Int          = 0

  implicit val placingOrdering: Ordering[Card] = Ordering.by(k => (k.handValue, k.suit))  // Sorting first by handValue and then by suit. This way 2 of Spades will be the last of the 2s to be placed

  //private implicit def bool2int(b: Boolean): Int = if b then 1 else 0
  implicit def ordering[A <: (Buffer[Card], Int)]: Ordering[A] = (x: A, y: A) => x._2.compareTo(y._2)

  //implicit def sorted[B <: (Buffer[Card], Int)](implicit ord: Ordering[B]): Buffer[(Buffer[Card], Int)]
//
  implicit val myOrdering: Ordering[(Buffer[Card], Int)] = Ordering.by[(Buffer[Card], Int), Int](_._2).reverse

  def makePlay(): Unit =  //Here the COM will decide what to play
    var usedForTaking: Option[Card] = None
    var toBeTaken = Buffer[Card]()
    if gameOption.nonEmpty then
      val game = gameOption.get
      if game.tableCards.nonEmpty then
        //place
        val allPossibleTakes = findAllTakes(game)
        if allPossibleTakes.forall(k => k._2.isEmpty) then //Have to place something since can't take anything
          println((placing(), Buffer[Card]())) //
          game.playTurn(placing(), Buffer[Card]())
        else
          val scored: Buffer[(Card, Buffer[(Buffer[Card], Int)])] = findAllTakes(game).map(k => (k._1, k._2.zip(k._2.map(scoreCardBuffer(_) + scoreCardBuffer(Buffer(k._1))))))  // Gives each possible taking move a score based on how good of a move it is, ADD SWEEPS
          val tableCardsSorted = game.tableCards.sorted.toVector
          var potentialPlays: Buffer[(Card, Buffer[(Buffer[Card], Int)])] = cards.sorted.map(k => (k, Buffer[(Buffer[Card], Int)]()))
          for index <- cards.indices do
            scored.update(index, (scored(index)._1, scored(index)._2.sortBy(_._2))) //sort takes by their respective points Int
            for buff <- scored(index)._2.indices do
              scored(index)._2.update(buff, (scored(index)._2(buff)._1.sorted, scored(index)._2(buff)._2)) //sort tableCards in takes using implicit ordering placingOrdering(?)
              if scored(index)._2(buff)._1 == tableCardsSorted then                                           //check if this is a sweep
                scored(index)._2(buff) = scored(index)._2(buff).copy(_2 = scored(index)._2(buff)._2 + 100)      //100 points to sweeps
                potentialPlays.update(index, (potentialPlays(index)._1, scored(index)._2))                        ///add sweeps to potentialPlays. Sort again if needed
          if !potentialPlays.forall(k => k._2.isEmpty) then //if any sweep is possible with COM's cards
            val bestSweeps = Buffer[(Card, (Buffer[Card], Int))]()
            for i <- potentialPlays do
              if i._2.nonEmpty then
                bestSweeps += ((i._1, i._2.head))
            val bestSweep = bestSweeps.maxBy(_._2._2)
            println((Buffer(bestSweep._1), bestSweep._2._1)) //
            game.playTurn(Buffer(bestSweep._1), bestSweep._2._1)
          else // Coudn't play a sweep
            //for index <- cards.indices do
            //  scored.update(index, (scored(index)._1 , scored(index)._2.filterNot(_._2 >= 100))) //remove plays with over 100 points (sweeps)
            val firstSatisfactories = Buffer[(Card, Buffer[Card], Int)]()
            for index <- cards.indices do
              for buff <- scored(index)._2.indices do
                firstSatisfactories += ((scored(index)._1, scored(index)._2(buff)._1, scored(index)._2(buff)._2)) //change format into (Card, Buffer[Card], Int)
            val playOption = firstSatisfactories.sortBy(_._3).findLast(k => k._3 >= 3 && helper(tableCardsSorted, k._2, 10) >= 10) //IF after play, smallest sweep is 8 and points for the take atleast 3 then good
            if playOption.nonEmpty then
              val play = playOption.get
              println((Buffer(play._1), play._2)) //
              game.playTurn(Buffer(play._1), play._2)
            else
              println((placing(), Buffer[Card]())) //
              game.playTurn(placing(), Buffer[Card]())






  def helper(tableCards: Vector[Card], toBeRemoved: Buffer[Card], smallestSweepAcceptable: Int): Int =
    val leftTableCards = tableCards.filterNot(toBeRemoved.contains(_))
    val tableValues = leftTableCards.map(_.tableValue)
    if tableValues.max >= smallestSweepAcceptable then return smallestSweepAcceptable
    val leftSweeps = Buffer[Int]()
    for i <- tableValues.max until smallestSweepAcceptable do
      if gameOption.get.recursive(0, tableValues.toBuffer, Buffer.fill(tableValues.sum / i)(0)) then
        leftSweeps += i
    if leftSweeps.nonEmpty then
      return leftSweeps.min
    -1



          //possibleSweeps.find(_._2.find(k => k._2 - 4 >= (k._1).max  //the value with which a sweep could be taken that would be left))
  //need buffer with all sweep values, then .max of that







          //SWEEPS BY ORDERING TABLE CARDS AND ORDERING CARDS THAT WILL BE TAKEN. IF THEY ARE SAME, ITS A SWEEP
          //FIND SWEEPS AND Filter them. Always go for the sweep

  def placing(): Buffer[Card] = //Determine which card would be best for placing,  blocking sweeps / least value / for own taking later. In that order
    var placings = cards.filterNot(k => k.handValue > 13 || k.suit == 3).sortBy(_.handValue).reverse
    var valuables = cards.filter(k => k.handValue > 13 || k.suit == 3).sorted
    if placings.nonEmpty then
      val tableCards = gameOption.get.tableCards
      if tableCards.map(_.tableValue).sum % 14 > 10 then
        val sweepBreaker: Option[Card] = placings.find(k => (tableCards.map(_.tableValue).sum + k.tableValue) % 14 < 10)
        if sweepBreaker.nonEmpty then
          return Buffer(sweepBreaker.get)
      Buffer(placings.maxBy(_.tableValue))
    else
      Buffer(valuables.min)

  def scored(takes: Buffer[(Card, Buffer[Buffer[Card]])]): Buffer[(Card, Buffer[(Buffer[Card], Int)])] =
    takes.map(k => (k._1, k._2.zip(k._2.map(scoreCardBuffer(_) + scoreCardBuffer(Buffer(k._1)))))) // k._2.foreach(l => l.map(scoreCard(_)).sum)
    //REDUNDANT, just copy the line above to where ever


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


    /*if gameOption.nonEmpty then
      val game = gameOption.get
      val tableValues = game.tableCards.map(_.tableValue)
      var d10OnTable = game.tableCards.exists(_.cardID == 21)
      var d10InHand = this.cards.exists(_.cardID == 21)
      var s2OnTable = game.tableCards.exists(_.cardID == 39)
      var s2InHand = this.cards.exists(_.cardID == 39)
      val spadesOnHand = this.cards.filter(_.suit == 3)
      //val numOfTableCards = game.tableCards.size
      //val lowestTableValue = game.tableCards.minBy(_.tableValue).tableValue
      val highestTableValue = game.tableCards.maxBy(_.tableValue).tableValue
      val sweepsPossibleGen = Buffer[Int]() //handValues with which sweeps are possible in this table right now
       val sweepsPossibleThis: Buffer[Boolean] = this.cards.map(game.recursive(0, tableValues, Buffer.fill((tableValues.sum / _.handValue)(0)))) //handValue with which this COM is able to sweep
      for i <- highestTableValue to 16 do
        if game.recursive(0, tableValues, Buffer.fill(tableValues.sum / i)(0)) then
          sweepsPossibleGen += i
      val sweepsPossibleThis: Buffer[Boolean] = sweepsPossibleGen.filter(k => this.cards.exists(_.handValue == k))
      if sweepsPossibleGen.nonEmpty && this.cards.exists(k => sweepsPossibleGen.contains(k.handValue)) then
        if d10OnTable || s2OnTable then  //Diamonds 10 and Spades 2 from Hand if can be gained without leaving sweep under 10, or if deckSize < 1.5 * playersSize then without leaving sweep < 6, if deckSize <= 4 then just gain them. Then prioritize Aces, then spades
          if highestTableValue >= 8 then
            if d10OnTable then

            else
            //Prio Aces and spades, move to game.playTurn
        else if d10InHand || s2InHand then //Diamonds 10 and Spades 2 from Table if can be gained without leaving sweep under 8. Prio Aces and then spades
          if highestTableValue >= 10 then
            //Prio Aces and spades
          else if game.deck.returnDeckSize < 1.5 * game.players.size then
            if highestTableValue >= 6 then
              //take anything with d10, s2, prio Aces, spades without leaving sweep < 6
            else if game.deck.returnDeckSize <= 4 then
              //take anything with d10, s2, prio Aces, spades

        else if this.cards.exists(_.handValue == 14) && sweepsPossibleGen.contains(14) then //take sweep with Ace

        else if spadesOnHand.nonEmpty then

        else
          //smallest possible handValue sweep
      else if
        //take atleast 2 cards without leaving a sweep under 10
      else
        //place smallest playerCard without leaving a sweep / place a card resulting in the biggest sweep*/
    //gameOption.get.turn = (gameOption.get.turn + 1) % gameOption.get.players.size // TEMP, HERE WILL BE DECISION MAKING AND GIVING IT T game.get.playTurn

  def findAllTakes(game: Game): Buffer[(Card, Buffer[Buffer[Card]])] =
    //val handValue = playerCard.handValue
    val allTakes: Buffer[(Card, Buffer[Buffer[Card]])] = this.cards.zip(Buffer.tabulate(cards.size)(k => Buffer(Buffer[Card]()).empty))
    for i <- allTakes do
      val takes = Buffer[Buffer[Card]]()
      allTakes(allTakes.indexOf(i))._2 ++= canBeTaken(game.tableCards, i._1.handValue, Buffer[Card](), i._1.handValue)
      //allTakes.foreach(k => canBeTaken(game.tableCards, k._1.handValue, k._2,  k._1.handValue))
      def canBeTaken(tableCards: Buffer[Card], handValue: Int, collector: Buffer[Card], incHandValue: Int): Buffer[Buffer[Card]] = //Given a handValue returns which sets of tableCards can be taken tableCards and handValue stay the same over iterations, collector and incHandValue change
        if tableCards.nonEmpty && incHandValue != 0 then
          for card <- tableCards do
            canBeTaken(tableCards.filter(_ != card), handValue, collector :+ card, incHandValue - card.tableValue)
        else if collector.map(_.tableValue).sum == handValue && !takes.exists(collector.map(_.cardID).sorted == _.map(_.cardID).sorted) then
           takes += collector
           takes.filter(k => k.forall(l => !collector.contains(l))).foreach(takes += _ ++ collector)
        takes
    allTakes


  def returnRoundScore: Int =
    /// Round score calculated here from this.cards
    var roundScore = playerScore
    playerScore = 0
    pile.foreach(k => if k.handValue == 14 || k.cardID == 39 then roundScore += 1 else if k.cardID == 21 then roundScore += 2) //For aces and 2 of Spades you get 1 point, for 10 of Diamonds you get 2 points
    roundScore += sweeps
    roundScore

  def clearHand(): Unit = cards.clear //Clears player's hand for a new round formerly .drop(cards.size)

  def clearPile(): Unit = pile.clear

  def clearSweeps(): Unit = sweeps = 0

  def addCardToPile(card: Card)   = pile += card

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

  override def toString: String =
    playerName + ", COM" + ", Player Number: " + playerNumber

}


