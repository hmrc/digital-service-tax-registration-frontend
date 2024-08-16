/*
 * Copyright 2024 HM Revenue & Customs
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

package models

import config.FrontendAppConfig
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import play.api.Environment
import play.api.libs.json.{JsArray, Json}

import java.io.ByteArrayInputStream

class LocationSpec extends AnyFlatSpec with Matchers with MockitoSugar with BeforeAndAfterEach {

  val appConfig: FrontendAppConfig = mock[FrontendAppConfig]
  val env: Environment = mock[Environment]

  override def beforeEach(): Unit = {
    reset(appConfig, env)
    super.beforeEach()
  }

  "Location" should "filter out Alpha-3 codes. Accepting only Alpha-2 codes" in {
    when(appConfig.canonicalList).thenReturn("location-autocomplete-canonical-list.json")

    val countries: JsArray = Json.arr(
      Json.obj("type" -> "country", "code" -> "AD", "name" -> "Andorra"),
      Json.obj("type" -> "country", "code" -> "ZW", "name" -> "Zimbabwe")
    )

    val is = new ByteArrayInputStream(countries.toString.getBytes)
    when(env.resourceAsStream(any())).thenReturn(Some(is))

    val location = new Location(env, appConfig)
    val locations: Seq[Country] = location.locations
    locations.length mustBe 2
    locations       mustNot contain(Country("Akrotiri", "XQZ", "territory"))
  }

  "Location" must "throw an exception when the countries json file is missing" in {
    when(appConfig.canonicalList).thenReturn("countries.json")
    when(env.resourceAsStream(any())).thenReturn(None)

    val location = new Location(env, appConfig)

    val exception = intercept[Exception](location.locations)
    exception.getMessage mustEqual "location-autocomplete-canonical-list.json file is missing"
  }

}
