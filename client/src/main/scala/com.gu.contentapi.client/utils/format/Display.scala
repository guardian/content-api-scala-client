package com.gu.contentapi.client.utils.format

sealed trait Display

case object StandardDisplay extends Display
case object ImmersiveDisplay extends Display
case object ShowcaseDisplay extends Display
case object NumberedListDisplay extends Display
case object ColumnDisplay extends Display
