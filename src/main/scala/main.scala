import java.nio.file.{StandardWatchEventKinds, WatchEvent, WatchService}
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
        println("raw events")
        events.foreach { case(e,a) =>
          show(e,a)
        }
        println("delete events")
        var base: os.Path = null
        var contexts: Seq[os.Path] = Seq()
        events.foreach {
          case ("WATCH KEY",_) =>
          case ("WATCH KEY0",_) =>
          case ("WATCH PATH",a) =>
            base = a.asInstanceOf[os.Path]
            //println(s"base=$base")
          case ("WATCH CONTEXTS",a) =>
            contexts = a.asInstanceOf[mutable.Buffer[java.nio.file.Path]].map(x => base / x.toString).toSeq
          case ("WATCH KINDS",a) =>
            a.asInstanceOf[mutable.Buffer[WatchEvent.Kind[_]]].foreach { k =>
              k.name match {
                case "ENTRY_DELETE" =>
                  contexts.foreach { p =>
                    println(s" DELETE $p")
                  }
                case _ =>
              }
            }
          case ("WATCH CANCEL",a) =>
            println(s"    CANCEL $a")

          case ("TRIGGER",a) =>
            println(s"        TRIGGER $a")

          case ("WATCH CURRENT",_) =>


          case (e,a) =>
            println(s"    ???????????? $e $a")
        }
        return
      }
      changed.clear()
      events.clear()
    }
  }

}
