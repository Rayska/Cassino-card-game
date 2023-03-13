package Cassino
import scala.collection.mutable.Buffer
import scala.io.StdIn.readLine

class Round(val players: Vector[Player], val dealer: Int) {

  private var turn: Int                   = (dealer + 1) // Round starts from the player next to the dealer

  val deck:         Deck                  = new Deck
  val tableCards:   Buffer[Card]          = Buffer()
  val sweeps:       Buffer[(Player, Int)] = players.zip(Vector.tabulate(players.size)(k => 0)).toBuffer //Could be implemented into Player via methods in playTurn
  var endCondition: Boolean               = false

  def playRound: Unit =
    this.dealCards
    while !endCondition do
      this.playTurn
      endCondition = players.forall(_.cards.isEmpty)
    players.foreach(k => k.addExtraScore(sweeps.find(l => l._1.playerNumber == k.playerNumber).get._2, true, true)) //not working yet

  def playTurn: (Buffer[(Card, Buffer[Card])], Buffer[Card]) =
    turn += 1
    val whosTurn = players(turn % players.size)                                 // which player's turn it is (indexes so starts from 0)
    //println(tableCards)                                                       //
    //println(whosTurn.cards)                                                   //
    println("What would you like to do?")                                       //
    var userInput = readLine().split(" ")                                       //
    var take = userInput.takeWhile(_ != "place").tail.toBuffer                  //
    var place = userInput.reverse.takeWhile(_ != "place").reverse.toBuffer      //
    //print(s"${take.mkString(" ")}, ${place.mkString(" ")}")                   //
    /*while !checkLegitimacyTake(take) && !checkLegitimacyPlace(place) do         //
      println("Invalid move! Please make your move:")                           //
      userInput = readLine().split(" ")                                         //
      take = userInput.takeWhile(_ != "place").tail.toBuffer                    //
      place = userInput.reverse.takeWhile(_ != "place").reverse.toBuffer*/        //
    (Buffer((new Card(52), Buffer(new Card(52), new Card(52)))), Buffer(new Card(52)))  //

  def checkMoveLegitimacyTake(take: Buffer[(Card, Buffer[Card])]): Boolean =
    //For each taking move the player has to determine which card is used to pick which cards
    val toBeTaken     = take.collect(_._2).flatten                                                                    //Cards which the player is trying to take off the table
    val noDuplicates  = toBeTaken == toBeTaken.distinct                                                               //There are no duplicates in the cards we are trying to take (same card cannot be taken multiple times)
    val playerHasCard = checkPlayerHasCard(take.collect(_._1))                                                        //Player has every(/the) card in their hand they are trying to use
    val oneAndSame    = take.forall(take.head._1.cardID == _._1.cardID)                                               //Same card is used for every "taking" move
    val isEqual       = take.forall(k => (k._1.handValue == k._2.foldLeft(0)((l: Int, m: Card) => l + m.tableValue))) //handValue of card in use is equal to table cards' tableValue
    val cardOnTable   = take.forall(k => k._2.forall(l => tableCards.exists(_.cardID == l.cardID)))                   //Cards we are trying to take are on the Table

    noDuplicates && playerHasCard && oneAndSame && isEqual && cardOnTable

  def checkPlayerHasCard(place: Buffer[Card]): Boolean =
    var truth = true
    var index = 0
    while truth && index < place.size do
      if !players(turn % players.size).cards.exists(_.cardID == place(index).cardID) then
        truth = false
      index += 1
    truth


  def dealCards: Unit =
    for player <- players do //to the players
      val hand = Buffer.tabulate(4)(k => deck.draw.getOrElse(new Card(52)))
      for card <- hand do
        player.addCardToPlayer(card)
    for card <- Buffer.tabulate(4)(k => deck.draw.getOrElse(new Card(52))) do //to the table
      tableCards += card

}
