package com.gu.openplatform.contentapi.parser

import scala.xml._
import org.joda.time.format.ISODateTimeFormat
import com.gu.openplatform.contentapi.model._
import java.net.URL

object XmlParser {

  def parseEndpoint(endpointName: String, xml: String) : Response = {
    parseEndpoint(endpointName, XML.loadString(xml))
  }

  def parseEndpoint(endpointName: String, xml: Elem) : Response = {
    endpointName match {
      case "search" => parseSearchEndpoint(xml)
      case "tags" => parseTagsEndpoint(xml)
      case "sections" => parseSectionsEndpoint(xml)
      case "id" => parseIdEndpoint(xml)
      case _ => throw new UnsupportedOperationException("unsupported endpoint " + endpointName)
    }
  }

  def parseSearchEndpoint(xml: String) : SearchResponse = {
    parseSearchEndpoint(XML.loadString(xml))
  }

  def parseSearchEndpoint(xml: Elem) : SearchResponse = {
    SearchResponse(
        "xml",
        (xml \ "@status" text),
        (xml \ "@user-tier" text),
        (xml \ "@start-index" text).toInt,
        (xml \ "@current-page" text).toInt,
        (xml \ "@pages" text).toInt,
        (xml \ "@page-size" text).toInt,
        (xml \ "@total" text).toInt,
        (xml \ "@order-by" text),
        (xml \ "results" \ "content" map { contentNode => parseContentNode(contentNode)}).toList,
        xml \ "refinement-groups" \ "refinement-group" length match {
          case 0 => None
          case _ => Some((xml \ "refinement-groups" \ "refinement-group" map {refinementGroupNode => parseRefinementGroupNode(refinementGroupNode)}).toList)
        }
      )
  }

  def parseTagsEndpoint(xml: String) : TagsResponse = {
    parseTagsEndpoint(XML.loadString(xml))
  }

  def parseTagsEndpoint(xml: Elem) : TagsResponse = {
    TagsResponse(
        "xml",
        (xml \ "@status" text),
        (xml \ "@user-tier" text),
        (xml \ "@start-index" text).toInt,
        (xml \ "@current-page" text).toInt,
        (xml \ "@pages" text).toInt,
        (xml \ "@page-size" text).toInt,
        (xml \ "@total" text).toInt,
        (xml \ "results" \ "tag" map { tagNode => parseTagNode(tagNode)}).toList
      )
  }

  def parseSectionsEndpoint(xml: String) : SectionsResponse = {
    parseSectionsEndpoint(XML.loadString(xml))
  }

  def parseSectionsEndpoint(xml: Elem) : SectionsResponse = {
    SectionsResponse(
        "xml",
        (xml \ "@status" text),
        (xml \ "@user-tier" text),
        (xml \ "@total" text).toInt,
        (xml \ "results" \ "section" map { sectionNode => parseSectionNode(sectionNode)}).toList
      )
  }

  def parseIdEndpoint(xml: String) : ItemResponse = {
    parseIdEndpoint(XML.loadString(xml))
  }

  def parseIdEndpoint(xml: Elem) : ItemResponse = {
    ItemResponse(
        "xml",
        (xml \ "@status" text),
        (xml \ "@user-tier" text),
        (xml \ "@start-index" size match {
          case 0 => None
          case 1 => Some((xml \ "@start-index" text)toInt)
        }),
        (xml \ "@current-page" size match {
          case 0 => None
          case 1 => Some((xml \ "@current-page" text)toInt)
        }),
        (xml \ "@pages" size match {
          case 0 => None
          case 1 => Some((xml \ "@pages" text)toInt)
        }),
        (xml \ "@page-size" size match {
          case 0 => None
          case 1 => Some((xml \ "@page-size" text)toInt)
        }),
        (xml \ "@total" size match {
          case 0 => None
          case 1 => Some((xml \ "@total" text)toInt)
        }),
        (xml \ "content" size match {
          case 0 => None
          case 1 => Some(parseContentNode(xml \ "content" head))
          case _ => throw new RuntimeException("more than 1 content item returned from id endpoint")
        }),
        (xml \ "tag" size match {
          case 0 => None
          case 1 => Some(parseTagNode(xml \ "tag" head))
          case _ => throw new RuntimeException("more than 1 tag returned from id endpoint")
        }),
        (xml \ "section" size match {
          case 0 => None
          case 1 => Some(parseSectionNode(xml \ "section" head))
          case _ => throw new RuntimeException("more than 1 section returned from id endpoint")
        }),
        (xml \ "results" \ "content" map { contentNode => parseContentNode(contentNode)}).toList
      )
  }

  def parseContentNode(contentNode: Node) : Content = {
    Content(
        contentNode \ "@id" text,
        contentNode \ "@section-id" size match {
          case 0 => None
          case 1 => Some(contentNode \ "@section-id" text)
        },
        contentNode \ "@section-name" size match {
          case 0 => None
          case 1 => Some(contentNode \ "@section-name" text)
        },
        ISODateTimeFormat.dateTimeNoMillis.parseDateTime(contentNode \ "@web-publication-date" text),
        contentNode \ "@web-title" text,
        new URL(contentNode \ "@web-url" text),
        new URL(contentNode \ "@api-url" text),
        parseFields(contentNode),
        contentNode \ "tags" \ "tag" length match {
          case 0 => None
          case _ => Some((contentNode \ "tags" \ "tag" map {tagNode => parseTagNode(tagNode)}).toList)
        },
        contentNode \ "factboxes" \ "factbox" length match {
          case 0 => None
          case _ => Some((contentNode \ "factboxes" \ "factbox" map {factboxNode => parseFactboxNode(factboxNode)}).toList)
        },
        contentNode \ "media-assets" \ "asset" length match {
          case 0 => None
          case _ => Some((contentNode \ "media-assets" \ "asset" map {mediaAssetNode => parseMediaAssetNode(mediaAssetNode)}).toList)
        }
    )
  }

  def parseFields(xmlItem: Node) : Option[Map[String,String]] = {
    val fields = xmlItem \ "fields" \ "field"

    fields.length match {
      case 0 => None
      case _ => Some(
        Map.empty[String, String] ++
                ( fields map { xmlField => (camelCase(xmlField \ "@name" text) , xmlField text) } )
      )
    }
  }

  def camelCase(text: String) = text split("-") reduceLeft(_ + _.capitalize)

  def parseTagNode(tagNode: Node) : Tag = {
    Tag(
      tagNode \ "@id" text,
      tagNode \ "@type" text,
      tagNode \ "@section-id" size match {
        case 0 => None
        case 1 => Some(tagNode \ "@section-id" text)
      },
      tagNode \ "@section-name" size match {
        case 0 => None
        case 1 => Some(tagNode \ "@section-name" text)
      },
      tagNode \ "@web-title" text,
      new URL(tagNode \ "@web-url" text),
      new URL(tagNode \ "@api-url" text)
    )
  }

  def parseSectionNode(sectionNode: Node) : Section = {
    Section(
      sectionNode \ "@id" text,
      sectionNode \ "@web-title" text,
      new URL(sectionNode \ "@web-url" text),
      new URL(sectionNode \ "@api-url" text)
    )
  }

  def parseFactboxNode(factboxNode: Node) : Factbox = {
    Factbox(
      factboxNode \ "@heading" size match {
        case 0 => None
        case 1 => Some(factboxNode \ "@heading" text)
      },
      factboxNode \ "@type" text,
      factboxNode \ "@picture" size match {
        case 0 => None
        case 1 => Some(factboxNode \ "@picture" text)
      },
      parseFields(factboxNode)
    )
  }

  def parseMediaAssetNode(mediaAssetNode: Node) : MediaAsset = {
    MediaAsset(
      mediaAssetNode \ "@type" text,
      mediaAssetNode \ "@rel" text,
      (mediaAssetNode \ "@index" text).toInt,
      mediaAssetNode \ "@file" text,
      parseFields(mediaAssetNode)
    )
  }

  def parseRefinementGroupNode(refinementGroupNode: Node) : RefinementGroup = {
    RefinementGroup(
      refinementGroupNode \ "@type" text,
      (refinementGroupNode \ "refinements" \ "refinement" map { refinementNode => parseRefinementNode(refinementNode)}).toList
    )
  }

  def parseRefinementNode(refinementNode: Node) : Refinement = {
    Refinement(
      (refinementNode \ "@count" text).toInt,
      new URL(refinementNode \ "@refined-url" text),
      refinementNode \ "@display-name" text,
      refinementNode \ "@id" text,
      new URL(refinementNode \ "@api-url" text)
    )
  }
}
