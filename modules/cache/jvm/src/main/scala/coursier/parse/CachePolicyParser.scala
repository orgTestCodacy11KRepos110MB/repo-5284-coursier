package coursier.parse

import coursier.cache.CachePolicy
import coursier.util.ValidationNel
import coursier.util.Traverse.TraverseOps

object CachePolicyParser {

  def cachePolicy(input: String): Either[String, CachePolicy] =
    input match {
      case "offline" =>
        Right(CachePolicy.LocalOnly)
      case "offline-no-changing" =>
        Right(CachePolicy.NoChanging.LocalOnly)
      case "local" =>
        Right(CachePolicy.LocalOnlyIfValid)
      case "update-local-changing" =>
        Right(CachePolicy.LocalUpdateChanging)
      case "update-local" =>
        Right(CachePolicy.LocalUpdate)
      case "update-local-no-changing" =>
        Right(CachePolicy.NoChanging.LocalUpdate)
      case "update-changing" =>
        Right(CachePolicy.UpdateChanging)
      case "update" =>
        Right(CachePolicy.Update)
      case "missing" =>
        Right(CachePolicy.FetchMissing)
      case "missing-no-changing" =>
        Right(CachePolicy.NoChanging.FetchMissing)
      case "force" =>
        Right(CachePolicy.ForceDownload)
      case "force-no-changing" =>
        Right(CachePolicy.NoChanging.ForceDownload)
      case other =>
        Left(s"Unrecognized cache policy: $other")
    }

  private def cachePolicies0(
    input: String,
    default: Option[Seq[CachePolicy]]
  ): ValidationNel[String, Seq[CachePolicy]] =
    input
      .split(',')
      .toVector
      .validationNelTraverse[String, Seq[CachePolicy]] {
        case "default" if default.nonEmpty =>
          ValidationNel.success(default.getOrElse(Nil))
        case "pure" | "no-changing" if default.nonEmpty =>
          ValidationNel.success(default.getOrElse(Nil).map(_.rejectChanging).distinct)
        case other =>
          ValidationNel.fromEither(cachePolicy(other).map(Seq(_)))
      }
      .map(_.flatten)

  def cachePolicies(input: String): ValidationNel[String, Seq[CachePolicy]] =
    cachePolicies0(input, None)

  def cachePolicies(
    input: String,
    default: Seq[CachePolicy]
  ): ValidationNel[String, Seq[CachePolicy]] =
    cachePolicies0(input, Some(default))

}
