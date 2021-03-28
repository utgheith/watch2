import scala.collection.mutable

object main {

  val LOG = false

  def logger(what: String, data: Any) : Unit = {
    if (LOG) {
      val d = data match {
        case a: Array[_] => a.toList.toString
        case x => s"$x : ${x.getClass.getName}"
      }
      println(s"$what $d")
    }
  }

  def printer(paths: Set[os.Path]): Unit = {
    println("event")
    paths.map(_.relativeTo(os.pwd)).foreach { p => println(s"  $p")}
  }

  def main(args: Array[String]): Unit = {
    val data = os.pwd / "data"


    os.remove.all(data)
    os.makeDir(data)

    val changed = mutable.Set[os.Path]()


    os.watch.watch(Seq(data), changed.addAll, logger)

    (0 to 1000000).foreach { i =>
      println(i)

      ('a' to 'z').foreach { n =>
        val s = n.toString
        os.makeDir.all(data / "r" / s)
        os.write(data / "r" / s / s, s)
      }

      while (changed.size != 53) {
        Thread.sleep(1)
      }
      changed.clear()

      os.remove.all(data / "r")

      while (changed.size != 53) {
        Thread.sleep(1)
      }
      changed.clear()
    }
  }

}
