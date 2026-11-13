package com.gu.facia.api.utils

import java.net.URI

import scala.util.Try

object ExternalLinks {
  val origins = List(
    "http://www.theguardian.com",
    "https://www.theguardian.com",
    "https://profile.theguardian.com",
    "https://composer.gutools.co.uk",
    "https://composer.release.dev-gutools.co.uk",
    "https://composer.code.dev-gutools.co.uk",
    "http://preview.gutools.co.uk",
    "https://preview.gutools.co.uk"
  )

  val guardianDomains: List[String] = origins flatMap { uri =>
    Try {
      new URI(uri).getHost.stripPrefix("www.")
    }.toOption
  }

  def external(url: String): Boolean =
    Try(
      Option(new URI(url).getHost)
        .exists({ host =>
          !guardianDomains.exists({ domain =>
            host == domain || host.endsWith(s".$domain")
          })
        })
    )
      .getOrElse(false)

  def internalPath(url: String) =
    if (external(url))
      None
    else
      Try {
        Option(new URI(url).getPath)
      }.toOption.flatten
}
