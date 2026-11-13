package lib

trait ResourcesHelper {
  def slurp(path: String): Option[String] =
    Option(getClass.getClassLoader.getResource(path))
      .map(scala.io.Source.fromURL(_).mkString)

  def slurpOrDie(path: String) =
    slurp(path).getOrElse(
      throw new RuntimeException(s"Unable to load required resource '$path'")
    )
}
