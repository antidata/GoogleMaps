package com.github.antidata.GoogleMaps

/**
* This file is part of Lift GoogleMaps Integration. Lift GoogleMaps Integration is free software: you can
* redistribute it and/or modify it under the terms of the GNU General Public
* License as published by the Free Software Foundation, version 2.
*
* This program is distributed in the hope that it will be useful, but WITHOUT
* ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
* FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
* details.
*
* You should have received a copy of the GNU General Public License along with
* this program; if not, write to the Free Software Foundation, Inc., 51
* Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*
* Copyright M. Lucchetta - 2013
*/

import net.liftweb.util.Props
import net.liftweb.json.JsonAST.JValue
import net.liftweb.json.{DefaultFormats, JsonParser}
import com.twitter.finagle.http._
import com.twitter.finagle.http.Http
import org.jboss.netty.handler.codec.http._
import com.twitter.util.{Closable, Future}
import com.twitter.finagle.ServiceFactory
import scala.concurrent.future
import com.twitter.finagle.builder.ClientBuilder
import com.twitter.conversions.time._
import net.liftweb.common.{Empty, Box}

trait GoogleMapsServicesConfig {
  val AutocompleteUrl: String = Props.get("autocompleteurl", "maps.googleapis.com:443")
  val PlaceDetailsUrl: String = Props.get("placedetailurl", "maps.googleapis.com:443")
  val StandardUrlOptions = List(TypesGeocode, Language, SensorFalse, ApiKey)
  val ApiUrl = "maps.googleapis.com"

  def UrlOptionsToString(list: List[UrlOption]): String = {
    list map (s => s.name + "=" + s.value) mkString ("&")
  }
}

case class UrlOption(name: String, value: String)

object TypesGeocode extends UrlOption("types", "geocode")

object Language extends UrlOption("language", "en")

object SensorTrue extends UrlOption("sensor", "true")

object SensorFalse extends UrlOption("sensor", "false")

// Here we load the api key, additional keys can be defined
object ApiKey extends UrlOption("key", Props.get("mapsApiKey", /*"YourKeyHereOrInPropFile"*/"AIzaSyCHcbjBs0ijkzKDGc-mKV0w6DCR_b1kaAY"))

case class Prediction(description: String, id: String, reference: String, types: List[String])

case class PredictionsResult(predictions: List[Prediction], status: String)

case class AddressComponent(long_name: String, short_name: String, types: List[String])

case class Geometry(location: Location)

case class DetailsResult(address_components: List[AddressComponent], formatted_address: String, geometry: Geometry)

case class PlaceDetails(result: DetailsResult, status: String)

object GoogleMapsServicesManager extends GoogleMapsServicesConfig {

  private def createFactory(host : String, tls : String) : ServiceFactory[HttpRequest, HttpResponse] = {
    ClientBuilder()
      .codec(Http())
      .tcpConnectTimeout(5.seconds)
      .hosts(host)
      .hostConnectionLimit(10)
      .requestTimeout(55.seconds).tls(tls).tlsWithoutValidation()
      .buildFactory()
  }

  val autocompleteClient = createFactory(AutocompleteUrl, ApiUrl)

  val detailsClient = createFactory(PlaceDetailsUrl, ApiUrl)

  def init {}

  private object asJson extends (String => JValue) {
    def apply(r: String) = JsonParser.parse(r)
  }

  def GetPlaces[A](search : String, successFunc : PredictionsResult => A, failureFunc : Box[Throwable => A]= Empty) {
    val inputOption = UrlOption("input", search.replace(" ", "+"))

    val url = s"https://maps.googleapis.com/maps/api/place/autocomplete/json?${UrlOptionsToString(inputOption :: StandardUrlOptions)}"

    val newReq = RequestBuilder().url(url).buildGet()
    val client = autocompleteClient()()
    val response: Future[HttpResponse] = client(newReq)

    ManageResponse(successFunc, failureFunc)(response, client)
  }

  def GetGeolocations[A](search : String, successFunc : PlaceDetails => A, failureFunc : Box[Throwable => A]= Empty) {
    val inputOption = UrlOption("reference", search)

    val url = s"https://maps.googleapis.com/maps/api/place/details/json?${UrlOptionsToString(List(ApiKey, SensorFalse, inputOption))}"

    val newReq = RequestBuilder().url(url).buildGet()
    val client = detailsClient()()
    val response: Future[HttpResponse] = client(newReq)

    ManageResponse(successFunc, failureFunc)(response, client)
  }

  private def ManageResponse[A,B](successFun : A => B, failureFun : Box[Throwable => B])
                                 (response : Future[HttpResponse], client : Closable)
                                 (implicit mf : Manifest[A]) {

    response onSuccess { resp: HttpResponse =>
      implicit val formats = DefaultFormats
      val json = asJson(resp.getContent.toString("UTF-8")).extractOpt[A](formats, mf)
      client.close()
      json match {
        case Some(cc) => successFun(cc)
        case _ => failureFun map(_(new Exception("Cannot extract json to case class")))
      }
    }

    response onFailure { ex : Throwable =>
      client.close()
      failureFun map(_(ex))
    }
  }
}
