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
import net.liftweb.json.JsonAST.JValue
import net.liftweb.json.JsonParser

trait GoogleMapsServicesConfig {
  val AutocompleteUrl: String = Props.get("autocompleteurl", "https://maps.googleapis.com/maps/api/place/autocomplete/json?")
  val PlaceDetailsUrl: String = Props.get("placedetailurl", "https://maps.googleapis.com/maps/api/place/details/json?")
  val StandartUrlOptions = List(TypesGeocode, Language, SensorFalse, ApiKey)

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
object ApiKey extends UrlOption("key", Props.get("mapsApiKey", "[INSERTTHEKEYHERE]"))

case class Prediction(description: String, id: String, reference: String, types: List[String])

case class PredictionsResult(predictions: List[Prediction], status: String)

case class AddressComponent(long_name: String, short_name: String, types: List[String])

case class Geometry(location: Location)

case class DetailsResult(address_components: List[AddressComponent], formatted_address: String, geometry: Geometry)

case class PlaceDetails(result: DetailsResult, status: String)

/*
  WIP this will be changed to use Finagle

object GoogleMapsServicesManager extends GoogleMapsServicesConfig {

  object asJson extends (client.Response => JValue) {
    def apply(r: client.Response) = JsonParser.parse(r.getResponseBody)
  }

  def GetPredictions(input: String) = {
    val inputOption = UrlOption("input", input)
    val svc = url(AutocompleteUrl + UrlOptionsToString(inputOption :: StandartUrlOptions))
    val json: Promise[JValue] = Http(svc > asJson)
    json.onComplete(e => println(e.right))
    implicit val formats = DefaultFormats
    val res = json.map(_.extract[PredictionsResult])()
    println(res)
    res
  }

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
}
*/