package Cassino

import scala.collection.mutable.Buffer
import scala.util.Random

class Deck {
 val deck: Buffer[Card] = Random.shuffle(Buffer.tabulate(52)(new Card(_))) //Shuffled Buffer with one of each card

}
