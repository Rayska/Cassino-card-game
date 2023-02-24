package Cassino

import scala.collection.mutable.Buffer
import scala.util.Random

class Deck {

 val deck: Buffer[Card] = Random.shuffle(Buffer.tabulate(52)(k => new Card(k))) //Shuffled Buffer with one of each card

 def draw: Card = deck.remove(0) // removes and returns the first element of the Buffer simultaniously

}
