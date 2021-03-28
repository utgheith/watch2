import scala.collection.mutable

object main {

  def show(what: String, data: Any) : Unit = {
    val d = data match {
      case a: Array[_] => a.toList.toString
      case x => s"$x : ${x.getClass.getName}"
    }
    println(s"  $what $d")
  }

  val names = ('a' to 'z').map(_.toString)
  val n_events = names.size * 2 + 1

  def main(args: Array[String]): Unit = {
    val data = os.pwd / "data"


    os.remove.all(data)
    os.makeDir(data)

    val changed = mutable.Set[os.Path]()
    val events = mutable.Buffer[(String,Any)]()

    os.watch.watch(Seq(data), changed.addAll, (w,a) => events.append((w,a)))



    (0 to 1000000).foreach { i =>
      println(i)

      names.foreach { s =>
        os.makeDir.all(data / "r" / s)
        os.write(data / "r" / s / s, s)
      }

      while (changed.size != n_events) {
        Thread.sleep(1)
      }
      changed.clear()
      events.clear()

      os.remove.all(data / "r")

      var iter = 0
      while ((changed.size != n_events) && (iter < 10000)) {
        Thread.sleep(1)
        iter += 1
      }
      if (changed.size != n_events) {
        println(s"size = ${changed.size}")
        changed.toList.sorted.foreach(p => println(s" ${p.relativeTo(data)}"))
        println("events")
        events.foreach { case(e,a) => show(e,a)}
        return
      }
      changed.clear()
      events.clear()
    }
  }

}
