import java.io.PrintWriter
import scala.io.Source
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization.write

object spark2 extends App {

  implicit val formats: DefaultFormats.type = DefaultFormats

  def source = Source.fromURL("https://raw.githubusercontent.com/mledoze/countries/master/countries.json")

  case class OffCom(official: String, common: String)
  case class Native(nld: OffCom, pap: OffCom)
  case class CountryName(common: String, official: String, native: Native)

  // парсинг имени падает с ошибкой
  case class Country(/*name: CountryName,*/ capital: List[String], area: Int)

  val data: JValue = parse(source.mkString)

  val countries: List[Country] = data.extract[List[Country]]

  val top10ByArea: Seq[Country] = countries.sortWith(_.area > _.area).take(10)

  // как записывать именно первый элемент списка столиц?
  val serJson: String = write(top10ByArea)

  def writeToFile(fileName: String, data: String): Unit = {
    val writer = new PrintWriter(fileName)
    writer.println(data)
    writer.close()
  }

  val outFile: String = args(0)

  writeToFile(outFile, serJson)

}
