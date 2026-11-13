package com.gu.facia.api.utils

import com.gu.commercial.branding.{Branding, Logo, PaidMultiSponsorBranding, Sponsored}
import com.gu.contentapi.client.model.v1.Content
import com.gu.facia.api.TestModel.{getContentItem, getTag}
import com.gu.facia.api.models.CollectionConfig
import com.gu.facia.api.utils.ContainerBrandingFinder._
import com.gu.facia.client.models.Branded
import org.scalatest.OptionValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ContainerBrandingFinderTest extends AnyFlatSpec with Matchers with OptionValues {

  private def getTagBrandedItem = getContentItem("TagBrandedContent.json")
  private def getMultipleTagBrandedItem = getContentItem(
    "TagBrandedContent-MultipleBrands.json"
  )
  private def getSectionBrandedItem = getContentItem(
    "SectionBrandedContent.json"
  )
  private def getAfterDateTargetedTagBrandedItem = getContentItem(
    "AfterDateTargetedTagBrandedContent.json"
  )
  private def getInappropriateItem = getContentItem("InappropriateContent.json")
  private def getPaidItem = getContentItem("PaidContent.json")
  private def getFoundationTag = getTag("FoundationTag.json")
  private def getFoundationTag2 = getTag("FoundationTag2.json")
  private def getPaidTag = getTag("PaidTag.json")
  private def getPaidTag2 = getTag("PaidTag2.json")
  private def getSeriesTag = getTag("SeriesTag.json")

  private val brandedContainerConfig =
    CollectionConfig.empty.copy(metadata = Some(List(Branded)))

  "findBranding: content" should "give branding if all items in set have same branding" in {
    val items = Set(
      getTagBrandedItem,
      getMultipleTagBrandedItem
    )
    val branding = findBranding(brandedContainerConfig, "uk", items)
    branding.value should be(
      Branding(
        brandingType = Sponsored,
        sponsorName = "Fairtrade Foundation",
        logo = Logo(
          src = "https://static.theguardian.com/commercial/sponsor/sustainable/series/spotlight-commodities/logo.png",
          dimensions = None,
          link = "http://www.fairtrade.org.uk/",
          label = "Supported by"
        ),
        logoForDarkBackground = None,
        aboutThisLink = "https://www.theguardian.com/uk",
        hostedCampaignColour = None
      )
    )
  }

  it should "give no branding if any item in set has different branding" in {
    val items = Set(
      getTagBrandedItem,
      getSectionBrandedItem
    )
    val branding = findBranding(brandedContainerConfig, "uk", items)
    branding should be(None)
  }

  it should "give no branding if any item in set has no branding" in {
    val items = Set(
      getTagBrandedItem,
      getMultipleTagBrandedItem,
      getInappropriateItem
    )
    val branding = findBranding(brandedContainerConfig, "uk", items)
    branding should be(None)
  }

  it should "give no branding for an empty set" in {
    val branding =
      findBranding(brandedContainerConfig, "uk", Set.empty[Content])
    branding should be(None)
  }

  it should "give no branding for a container without branded config" in {
    val items = Set(
      getTagBrandedItem,
      getMultipleTagBrandedItem
    )
    val branding = findBranding(CollectionConfig.empty, "uk", items)
    branding should be(None)
  }

  it should "give paid container branding for a multi-sponsor paid container" in {
    val items = Set(
      getAfterDateTargetedTagBrandedItem,
      getPaidItem
    )
    val branding = findBranding(brandedContainerConfig, "uk", items)
    branding.value should be(PaidMultiSponsorBranding)
  }

  "findBranding: tags" should "give branding if all tags have same branding" in {
    val tags = Set(
      getFoundationTag,
      getFoundationTag2
    )
    val branding =
      findBranding(brandedContainerConfig, "uk", items = Set.empty, tags)
    branding.value should be(
      Branding(
        brandingType = Sponsored,
        sponsorName = "Fairtrade Foundation",
        logo = Logo(
          src = "https://static.theguardian.com/commercial/sponsor/sustainable/series/spotlight-commodities/logo.png",
          dimensions = None,
          link = "http://www.fairtrade.org.uk/",
          label = "Supported by"
        ),
        logoForDarkBackground = None,
        aboutThisLink = "https://www.theguardian.com/uk",
        hostedCampaignColour = None
      )
    )
  }

  it should "give no branding if any tag in set has no branding" in {
    val tags = Set(
      getFoundationTag,
      getSeriesTag
    )
    val branding =
      findBranding(brandedContainerConfig, "uk", items = Set.empty, tags)
    branding should be(None)
  }

  it should "give paid container branding for a multi-sponsor paid container full of tags" in {
    val tags = Set(
      getPaidTag,
      getPaidTag2
    )
    val branding =
      findBranding(brandedContainerConfig, "uk", items = Set.empty, tags)
    branding.value should be(PaidMultiSponsorBranding)
  }

  "findBranding: mixed" should "give branding if all tags and content in set have same branding" in {
    val items = Set(getTagBrandedItem)
    val tags = Set(getFoundationTag)
    val branding = findBranding(brandedContainerConfig, "uk", items, tags)
    branding.value should be(
      Branding(
        brandingType = Sponsored,
        sponsorName = "Fairtrade Foundation",
        logo = Logo(
          src = "https://static.theguardian.com/commercial/sponsor/sustainable/series/spotlight-commodities/logo.png",
          dimensions = None,
          link = "http://www.fairtrade.org.uk/",
          label = "Supported by"
        ),
        logoForDarkBackground = None,
        aboutThisLink = "https://www.theguardian.com/uk",
        hostedCampaignColour = None
      )
    )
  }

  "findBranding: mixed" should "give paid container branding for a multi-sponsor paid container holding a combination of tags and content" in {
    val items = Set(getPaidItem)
    val tags = Set(getPaidTag, getPaidTag2)
    val branding = findBranding(brandedContainerConfig, "uk", items, tags)
    branding.value should be(PaidMultiSponsorBranding)
  }
}
