package Cassino

import scala.collection.mutable.Buffer
import scala.util.Random

class Deck(val deck: Buffer[Card] = Random.shuffle(Buffer.tabulate(52)(k => new Card(k)))) {

 def draw: Option[Card] = 
  if deck.nonEmpty then
   Some(deck.remove(0)) // removes and returns the first element of the Buffer simultaniously
  else
   None

}
