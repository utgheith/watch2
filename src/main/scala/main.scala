import java.nio.file.WatchEvent
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


  case class Locked[A <: AnyRef](it : A) {
    def apply[B](f: A => B): B = it.synchronized {
      f(it)
    }
  }

  def main(args: Array[String]): Unit = {
    val data = os.pwd / "data"


    os.remove.all(data)
    os.makeDir(data)

    val changed_ = Locked(mutable.Set[os.Path]())
    val events_ = Locked(mutable.Buffer[(String,Any)]())

    os.watch.watch(Seq(data), s => changed_(_.addAll(s)), (w,a) => events_(_.append((w,a))))

    (0 to 1000000).foreach { i =>
      println(i)

      names.foreach { s =>
        os.makeDir.all(data / "r" / s)
        os.write(data / "r" / s / s, s)
      }

      while (changed_(_.size) < n_events) {
        Thread.sleep(1)
      }
      changed_(_.clear())
      events_(_.clear())

      os.remove.all(data / "r")

      var iter = 0
      while ((changed_(_.size) < n_events) && (iter < 10000)) {
        Thread.sleep(1)
        iter += 1
      }

      val changed_list = changed_(_.toList).sorted
      val event_list = events_(_.toList)
      if (changed_list.size != n_events) {
        println(s"size = ${changed_list.size}")
        changed_list.foreach(p => println(s" ${p.relativeTo(data)}"))
        println("raw events")
        event_list.foreach { case(e,a) =>
          show(e,a)
        }
        println("delete events")
        var base: os.Path = null
        var contexts: Seq[os.Path] = Seq()
        event_list.foreach {
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
      changed_(_.clear())
      events_(_.clear())
    }
  }

}
