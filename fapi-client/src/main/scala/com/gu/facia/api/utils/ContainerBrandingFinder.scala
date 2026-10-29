package com.gu.facia.api.utils

import com.gu.contentapi.client.model.v1.{Content, Section, Tag}
import com.gu.facia.api.models.CollectionConfig
import com.gu.facia.client.models.Branded
import com.gu.commercial.branding.{
  BrandingFinder,
  Brandable,
  Branding,
  ContainerBranding,
  PaidMultiSponsorBranding,
  PaidContent
}

object ContainerBrandingFinder {

  /** Finds branding of a set of content items, tags and sections in a container.
    *
    * @param config
    *   Configuration of container holding this content
    * @param items
    *   Content items with <code>section</code>, <code>isInappropriateForSponsorship</code> field and all
    *   <code>tags</code> populated
    * @param edition
    *   in lowercase String format eg. <code>uk</code>
    */
  def findBranding(
      config: CollectionConfig,
      edition: String,
      items: Set[Content],
      tags: Set[Tag] = Set.empty,
      sections: Set[Section] = Set.empty
  ): Option[ContainerBranding] = {
    def toBranding[T: Brandable](brandable: T) =
      BrandingFinder.findBranding(edition)(brandable)
    findBranding(
      isConfiguredForBranding = config.metadata.exists(_.contains(Branded)),
      optBrandings = items.map(toBranding(_)) ++ tags.map(toBranding(_)) ++ sections.map(
        toBranding(_)
      )
    )
  }

  def findBranding(
      isConfiguredForBranding: Boolean,
      optBrandings: Set[Option[Branding]]
  ): Option[ContainerBranding] = {

    def findCommonBranding(brandings: Set[Branding]) =
      if (brandings.size == 1) brandings.headOption else None

    def areAllPaidContent(brandings: Set[Branding]) =
      brandings.forall(_.brandingType == PaidContent)

    if (
      isConfiguredForBranding && optBrandings.nonEmpty && optBrandings.forall(
        _.nonEmpty
      )
    ) {
      val brandings = optBrandings.flatten
      findCommonBranding(brandings) orElse {
        if (areAllPaidContent(brandings)) Some(PaidMultiSponsorBranding)
        else None
      }
    } else None
  }
}
