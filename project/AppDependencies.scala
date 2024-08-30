import sbt._

object AppDependencies {

  private val bootstrapVersion = "9.0.0"
  private val hmrcMongoVersion = "2.1.0"

  val compile = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"       %% "play-frontend-hmrc-play-30"    % "10.4.0",
    "uk.gov.hmrc"       %% "bootstrap-frontend-play-30"    % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"            % hmrcMongoVersion,
    "com.chuusai"       %% "shapeless"                     % "2.4.0-M1",
    "org.typelevel"     %% "cats-core"                     % "2.12.0",
    "com.beachape"      %% "enumeratum-play-json"          % "1.8.1",
    "com.beachape"      %% "enumeratum"                    % "1.7.4"
  )

  val test = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-30"  % bootstrapVersion,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-test-play-30" % hmrcMongoVersion,
    "org.scalatestplus"       %% "scalacheck-1-17"         % "3.2.18.0",
    "wolfendale"              %% "scalacheck-gen-regexp"   % "0.1.2",
    "io.chrisdavenport"       %% "cats-scalacheck"         % "0.3.2"
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test
}
