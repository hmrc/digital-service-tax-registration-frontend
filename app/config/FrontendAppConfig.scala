/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package config

import com.google.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.i18n.Lang
import play.api.mvc.RequestHeader
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

@Singleton
class FrontendAppConfig @Inject() (configuration: Configuration, servicesConfig: ServicesConfig) {

  val host: String    = configuration.get[String]("host")
  val appName: String = configuration.get[String]("appName")

  private val contactHost                  = configuration.get[String]("contact-frontend.host")
  private val contactFormServiceIdentifier = "digital-service-tax-registration-frontend"
  val dstBackendBaseUrl: String            = servicesConfig.baseUrl("digital-services-tax")
  val dstFrontendBaseUrl: String           = servicesConfig.baseUrl("digital-services-tax-frontend")
  val dstFrontendRegistrationUrl: String   = dstFrontendBaseUrl + "/digital-services-tax/register/"

  def feedbackUrl(implicit request: RequestHeader): String =
    s"$contactHost/contact/beta-feedback?service=$contactFormServiceIdentifier&backUrl=${host + request.uri}"

  val loginUrl: String             = configuration.get[String]("urls.login")
  val loginContinueUrl: String     = configuration.get[String]("urls.loginContinue")
  val signOutUrl: String           = configuration.get[String]("urls.signOut")
  val findLostUtr: String          = configuration.get[String]("urls.findLostUtr")
  val vatRegisteringGroups: String = configuration.get[String]("urls.vatRegisteringGroups")

  private val exitSurveyBaseUrl: String = configuration.get[String]("feedback-frontend.url")
  val exitSurveyUrl: String             = s"$exitSurveyBaseUrl/digital-service-tax-registration-frontend"

  val languageTranslationEnabled: Boolean =
    configuration.get[Boolean]("features.welsh-translation")

  def languageMap: Map[String, Lang] = Map(
    "en" -> Lang("en")
  )

  val timeout: Int   = configuration.get[Int]("timeout-dialog.timeout")
  val countdown: Int = configuration.get[Int]("timeout-dialog.countdown")

  val cacheTtl: Long = configuration.get[Int]("mongodb.timeToLiveInSeconds")

  val canonicalList: String = configuration.get[String]("location.canonical.list")

  lazy val dstNewRegistrationFrontendEnableFlag: Boolean =
    configuration.getOptional[Boolean]("features.dstNewRegistrationFrontendEnable").getOrElse(false)

  val companiesHouseLink = "https://www.gov.uk/file-changes-to-a-company-with-companies-house"
}
