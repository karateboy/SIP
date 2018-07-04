package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.Play.current
import com.github.nscala_time.time.Imports._
import models._
import PdfUtility._
import scala.concurrent.ExecutionContext.Implicits.global
import models.ModelHelper._

object PeriodReport extends Enumeration {
  val DailyReport = Value("daily")
  val MonthlyReport = Value("monthly")
  val MinMonthlyReport = Value("MinMonthly")
  val YearlyReport = Value("yearly")
  def map = Map(DailyReport -> "日報", MonthlyReport -> "月報",
    MinMonthlyReport -> "分鐘月報",
    YearlyReport -> "年報")

}

object Report extends Controller {

  def monitorReport() = Security.Authenticated { implicit request =>
    Ok(views.html.monitorReport(""))
  }

  def getMinMonthlySocket = WebSocket.acceptWithActor[String, String] { request => out =>
    MinMonthlyReportWorker.props(out)
  }

  def getOverallStatMap(statMap: Map[MonitorType.Value, Map[DateTime, Stat]]) = {
    val mTypes = statMap.keys.toList
    statMap.map { pair =>
      val mt = pair._1
      val dateMap = pair._2
      val values = dateMap.values.toList
      val total = values.size
      val hour_total = values.map { _.total }.sum
      val count = values.count(_.isEffective)
      val hour_count = values.map { _.count }.sum
      val overCount = values.map { _.overCount }.sum
      val max = values.map { _.avg }.max
      val validValues = values.flatMap { _.avg }
      val min = if (validValues.isEmpty)
        None
      else
        Some(validValues.min)

      val avg =
        if (mt != MonitorType.WIN_DIRECTION) {
          if (total == 0 || count == 0)
            None
          else {
            Some(values.filter { _.avg.isDefined }.map { s => s.avg.get * s.total }.sum / (values.map(_.total).sum))
          }
        } else {
          val winSpeedMap = statMap(MonitorType.WIN_SPEED)
          val dates = dateMap.keys.toList
          val windDir = dates.map { dateMap }
          val windSpeed = dates.map { winSpeedMap }
          def windAvg(): Option[Double] = {
            val windRecord = windSpeed.zip(windDir).filter(w => w._1.avg.isDefined && w._2.avg.isDefined)
            if (windRecord.length == 0)
              None
            else {
              val wind_sin = windRecord.map {
                v => v._1.avg.get * Math.sin(Math.toRadians(v._2.avg.get))
              }.sum

              val wind_cos = windRecord.map(v => v._1.avg.get * Math.cos(Math.toRadians(v._2.avg.get))).sum
              Some(Query.windAvg(wind_sin, wind_cos))
            }
          }
          windAvg()
        }

      mt -> Stat(
        avg = avg,
        min = min,
        max = max,
        total = total,
        count = count,
        overCount = overCount,
        hour_count = Some(hour_count),
        hour_total = Some(hour_total))
    }
  }

  case class CellData(v: String, cellClassName: String)
  case class RowData(cellData: Seq[CellData])
  case class DataTab(columnNames: Seq[String], rows: Seq[RowData])

  implicit val cellWrite = Json.writes[CellData]
  implicit val rowWrite = Json.writes[RowData]
  implicit val dtWrite = Json.writes[DataTab]

  def getMonitorReport(monitorStr: String, reportTypeStr: String, startDate: Long, outputTypeStr: String) = Security.Authenticated {
    implicit request =>
      val monitor = Monitor.withName(java.net.URLDecoder.decode(monitorStr, "UTF-8"))
      val reportType = PeriodReport.withName(reportTypeStr)
      val start = new DateTime(startDate)
      val outputType = OutputType.withName(outputTypeStr)

      if (outputType == OutputType.html || outputType == OutputType.pdf) {
        val (title, output) =
          reportType match {
            case PeriodReport.DailyReport =>
              val adjustedStart = start.withMillisOfDay(0)
              val periodMap = Record.getRecordMap(Record.HourCollection)(MonitorType.mtvList, monitor, adjustedStart, adjustedStart + 1.day)
              val mtTimeMap = periodMap.map { pair =>
                val k = pair._1
                val v = pair._2
                k -> Map(v.map { r => r.time -> r }: _*)
              }
              val statMap = Query.getPeriodStatReportMap(periodMap, 1.day)(adjustedStart, adjustedStart + 1.day)

              ("日報", views.html.dailyReport(monitor, start, MonitorType.activeMtvList, mtTimeMap, statMap))

            case PeriodReport.MonthlyReport =>
              val adjustedStart = start.withDayOfMonth(1).withMillisOfDay(0)
              val periodMap = Record.getRecordMap(Record.HourCollection)(MonitorType.activeMtvList, monitor, adjustedStart, adjustedStart + 1.month)
              val statMap = Query.getPeriodStatReportMap(periodMap, 1.day)(adjustedStart, adjustedStart + 1.month)
              val overallStatMap = getOverallStatMap(statMap)
              ("月報", views.html.monthlyReport(monitor, adjustedStart, MonitorType.activeMtvList, statMap, overallStatMap))

            case PeriodReport.YearlyReport =>
              val adjustedStart = start.withDayOfYear(1).withMillisOfDay(0)
              val periodMap = Record.getRecordMap(Record.HourCollection)(MonitorType.activeMtvList, monitor, adjustedStart, adjustedStart + 1.year)
              val statMap = Query.getPeriodStatReportMap(periodMap, 1.month)(adjustedStart, adjustedStart + 1.year)
              val overallStatMap = getOverallStatMap(statMap)
              ("年報", views.html.yearlyReport(monitor, adjustedStart, MonitorType.activeMtvList, statMap, overallStatMap))

          }

        outputType match {
          case OutputType.html =>
            Ok(output)
          case OutputType.pdf =>
            Ok.sendFile(
              creatPdfWithReportHeader(title, output),
              fileName = _ =>
                play.utils.UriEncoding.encodePathSegment(title + start.toString("YYYYMM") + ".pdf", "UTF-8"))
        }
      } else if (outputType == OutputType.json) {
        val (columnNames, rows) =
          reportType match {
            case PeriodReport.DailyReport =>
              val adjustedStart = start.withMillisOfDay(0)
              val periodMap = Record.getRecordMap(Record.HourCollection)(MonitorType.mtvList, monitor, adjustedStart, adjustedStart + 1.day)
              val mtTimeMap = periodMap.map { pair =>
                val k = pair._1
                val v = pair._2
                k -> Map(v.map { r => r.time -> r }: _*)
              }
              val statMap = Query.getPeriodStatReportMap(periodMap, 1.day)(adjustedStart, adjustedStart + 1.day)

              val mtColumnNames =
                for (mt <- MonitorType.activeMtvList) yield s"${MonitorType.map(mt).desp}"

              val topRows = for {
                i <- 0 to 23
              } yield {
                val timeCell = CellData("%02d:00".format(i), "")
                val valueCells =
                  for {
                    mt <- MonitorType.activeMtvList
                    v = MonitorType.formatRecord(mt, mtTimeMap(mt).get(start + i.hour))
                    styleStr = MonitorType.getCssClassStr(mt, mtTimeMap(mt).get(start + i.hour))
                  } yield CellData(v, styleStr)
                RowData(timeCell :: valueCells)
              }
              val avgRow = {
                val titleCell = CellData("平均", "")
                val avgCells =
                  for {
                    mt <- MonitorType.activeMtvList
                    v = MonitorType.format(mt, statMap(mt)(start).avg)
                  } yield CellData(v, "")
                RowData(titleCell :: avgCells)
              }
              val maxRow = {
                val titleCell = CellData("最大", "")
                val vCells =
                  for {
                    mt <- MonitorType.activeMtvList
                    v = MonitorType.format(mt, statMap(mt)(start).max)
                  } yield CellData(v, "")
                RowData(titleCell :: vCells)
              }
              val minRow = {
                val titleCell = CellData("最小", "")
                val vCells =
                  for {
                    mt <- MonitorType.activeMtvList
                    v = MonitorType.format(mt, statMap(mt)(start).min)
                  } yield CellData(v, "")
                RowData(titleCell :: vCells)
              }

              val effectiveRow = {
                val titleCell = CellData("有效率(%)", "")
                val vCells =
                  for {
                    mt <- MonitorType.activeMtvList
                    v = MonitorType.format(mt, statMap(mt)(adjustedStart).effectPercent)
                  } yield CellData(v, "")
                RowData(titleCell :: vCells)
              }

              val rows = topRows :+ avgRow :+ maxRow :+ minRow :+ effectiveRow
              ("時間" :: mtColumnNames, rows)

            case PeriodReport.MonthlyReport =>
              val adjustedStart = start.withDayOfMonth(1).withMillisOfDay(0)
              val periodMap = Record.getRecordMap(Record.HourCollection)(MonitorType.activeMtvList, monitor, adjustedStart, adjustedStart + 1.month)
              val statMap = Query.getPeriodStatReportMap(periodMap, 1.day)(adjustedStart, adjustedStart + 1.month)
              val overallStatMap = getOverallStatMap(statMap)
              val mtColumnNames =
                for (mt <- MonitorType.activeMtvList) yield s"${MonitorType.map(mt).desp}"

              val topRows = for {
                t <- Query.getPeriods(adjustedStart, adjustedStart + 1.month, 1.day)
              } yield {
                val timeCell = CellData(t.toString("d"), "")
                val valueCells =
                  for {
                    mt <- MonitorType.activeMtvList
                    v = MonitorType.format(mt, statMap(mt)(t).avg)
                    styleStr = if (!statMap(mt)(t).isEffective)
                      "abnormal_status"
                    else
                      ""
                  } yield CellData(v, styleStr)
                RowData(timeCell :: valueCells)
              }
              val avgRow = {
                val titleCell = CellData("平均", "")
                val avgCells =
                  for {
                    mt <- MonitorType.activeMtvList
                    v = MonitorType.format(mt, overallStatMap(mt).avg)
                  } yield CellData(v, "")
                RowData(titleCell :: avgCells)
              }
              val maxRow = {
                val titleCell = CellData("最大", "")
                val vCells =
                  for {
                    mt <- MonitorType.activeMtvList
                    v = MonitorType.format(mt, overallStatMap(mt).max)
                  } yield CellData(v, "")
                RowData(titleCell :: vCells)
              }
              val minRow = {
                val titleCell = CellData("最小", "")
                val vCells =
                  for {
                    mt <- MonitorType.activeMtvList
                    v = MonitorType.format(mt, overallStatMap(mt).min)
                  } yield CellData(v, "")
                RowData(titleCell :: vCells)
              }

              val hourCountRow = {
                val titleCell = CellData("有效時數", "")
                val vCells =
                  for {
                    mt <- MonitorType.activeMtvList
                    v = overallStatMap(mt).hour_count.get.toString()
                  } yield CellData(v, "")
                RowData(titleCell :: vCells)
              }

              val hourTotalRow = {
                val titleCell = CellData("應測時數", "")
                val vCells =
                  for {
                    mt <- MonitorType.activeMtvList
                    v = overallStatMap(mt).hour_total.get.toString()
                  } yield CellData(v, "")
                RowData(titleCell :: vCells)
              }
              val effectiveRow = {
                val titleCell = CellData("有效率(%)", "")
                val vCells =
                  for {
                    mt <- MonitorType.activeMtvList
                    v = MonitorType.format(mt, overallStatMap(mt).hourEffectPercent)
                  } yield CellData(v, "")
                RowData(titleCell :: vCells)
              }

              val rows = topRows :+ avgRow :+ maxRow :+ minRow :+ hourCountRow :+ hourTotalRow :+ effectiveRow
              ("日期" :: mtColumnNames, rows)

            /*
            case PeriodReport.YearlyReport =>
              val adjustedStart = start.withDayOfYear(1).withMillisOfDay(0)
              val periodMap = Record.getRecordMap(Record.HourCollection)(MonitorType.activeMtvList, monitor, adjustedStart, adjustedStart + 1.year)
              val statMap = Query.getPeriodStatReportMap(periodMap, 1.month)(adjustedStart, adjustedStart + 1.year)
              val overallStatMap = getOverallStatMap(statMap)
              ("年報", views.html.yearlyReport(monitor, adjustedStart, MonitorType.activeMtvList, statMap, overallStatMap))
					*/
          }

        Ok(Json.toJson(DataTab(columnNames, rows)))

      } else {
        import java.io.File
        import java.nio.file.Files
        val (title, excelFile) =
          reportType match {
            case PeriodReport.DailyReport =>
              val adjustedStart = start.withMillisOfDay(0)
              ("日報" + start.toString("YYYYMMdd"), ExcelUtility.createDailyReport(monitor, adjustedStart))

            case PeriodReport.MonthlyReport =>
              val adjustedStart = start.withDayOfMonth(1).withMillisOfDay(0)
              ("月報" + adjustedStart.toString("YYYYMM"), ExcelUtility.createMonthlyReport(monitor, adjustedStart))

          }

        Ok.sendFile(excelFile, fileName = _ =>
          play.utils.UriEncoding.encodePathSegment(title + ".xlsx", "UTF-8"),
          onClose = () => { Files.deleteIfExists(excelFile.toPath()) })
      }
  }

  def monitorMonthlyHourReport = Security.Authenticated {
    Ok(views.html.monitorMonthlyHourReport())
  }

  def monthlyHourReport(monitorStr: String, monitorTypeStr: String, startDate: Long, outputTypeStr: String) = Security.Authenticated {
    val monitor = Monitor.withName(java.net.URLDecoder.decode(monitorStr, "UTF-8"))
    val mt = MonitorType.withName(java.net.URLDecoder.decode(monitorTypeStr, "UTF-8"))
    val start = {
      val original = new DateTime(startDate)
      original.withDayOfMonth(1).withMillisOfDay(0)
    }
    val outputType = OutputType.withName(outputTypeStr)
    val recordList = Record.getRecordMap(Record.HourCollection)(List(mt), monitor, start, start + 1.month)(mt)
    val timePair = recordList.map { r => r.time -> r }
    val timeMap = Map(timePair: _*)

    def getHourPeriodStat(records: Seq[Record], hourList: List[DateTime]) = {
      if (records.length == 0)
        Stat(None, None, None, 0, 0, 0)
      else {
        val values = records.map { r => r.value }
        val min = values.min
        val max = values.max
        val sum = values.sum
        val count = records.filter { r => MonitorStatus.isValid(r.status) }.length
        val total = new Duration(start, start + 1.month).getStandardDays.toInt
        val overCount = if (MonitorType.map(mt).std_law.isDefined) {
          values.count { _ > MonitorType.map(mt).std_law.get }
        } else
          0

        val avg = if (mt == MonitorType.WIN_DIRECTION) {
          val windDir = records
          val windSpeed = hourList.map(timeMap)
          Query.windAvg(windSpeed, windDir)
        } else {
          sum / total
        }
        Stat(
          avg = Some(avg),
          min = Some(min),
          max = Some(max),
          total = total,
          count = count,
          overCount = overCount)
      }
    }
    val hourValues =
      for {
        h <- 0 to 23
        hourList = Query.getPeriods(start + h.hour, start + 1.month, 1.day)
      } yield {
        h -> getHourPeriodStat(hourList.flatMap { timeMap.get }, hourList)
      }
    val hourStatMap = Map(hourValues: _*)
    val dayStatMap = Query.getPeriodStatReportMap(Map(mt -> recordList), 1.day)(start, start + 1.month)
    val overallStat = Query.getPeriodStatReportMap(Map(mt -> recordList), 1.day)(start, start + 1.month)(mt)(start)

    val title = "月份時報表"
    if (outputType == OutputType.html || outputType == OutputType.pdf) {
      Ok(views.html.monthlyHourReport(monitor, mt, start, timeMap, hourStatMap, dayStatMap(mt), overallStat))
    } else if (outputType == OutputType.json) {

      val hourColumn =
        for (t <- 0 to 23) yield s"${t}"

      val columns = "日\\小時" +: hourColumn :+ "平均" :+ "最大" :+ "最小" :+ "有效筆數" :+ "應測筆數" :+ "有效率(%)"

      val topRows = for {
        day <- Query.getPeriods(start, start + 1.month, 1.day)
      } yield {
        val date = CellData(day.toString("dd"), "")
        val values =
          for {
            h <- 0 to 23
            v = MonitorType.formatRecord(mt, timeMap.get(day + h.hour))
            styleStr = MonitorType.getCssClassStr(mt, timeMap.get(day + h.hour))
          } yield CellData(v, styleStr)
        val avg = CellData(MonitorType.format(mt, dayStatMap(mt)(day).avg), "")
        val max = CellData(MonitorType.format(mt, dayStatMap(mt)(day).max), "")
        val min = CellData(MonitorType.format(mt, dayStatMap(mt)(day).min), "")
        val count = CellData(dayStatMap(mt)(day).count.toString, "")
        val total = CellData(dayStatMap(mt)(day).total.toString, "")
        val effective = CellData(MonitorType.format(mt, dayStatMap(mt)(day).effectPercent), "")
        val cells = date +: values :+ avg :+ max :+ min :+ count :+ total :+ effective

        RowData(cells)
      }

      val avgRow = {
        val title = CellData("平均", "")
        val values =
          for {
            h <- 0 to 23
            v = MonitorType.format(mt, hourStatMap(h).avg)
          } yield CellData(v, "")
        val avg = CellData(MonitorType.format(mt, overallStat.avg), "")
        val max = CellData("", "")
        val min = CellData("", "")
        val count = CellData("", "")
        val total = CellData("", "")
        val effective = CellData("", "")
        val cells = title +: values :+ avg :+ max :+ min :+ count :+ total :+ effective
        RowData(cells)
      }

      val maxRow = {
        val title = CellData("最大", "")
        val values =
          for {
            h <- 0 to 23
            v = MonitorType.format(mt, hourStatMap(h).max)
          } yield CellData(v, "")
        val avg = CellData("", "")
        val max = CellData(MonitorType.format(mt, overallStat.max), "")
        val min = CellData("", "")
        val count = CellData("", "")
        val total = CellData("", "")
        val effective = CellData("", "")
        val cells = title +: values :+ avg :+ max :+ min :+ count :+ total :+ effective
        RowData(cells)
      }

      val minRow = {
        val title = CellData("最小", "")
        val values =
          for {
            h <- 0 to 23
            v = MonitorType.format(mt, hourStatMap(h).min)
          } yield CellData(v, "")
        val avg = CellData("", "")
        val max = CellData("", "")
        val min = CellData(MonitorType.format(mt, overallStat.min), "")
        val count = CellData("", "")
        val total = CellData("", "")
        val effective = CellData("", "")
        val cells = title +: values :+ avg :+ max :+ min :+ count :+ total :+ effective
        RowData(cells)
      }

      /*
      val hourCountRow = {
        val title = CellData("有效時數", "")
        val values =
          for {
            h <- 0 to 23
            v = hourStatMap(h).hour_count.get.toString
          } yield CellData(v, "")
        val avg = CellData("", "")
        val max = CellData("", "")
        val min = CellData("", "")
        val count = CellData(overallStat.hour_count.get.toString, "")
        val total = CellData("", "")
        val effective = CellData("", "")
        val cells = title +: values :+ avg :+ max :+ min :+ count :+ total :+ effective
        RowData(cells)
      }

      val hourTotalRow = {
        val title = CellData("應測時數", "")
        val values =
          for {
            h <- 0 to 23
            v = hourStatMap(h).hour_total.get.toString
          } yield CellData(v, "")
        val avg = CellData("", "")
        val max = CellData("", "")
        val min = CellData("", "")
        val count = CellData("", "")
        val total = CellData(overallStat.hour_total.get.toString, "")
        val effective = CellData("", "")
        val cells = title +: values :+ avg :+ max :+ min :+ count :+ total :+ effective
        RowData(cells)
      }

      val effectiveRow = {
        val title = CellData("有效率(%)", "")
        val values =
          for {
            h <- 0 to 23
            v = MonitorType.format(mt, hourStatMap(h).effectPercent)
          } yield CellData(v, "")
        val avg = CellData("", "")
        val max = CellData("", "")
        val min = CellData("", "")
        val count = CellData("", "")
        val total = CellData("", "")
        val effective = CellData(MonitorType.format(mt, overallStat.effectPercent), "")
        val cells = title +: values :+ avg :+ max :+ min :+ count :+ total :+ effective
        RowData(cells)
      }
*/
      val rows = topRows :+ avgRow :+ maxRow :+ minRow
      val tab = DataTab(columns, rows)
      Ok(Json.toJson(tab))
    } else {
      import java.io.File
      import java.nio.file.Files
      Ok("")
    }
  }

  def audit = Security.Authenticated {
    Ok(views.html.audit())
  }

  def auditReport(monitorStr: String, monitorTypeStr: String, startLong: Long, endLong: Long, outputTypeStr: String) = Security.Authenticated.async {
    implicit request =>
      import scala.collection.JavaConverters._
      val monitor = Monitor.withName(java.net.URLDecoder.decode(monitorStr, "UTF-8"))
      val monitorTypeStrArray = monitorTypeStr.split(':')
      val monitorTypes = monitorTypeStrArray.map { MonitorType.withName }
      val (start, end) = (new DateTime(startLong), new DateTime(endLong))
      val outputType = OutputType.withName(outputTypeStr)

      val recordMapF = Record.getAuditRecordMapFuture(Record.HourCollection)(monitorTypes.toList, monitor, start, end)
      for (recordMap <- recordMapF) yield {
        val timeList = recordMap.keys.toList.sorted

        val explain = monitorTypes.map { t =>
          val mtCase = MonitorType.map(t)
          s"${mtCase.desp}(${mtCase.unit})"
        }.mkString(",")

        def jsonOutput = {
          val mtColumns = for (mt <- monitorTypes) yield Seq(MonitorType.map(mt).desp, "註記理由")
          val mtColumn1 = mtColumns.flatMap(x => x)

          val columns = "時間" +: mtColumn1
          val rows = for (t <- timeList) yield {
            val timeC = CellData(t.toString("YYYY-MM-dd HH:mm"), "")
            val mtCells = for (mt <- monitorTypes) yield {
              val r = recordMap(t).get(mt)
              val c1 = CellData(MonitorType.formatRecord(mt, r), MonitorType.getCssClassStr(mt, r))
              val c2 = CellData(MonitorStatus.formatRecordExplain(r), "")
              Seq(c1, c2)
            }
            val mtCell1 = mtCells.flatMap(x => x)
            RowData(timeC +: mtCell1)
          }
          DataTab(columns, rows)
        }
        outputType match {
          case OutputType.html =>
            val output = views.html.auditReport(monitorTypes, explain, start, end, timeList, recordMap)
            Ok(output)
          case OutputType.json =>
            val tab = jsonOutput
            Ok(Json.toJson(tab))
        }
      }
  }

  def reauditReport(monitorStr: String, monitorTypeStr: String, startLong: Long, endLong: Long) = Security.Authenticated.async {
    implicit request =>
      import scala.collection.JavaConverters._
      val monitor = Monitor.withName(java.net.URLDecoder.decode(monitorStr, "UTF-8"))

      val monitorTypeStrArray = monitorTypeStr.split(':')
      val monitorTypes = monitorTypeStrArray.map { MonitorType.withName }
      val (start, end) = (new DateTime(startLong), new DateTime(endLong))

      val f = Record.resetAuditedRecord(Record.HourCollection)(monitorTypes.toList, monitor, start, end)
      waitReadyResult(f)

      val recordListF = Record.getRecordListFuture(Record.HourCollection)(monitor, start, end)
      val recordList = waitReadyResult(recordListF)
      AutoAudit.audit2(monitor, recordList, false)

      val recordMapF = Record.getAuditRecordMapFuture(Record.HourCollection)(monitorTypes.toList, monitor, start, end)
      for (recordMap <- recordMapF) yield {
        val timeList = recordMap.keys.toList.sorted

        val explain = monitorTypes.map { t =>
          val mtCase = MonitorType.map(t)
          s"${mtCase.desp}(${mtCase.unit})"
        }.mkString(",")
        val output = views.html.auditReport(monitorTypes, explain, start, end, timeList, recordMap)
        Ok(output)

      }
  }

}