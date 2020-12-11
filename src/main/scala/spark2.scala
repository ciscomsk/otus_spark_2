import com.fasterxml.jackson.annotation.JsonValue

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

  // v1.1
  case class DataForJson(name: String, capital: String, area: Int)

  def transform1(list: List[Country]): List[DataForJson] =
    list.map(x => DataForJson(x.name.official.getOrElse(""), x.capital.headOption.getOrElse(""), x.area))

  val serJson1: String = write(transform1(top10ByArea))

  // v1.2
  // https://github.com/json4s/json4s
  import org.json4s.JsonDSL._

  def transform2(list: List[Country]): List[JValue] = {
    def countryToJValue(country: Country): JValue =
      ("name" -> country.name.official.getOrElse("")) ~
      ("capital" -> country.capital.headOption) ~
      ("area" -> country.area)

    list.map(countryToJValue)
  }

  val json: List[JValue] = transform2(top10ByArea)
  val json2: JValue = render(json)
  val serJson2 = write(json2)

  def writeToFile(fileName: String, data: String): Unit = {
    val writer = new PrintWriter(fileName)
    writer.println(data)
    writer.close()
  }

  if (args.length != 1) println("Name for output file is not set. Please set it as first argument.")
  else {
    val outFile: String = args(0)
    writeToFile(outFile, serJson2)
  }

}
