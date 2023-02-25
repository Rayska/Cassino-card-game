package Cassino.GUI

import java.awt.Frame
import java.io.PrintWriter
import scala.collection.mutable.Buffer
import scala.swing.*
import scala.swing.Swing.*
import scala.swing.event.ButtonClicked
import scala.sys.exit

object GUI extends MainFrame:

  val textArea = new TextArea()

  this.title = "Cassino"
  this.contents = new BoxPanel(Orientation.Vertical) {
    contents += new Label("this is a label.")
    contents += Button("Click")(println("Button was clicked"))
    contents += new TextField()
    contents += new ComboBox(List("This", "That"))
  }
  //this.contents = Button("Click Me!")(println("Button was clicked."))
  this.size = new Dimension(300,300)
  this.menuBar = new MenuBar {
    contents += new Menu("File") {
      contents += new MenuItem(Action("Open") {openFile})
      contents += new MenuItem("Save") {}
      contents += new Separator
      contents += new MenuItem(Action("Exit") {exit(0)
      })
    }
  }


  def openFile = {
    val chooser = new FileChooser()
    if (chooser.showOpenDialog(null) == FileChooser.Result.Approve) then
      val source = scala.io.Source.fromFile(chooser.selectedFile)
      textArea.text = source.mkString
      source.close()
  }

  def SaveFile = {
    val chooser = new FileChooser()
    if (chooser.showSaveDialog(null) == FileChooser.Result.Approve) then
      val pw = new PrintWriter(chooser.selectedFile)
      pw.print( textArea.text)
      pw.close()
  }
