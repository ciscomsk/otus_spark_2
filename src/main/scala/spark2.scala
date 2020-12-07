import java.io.PrintWriter
import scala.io.Source
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization.write

object spark2 extends App {

  implicit val formats: DefaultFormats.type = DefaultFormats

  def source = Source.fromURL("https://raw.githubusercontent.com/mledoze/countries/master/countries.json")

  case class CountryName(official: Option[String])
  case class Country(name: CountryName, capital: List[String], area: Int)

  val data: JValue = parse(source.mkString)

  val countries: List[Country] = data.extract[List[Country]]

  val top10ByArea: List[Country] = countries.sortWith(_.area > _.area).take(10)

  // Можно ли обойтись без преобразования кейс-классов ?
  case class DataForJson(name: String, capital: String, area: Int)

  def transform(list: List[Country]): List[DataForJson] =
    list.map(x => DataForJson(x.name.official.getOrElse(""), x.capital.headOption.getOrElse(""), x.area))

  val serJson: String = write(transform(top10ByArea))

  def writeToFile(fileName: String, data: String): Unit = {
    val writer = new PrintWriter(fileName)
    writer.println(data)
    writer.close()
  }

  val outFile: String = args(0)

  writeToFile(outFile, serJson)

}
