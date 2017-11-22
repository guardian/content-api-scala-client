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

