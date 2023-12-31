# Cassino by Ray

## Introduction
This project was made as part of Aalto University's course CS-C2120 Ohjelmointistudio 2: projekti, "Programming studio 2: Project", Spring of 2023. In this project I have created a Cassino card game with a Graphical UI using Scala 3 and ScalaFX. The game has features to save and load previous saves to continue a previous session. The game can have up to 6 players which can be individually selected to be either controlled by the user/users, or the computer.

## User's Manual
Game is launched by running .src/main/scala/Cassino/GUI/Main.scala. The user can then create or load a previous game from the top left corner menu “Game File”. A new game can also be created by pressing the “PLAY” button. When creating a new game, the user has to specify how many players will be taking part in this game and whether they are controlled by the user or a computer algorithm. Each player can be given a unique name, or the default “Player X”.

<p align="center">
  <img src="https://media.giphy.com/media/OTTNT23LfagGMcWYaJ/giphy.gif">
</p>

During the game the user will be able to see the cards on the table (center of screen), their own cards (bottom of screen), back of opponent cards (top of screen), number of cards remaining in the deck (between opponents’ cards and cards on the table), and the playlog (bottom right corner). Additionally the scoreboard can be viewed on a user’s turn by pressing and holding the “G” key on the keyboard. Cards can be selected by either clicking them or by pressing their corresponding keys. A selected card will be displayed as a transparent version of itself. Player’s own cards can be selected using keys A, S, D, and F. Only one card from the player’s hand can be selected so you have to unselect the previous card before you will be able to select another card. Cards on the table can be selected using keys 0 - 9. Due to most keyboard layouts, 1 selects the leftmost card and 0 the rightmost card. Here cards can be selected freely, but a notification will let the user know if their play was against the game rules. X and Enter can be used to play the selected cards. Clicking the PLAY button serves the same purpose.

<p align="center">
  <img src="https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExMzUybXo3bDU0OGhva2xmcDVxanBlbm95YzJxNndqZW1kMDkwOHcxeSZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/Mdtq7dpVRV7ogYMi8h/giphy.gif">
</p>

Computer opponents’ moves will be displayed in the bottom right corner playlog as no animations have been made for making plays. Depending on the situation of the game it might take a second for the algorithm to come up with a play.

## Algorithms

### Move Validity Check
I decided to do the move validity check with an approach I found more interesting than brute-forcing all possible plays and checking whether this particular play was in that set.

The move validity works by checking whether the buffer of tableValues, of the cards being taken, could be partitioned into sum / handValue of the players card. So first of course it has to check whether the sum of tableValues is divisible by the handValue. If it is, it checks whether the tableValues can be partitioned into (tableValues.sum / handValue) sub-buffers with equal sum (that would be the handValue). If the original Buffer can be partitioned into smaller sub-buffers with a sum equal to the handValue, that move is completely valid.

Most of this is done in Game.partitionCheck and more specifically in the helper-method named "recursive". Smaller checks like divisibility and whether the player or table actually has the card are checked in Game.checkMoveLegitimacyTake and Game.SumCheck.

```
def partitionCheck(tableValues: Buffer[Int], numOfPartitions: Int): Boolean =
val sum = tableValues.sum
if numOfPartitions == 0 || sum % numOfPartitions != 0 then
    return false
val targetSum = sum / numOfPartitions
val bufferSum = Array.fill(numOfPartitions)(0)
def recursive(i: Int): Boolean =
    var ret = false
    if i == tableValues.length then
    return bufferSum.forall(_ == targetSum)
    for j <- 0 until numOfPartitions do
    if targetSum >= bufferSum(j) + tableValues(i) then
        bufferSum(j) += tableValues(i)
        if recursive(i + 1) then
        ret = true
        bufferSum(j) -= tableValues(i)
    ret
recursive(0)
```

### Computer Opponent
The computerized opponents find all possible combinations they can take, and then based on the outcome decide what to play. If it is possible to play any sweeps, the algorithm will always play a sweep, and specifically the one that will net the most points to itself. This ranking is not a one-to-one comparison of gained score according to the game rules, but another way to score moves based on the gamescore they give, and the amount of spades and cards. This scoring happens in COM.scoreCardBuffer where it can be inspected more thoroughly.

When a sweep can not be played, the algorithm checks for whether it can play a move with a score of 4 or over. If so, it will pick that without hesitation. If not, it sees if it can play a move worth 2 or more points without leaving sweeps that can be performed with a card under 7. So if it sees a move which .scoreCardBuffer has scored to be 3 and that would leave a cards 9, 4, and 5, it would play this move.

If no such moves are possible, it will check if the remainder of the sum (when divided by 14) of table cards is between 10 and 14. If it is, it will try to find a card from its hand that leaves the remainder of the sum under 10. This is in hopes that it would break any sweep that might be available for the next player to play.

Otherwise it will place the largest card by tableValue to then hopefully further deny sweeps.Lastly if no other cards could be placed, it will pick the smallest card by handValue and place that. Spades are the last suit to be placed when other suits of the same handValue are available for placing.

### Deck Shuffling
Just like it was originally intended, the deck is shuffled upon its creation using scala.util.Random. Specifically the .shuffle method, which works very well here.
```
class Deck(val deck: Buffer[Card] = Random.shuffle(Buffer.tabulate(52)(k => new Card(k))))
```

## Data Structures

Most used data structures in this project were certainly Buffers. This was due to so many parts of the project requiring changes to the collections over the course of the game. Some elements could be stored in immutable collections and thus we can occasionally see Lists and Vectors in the project. This was mainly in GUI though. I have no good reason to use Lists instead of Vectors other than that Mark Lewis used Lists in his ScalaFX videos which I followed throughout making this project. Since they worked I stuck with them and I don’t think it has a major impact on performance. Vectors and Buffers I used because I was familiar with them from earlier courses. I did at one point consider using Sets to store possible moves for the computerized opponent in order to avoid duplicates, but found it unnecessarily complicated to keep jumping between Buffers and Sets so I stuck to Buffers.
Tuples were also used more than I expected at the start of this project. Buffer[(Card, Buffer[(Buffer[Card], Int)])] was the solution I ended with to store different plays while retaining the information about what card is used for taking, what is being taken and how many points it got from COM.scoreCardBuffer.

## Files
This project does not use resources from the internet during runtime. Files related to this project would be the picture of playing cards I got from [opengameart.org](https://opengameart.org/content/playing-cards-vector-png), the .txt save files and the back of card (FlippedCard.png) picture that I made myself. All the pictures used are of format .png. The savefile structure will be demonstrated in fileFormat.pdf. Some screenshots of the game will also be included in the folder screenshots

## Testing
Testing was limited to exploratory testing, debugging and writing short scenarios in REPL. This might lead to bugs I haven’t discovered but considering all the playing I’ve done they should be few.

## Known bugs and defects
Computerized opponent might take a really long time to make a decision if there are many cards on the table. This was most prevalent when placing computerized opponents against each other since they also placed more cards than took, which led to a large number of cards on the table. Having tweaked the parameters of what they will take, this shouldn’t occur anymore unless such a situation is specifically created. This could be fixed by possibly parallelizing scoring and checking sweeps left behind or otherwise making the decision-making algorithms more efficient.

Computerized opponents' turns aren’t very visual since there is no reaction by the GUI to a computer's move other than an addition to the playlog. This could possibly be fixed using AnimationTimers or other ScalaFX methods and structures to show the user an animation of the opponent picking the cards similar to how the user picks cards.

Similar to the previous problem, there are no GUI updates between consecutive computer turns, so no notifications about new Rounds or really anything visual is given to the user during an all-computer game. Only when the game ends, is the playlog visible and the scoreboard can be inspected. The GUI is also not designed with resizing in mind, so leaving it at the default size (1920x1000) is recommended.

## References
I utilized Scala, ScalaFX, and JavaFX documentations quite heavily:
* https://www.scala-lang.org/api/3.2.2/#
* https://www.scala-lang.org/api/2.13.3/index.html
* https://www.scalafx.org/api/8.0/index.html#package
* https://www.scalafx.org/docs/dialogs_and_alerts/
* https://fxdocs.github.io/docs/html5/#_anchorpane
* https://javadoc.io/doc/org.scalafx/scalafx_2.13/latest/index.html

Additionally I followed Mark Lewis's YouTube tutorial playlists about ScalaFX, those were incredibly easy to follow:
* https://www.youtube.com/playlist?list=PLLMXbkbDbVt-VCR1-u4ljyHZfB0VEUw6O
* https://www.youtube.com/playlist?list=PLLMXbkbDbVt-gBs0RqYN129NBfOdNbzYt
