package models
import com.github.nscala_time.time.Imports._
import scala.language.implicitConversions
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api._
import scala.collection.JavaConversions._

/**
 * @author user
 */

object ModelHelper {
  import org.mongodb.scala.bson.BsonDateTime
  implicit def toDateTime(time: BsonDateTime) = new DateTime(time.getValue)
  implicit def toBsonDateTime(jdtime: DateTime) = new BsonDateTime(jdtime.getMillis)

  def main(args: Array[String]) {
    val timestamp = DateTime.parse("2015-04-01")
    println(timestamp.toString())
  }

  def logException(ex: Throwable) = {
    Logger.error(ex.getMessage, ex)
  }

  def errorHandler: PartialFunction[Throwable, Any] = {
    case ex: Throwable =>
      Logger.error("Error=>", ex)
      throw ex
  }

  def errorHandler(prompt: String = "Error=>"): PartialFunction[Throwable, Any] = {
    case ex: org.mongodb.scala.MongoException =>
      Logger.error(ex.getMessage)

    case ex: Throwable =>
      Logger.error(prompt, ex)
      throw ex
  }

  import scala.concurrent._

  def waitReadyResult[T](f: Future[T]) = {
    import scala.concurrent.duration._
    import scala.util._

    val ret = Await.ready(f, Duration.Inf).value.get

    ret match {
      case Success(t) =>
        t
      case Failure(ex) =>
        Logger.error(ex.getMessage, ex)
        throw ex
    }
  }

  import org.mongodb.scala.bson._
  def getOptionTime(key: String)(implicit doc: Document) = {
    if (doc.get(key).isEmpty || doc(key).isNull())
      None
    else
      Some(doc(key).asInt64().getValue)
  }

  def getOptionStr(key: String)(implicit doc: Document) = {
    if (doc.get(key).isEmpty || doc(key).isNull())
      None
    else
      Some(doc.getString(key))
  }

  def getOptionDouble(key: String)(implicit doc: Document) = {
    if (doc.get(key).isEmpty || doc(key).isNull())
      None
    else
      Some(doc(key).asDouble().getValue)
  }

  def getOptionInt(key: String)(implicit doc: Document) = {
    if (doc.get(key).isEmpty || doc(key).isNull())
      None
    else
      Some(doc(key).asInt32().getValue)
  }

  def getOptionDoc(key: String)(implicit doc: Document) = {
    if (doc.get(key).isEmpty || doc(key).isNull())
      None
    else
      Some(doc(key).asDocument())
  }

  def getArray[T](key: String, mapper: (BsonValue) => T)(implicit doc: Document) = {

    val array = doc(key).asArray().getValues

    val result = array map {
      v => mapper(v)
    }
    result.toSeq
  }

  def getOptionArray[T](key: String, mapper: (BsonValue) => T)(implicit doc: Document) = {
    if (doc.get(key).isEmpty || doc(key).isNull())
      None
    else
      Some(getArray(key, mapper))
  }
  
  def isVaildPhone(phone: String) =
    phone.forall { x => x.isDigit || x == '-' || x == '(' || x == ')' || x.isSpaceChar } && !phone.isEmpty()

}

object ExcelTool {
  import org.apache.poi.openxml4j.opc._
  import org.apache.poi.xssf.usermodel._
  import org.apache.poi.xssf.usermodel.XSSFSheet

  def getIntFromCell(cell: XSSFCell) = {
    try {
      cell.getNumericCellValue.toInt
    } catch {
      case ex: IllegalStateException =>
        cell.getStringCellValue.toInt
    }
  }

  def getStrFromCell(cell: XSSFCell) = {
    try {
      cell.getStringCellValue
    } catch {
      case ex: IllegalStateException =>
        cell.getNumericCellValue.toString
    }
  }

  def getOptionStrFromCell(cell: XSSFCell) = {
    try {
      Some(cell.getStringCellValue)
    } catch {
      case ex: Throwable =>
        None
    }
  }
  def getOptionDateFromCell(cell: XSSFCell) = {
    try {
      Some(cell.getDateCellValue)
    } catch {
      case ex: Throwable =>
        None
    }
  }

  import java.io._

  def importXLSX(filePath: String)(parser: (XSSFSheet) => Unit): Boolean = {
    val file = new File(filePath)
    importXLSX(file)(parser)
  }

  def importXLSX(file: File, delete: Boolean = false)(parser: (XSSFSheet) => Unit): Boolean = {
    Logger.info(s"Start import ${file.getAbsolutePath}...")
    //Open Excel
    try {
      val fs = new FileInputStream(file)
      val pkg = OPCPackage.open(fs)
      val wb = new XSSFWorkbook(pkg);

      parser(wb.getSheetAt(0))
      fs.close()
      if (delete)
        file.delete()
      Logger.info(s"Success import ${file.getAbsolutePath}")
    } catch {
      case ex: FileNotFoundException =>
        Logger.warn(s"Cannot open ${file.getAbsolutePath}")
        false
      case ex: Throwable =>
        Logger.error(s"Fail to import ${file.getAbsolutePath}", ex)
        false
    }
    true
  }
}
object EnumUtils {
  def enumReads[E <: Enumeration](enum: E): Reads[E#Value] = new Reads[E#Value] {
    def reads(json: JsValue): JsResult[E#Value] = json match {
      case JsString(s) => {
        try {
          JsSuccess(enum.withName(s))
        } catch {
          case _: NoSuchElementException => JsError(s"Enumeration expected of type: '${enum.getClass}', but it does not appear to contain the value: '$s'")
        }
      }
      case _ => JsError("String value expected")
    }
  }

  implicit def enumWrites[E <: Enumeration]: Writes[E#Value] = new Writes[E#Value] {
    def writes(v: E#Value): JsValue = JsString(v.toString)
  }

  implicit def enumFormat[E <: Enumeration](enum: E): Format[E#Value] = {
    Format(enumReads(enum), enumWrites)
  }

}

import org.mongodb.scala.bson.ObjectId
object ObjectIdUtil {
  def objectIdReads: Reads[ObjectId] = new Reads[ObjectId] {
    def reads(json: JsValue): JsResult[ObjectId] = json match {
      case JsString(s) => {
        try {
          JsSuccess(new ObjectId(s))
        } catch {
          case _: NoSuchElementException => JsError(s"unexpected ObjectId")
        }
      }
      case _ => JsError("String value expected")
    }
  }
  implicit def objectWrites: Writes[ObjectId] = new Writes[ObjectId] {
    def writes(v: ObjectId): JsValue = JsString(v.toHexString)
  }
}
