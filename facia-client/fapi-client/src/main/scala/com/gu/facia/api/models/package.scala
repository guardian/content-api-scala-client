package com.gu.facia.api

import com.gu.commercial.branding.Branding

package object models {
  type BrandingByEdition = Map[String, Option[Branding]]
}
