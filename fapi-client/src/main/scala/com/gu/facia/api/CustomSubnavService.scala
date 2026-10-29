package com.gu.facia.api

import com.gu.contentapi.client.model.v1.Content
import com.gu.facia.api.models.PublicationStatus
import com.gu.facia.api.models.PublicationStatus.{Draft, Live}
import com.gu.facia.client.models.TargetedPageType.{Article, Front, HasTag}
import com.gu.facia.client.models.{CustomSubnav, CustomSubnavConfig}

object CustomSubnavService {
  def getSubnavForFront(
      config: CustomSubnavConfig,
      frontId: String,
      status: PublicationStatus = Live
  ): Option[CustomSubnav] = {
    val draft = if (status == Draft) config.draft else Nil
    val all = draft ++ config.live
    all.find(_.pages.exists(p => p.path == frontId && p.`type` == Front))
  }

  def getSubnavForContent(
      config: CustomSubnavConfig,
      content: Content,
      status: PublicationStatus = Live
  ): Option[CustomSubnav] = {
    def subnavMatchesSpecificArticle(subnav: CustomSubnav): Boolean =
      subnav.pages.exists(p => p.path == content.id && p.`type` == Article)
    def subnavMatchesOneOfTheArticleTag(subnav: CustomSubnav): Boolean =
      subnav.pages.exists(p => p.`type` == HasTag && content.tags.exists(_.id == p.path))

    val draft = if (status == Draft) config.draft else Nil
    val all = draft ++ config.live
    all.find { subnav =>
      subnavMatchesSpecificArticle(subnav) ||
      subnavMatchesOneOfTheArticleTag(subnav)
    }
  }
}
