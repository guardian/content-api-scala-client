package com.gu.contentapi.client.utils.format

sealed trait Theme

sealed trait Pillar extends Theme
sealed trait Special extends Theme

case object NewsPillar extends Pillar
case object OpinionPillar extends Pillar
case object SportPillar extends Pillar
case object CulturePillar extends Pillar
case object LifestylePillar extends Pillar
case object SpecialReportTheme extends Special
case object SpecialReportAltTheme extends Special
case object Labs extends Special
