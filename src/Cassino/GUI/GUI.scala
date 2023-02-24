package Cassino.GUI

import java.awt.Frame
import scala.collection.mutable.Buffer
import scala.swing.*
import scala.swing.Swing.*
import scala.swing.event.ButtonClicked

object GUI extends MainFrame:
  this.title = "My First GUI"
  this.contents = Button("Click Me!")(println("Button was clicked."))
  this.size = new Dimension(300,300)
