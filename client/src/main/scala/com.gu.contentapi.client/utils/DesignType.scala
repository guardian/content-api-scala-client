package com.gu.contentapi.client.utils

sealed trait DesignType

case object Article extends DesignType
case object Immersive extends DesignType
case object Media extends DesignType
case object Review extends DesignType
case object Analysis extends DesignType
case object Comment extends DesignType
case object Feature extends DesignType
case object Live extends DesignType
case object SpecialReport extends DesignType
case object Recipe extends DesignType
case object MatchReport extends DesignType
case object Interview extends DesignType
case object GuardianView extends DesignType
case object GuardianLabs extends DesignType
case object Quiz extends DesignType
case object AdvertisementFeature extends DesignType
case object NewsletterSignup extends DesignType
