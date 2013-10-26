package com.github.antidata.GoogleMaps

/**
# This file is part of Lift GoogleMaps Integration. Lift GoogleMaps Integration is free software: you can
# redistribute it and/or modify it under the terms of the GNU General Public
# License as published by the Free Software Foundation, version 2.
#
# This program is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
# details.
#
# You should have received a copy of the GNU General Public License along with
# this program; if not, write to the Free Software Foundation, Inc., 51
# Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
#
# Copyright M. Lucchetta - 2013
*/

import net.liftweb.util.Props
import net.liftweb.json.JsonAST.{JArray, JString, JValue}
import net.liftweb.json.{DefaultFormats, JsonParser}
import net.liftweb.http.RoundTripHandlerFunc
import com.twitter.finagle.http._
import com.twitter.finagle.http.Http
import org.jboss.netty.handler.codec.http._
import com.twitter.util.Future
import com.twitter.finagle.ServiceFactory
import com.twitter.finagle.builder.ClientBuilder
import com.twitter.conversions.time._
import net.liftweb.json.JsonDSL._

trait GoogleMapsServicesConfig {
  val AutocompleteUrl: String = Props.get("autocompleteurl", "maps.googleapis.com:443")
  val PlaceDetailsUrl: String = Props.get("placedetailurl", "maps.googleapis.com:443")
  val StandardUrlOptions = List(TypesGeocode, Language, SensorFalse, ApiKey)

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
object ApiKey extends UrlOption("key", Props.get("mapsApiKey", "AIzaSyCHcbjBs0ijkzKDGc-mKV0w6DCR_b1kaAY"))

case class Prediction(description: String, id: String, reference: String, types: List[String])

case class PredictionsResult(predictions: List[Prediction], status: String)

case class AddressComponent(long_name: String, short_name: String, types: List[String])

case class Geometry(location: Location)

case class DetailsResult(address_components: List[AddressComponent], formatted_address: String, geometry: Geometry)

case class PlaceDetails(result: DetailsResult, status: String)

object GoogleMapsServicesManager extends GoogleMapsServicesConfig {

  val clientt : ServiceFactory[HttpRequest, HttpResponse] = ClientBuilder()
    .codec(Http())
    .tcpConnectTimeout(5.seconds)
    .hosts(AutocompleteUrl)
    .hostConnectionLimit(10)
    .requestTimeout(55.seconds).tls("maps.googleapis.com").tlsWithoutValidation()
    .buildFactory()

  val clientDet : ServiceFactory[HttpRequest, HttpResponse] = ClientBuilder()
    .codec(Http())
    .tcpConnectTimeout(5.seconds)
    .hosts(PlaceDetailsUrl)
    .hostConnectionLimit(10)
    .requestTimeout(55.seconds).tls("maps.googleapis.com").tlsWithoutValidation()
    .buildFactory()

  def init = {}

  private object asJson extends (String => JValue) {
    def apply(r: String) = JsonParser.parse(r)
  }

  def GetPlaces(search : String, func : RoundTripHandlerFunc) {
    val inputOption = UrlOption("input", search)
    try{
    val newReq = RequestBuilder().url("https://maps.googleapis.com"+s"/maps/api/place/autocomplete/json?${UrlOptionsToString(inputOption :: StandardUrlOptions)}").buildGet()
    val client = clientt()()
    val response: Future[HttpResponse] = client(newReq)

    response onSuccess { resp: HttpResponse =>
      implicit val formats = DefaultFormats
      val json = asJson(resp.getContent.toString("UTF-8")).extract[PredictionsResult]
      func.send(JArray(json.predictions.map(p => ("name" -> p.description) ~ ("ref" -> p.reference)).toList))
      client.close()
    }

    response onFailure { ex =>
      client.close()
    }
    } catch {
      case ex=> println(ex)
    }

  }

  def GetGeolocations(search : String, func : RoundTripHandlerFunc) {
    val inputOption = UrlOption("reference", search)
    val newReq = RequestBuilder().url(s"https://maps.googleapis.com/maps/api/place/details/json?${UrlOptionsToString(List(ApiKey, SensorFalse, inputOption))}").buildGet()

    val client = clientDet()()
    val response: Future[HttpResponse] = client(newReq)

    response onSuccess { resp: HttpResponse =>
      implicit val formats = DefaultFormats
      val json = asJson(resp.getContent.toString("UTF-8")).extract[PlaceDetails]
      func.send(JArray(json.result.address_components.map(s => JString(s.short_name)).toList))
      client.close()
    }

    response onFailure { ex =>
      client.close()
    }
  }
/*
  def GetGeolocations(reference: String) = {
    val inputOption = UrlOption("reference", reference)
    val svc = url(PlaceDetailsUrl + UrlOptionsToString(List(ApiKey, SensorFalse, inputOption)))
    val json: Promise[JValue] = Http(svc > asJson)
    json.onComplete(e => println(e.right))
    implicit val formats = DefaultFormats
    val res = json.map(_.extract[PlaceDetails])()
    println(res)
    res
  }
  */
}
