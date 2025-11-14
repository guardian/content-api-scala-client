import io.circe.syntax._
import java.io.FileWriter
import java.nio.charset.StandardCharsets
import java.time.Instant
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.Future

import com.gu.contentapi.client._
import com.gu.contentapi.client.model._
import com.gu.contentapi.client.model.v1.Content


object FindTags {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  def main(args: Array[String]): Unit = {
    val capiKey = args(0)
    val outputFilename = args(1)

    val client = GuardianContentClient(capiKey)
    val tags = Await.result(commissioningDeskTags(client), 5.seconds)
    println(s"Got ${tags.size} commissioning desk tags. Finding the monthly usage for them.")
    val tagUsages: TagUsages = Await.result(
      tags.foldLeft(Future.successful(Map.empty[String, Seq[Int]])) {
        case (future, tag) => future.flatMap(resultsSoFar => {
        val query = SearchQuery().tag(tag).pageSize(200)
          yearUsages(client, query).map(results => resultsSoFar.concat(Seq(tag -> results.map(_.size))))
      })
      },
      60.seconds
    )
    val writer = new FileWriter(outputFilename, StandardCharsets.UTF_8)
    try {
      writer.write(tagUsages.asJson.spaces2)
    } finally {
      writer.close()
    }
  }
  
  type TagUsages = Map[String, Seq[Int]]

  def commissioningDeskTags(client: GuardianContentClient): Future[Set[String]] = {
    client.paginateFold(TagsQuery().tagType("tracking"))(Set.empty[String]) {
      case (response, results) => results.union(Set.from(response.results.map(_.id)))
    }
  }

  def yearUsages(client: GuardianContentClient, query: SearchQuery): Future[Seq[Set[String]]] = {
    Future.traverse(ranges) {
      case (from, to) => monthUsages(client, query, from, to)
    }
  }

  def monthUsages(client: GuardianContentClient, query: SearchQuery, from: Instant, to: Instant): Future[Set[String]] = {
    client.paginateFold(query.fromDate(from).toDate(to))(Set.empty[String]){
      case (response, results) => results.union(Set.from(response.results.map(_.id)))
    }
  }

  /** (from, to) */
  val ranges: Seq[(Instant, Instant)] = Seq(
    "2024-11-01" -> "2024-11-30",
    "2024-12-01" -> "2024-12-31",
    "2025-01-01" -> "2025-01-31",
    "2025-02-01" -> "2025-02-28",
    "2025-03-01" -> "2025-03-31",
    "2025-04-01" -> "2025-04-30",
    "2025-05-01" -> "2025-05-31",
    "2025-06-01" -> "2025-06-30",
    "2025-07-01" -> "2025-07-31",
    "2025-08-01" -> "2025-08-31",
    "2025-09-01" -> "2025-09-30",
    "2025-10-01" -> "2025-10-31",
  ).map {
    case (from, to) => {
      val startOfDay = "T00:00:00Z"
      val endOfDay = "T23:59:59Z"
      (Instant.parse(from + startOfDay), Instant.parse(to + endOfDay))
    }
  }

}
