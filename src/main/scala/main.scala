import java.nio.file.WatchEvent
import scala.collection.mutable

case class Locked[A <: AnyRef](it : A) {
  def apply[B](f: A => B): B = it.synchronized {
    f(it)
  }
}

object main {

  private val names = ('a' to 'z').map(_.toString)
  private val n_events = names.size * 2 + 1

  def main(args: Array[String]): Unit = {
    val data = os.pwd / "data"


    os.remove.all(data)
    os.makeDir(data)





    (0 to 1000000).foreach { i =>
      println(i)

      names.foreach { s =>
        os.makeDir.all(data / "r" / s)
        os.write(data / "r" / s / s"file_$s", s)
      }

      val changed = Locked(mutable.Set[os.Path]())
      val events = Locked(mutable.Buffer[(String,Any)]())

      val w = os.watch.watch(Seq(data), s => changed(_.addAll(s)), (w,a) => events(_.append((w,a))))

      os.remove.all(data / "r")

      var iter = 0
      while ((changed(_.size) < n_events) && (iter < 20000)) {
        Thread.sleep(1)
        iter += 1
      }

      val changed_list = changed(_.toList).sorted
      val event_list = events(_.toList)
      if (changed_list.size != n_events) {
        println(s"size = ${changed_list.size}")
        changed_list.foreach(p => println(s" ${p.relativeTo(data)}"))
        println("raw events")
        event_list.foreach { case(e,a) =>
          println(s"  $e $a")
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
                    println(s"  DELETE $p")
                  }
                case _ =>
              }
            }
          case ("WATCH CANCEL",a) =>
            println(s"    CANCEL $a")

          case ("TRIGGER",a) =>
            println(s"    TRIGGER $a")
            a.asInstanceOf[Set[(os.Path,Boolean)]].toList.sortBy(_._1).foreach { case (p,b) =>
              println(s"        ($p,$b)")
            }

          case ("WATCH",a) =>
            println(s"    WATCH $a")

          case ("WATCH CURRENT",_) =>


          case (e,a) =>
            println(s"    ???????????? $e $a")
        }
        return
      }

      w.close()
      
    }
  }

}
