package com.github.antidata.snippet.roundtrips

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

import com.github.antidata.lib.EmptyRoundTrip
import net.liftweb.json.JsonAST.{JDouble, JString, JArray, JValue}
import net.liftweb.http.{RoundTripInfo, RoundTripHandlerFunc}
import com.github.antidata.GoogleMaps.{PlaceDetails, GoogleMapsServicesManager, PredictionsResult}

trait ServiceRoundTrips extends EmptyRoundTrip {

  protected def findPlaces(value : JValue, func : RoundTripHandlerFunc) {

    def sendJArrayToBrowser(pred : PredictionsResult) : Unit = {
      import net.liftweb.json.JsonDSL._
      func.send(JArray(pred.predictions.map(p => ("name" -> p.description) ~ ("ref" -> p.reference)).toList))
    }

    val ter = value.values.toString
    GoogleMapsServicesManager.GetPlaces(ter, sendJArrayToBrowser)
  }

  protected def getDetails(value : JValue, func : RoundTripHandlerFunc) {

    def sendDetailsToBrowser(json : PlaceDetails) : Unit = {
      import net.liftweb.json.JsonDSL._
      func.send(JArray(List(
        ("address" -> JString(json.result.formatted_address)) ~
          ("components" -> JArray(json.result.address_components.map(s => JString(s.long_name)).toList)) ~
          ("location" -> ("lat" -> JDouble(json.result.geometry.location.lat)) ~ ("long" -> JDouble(json.result.geometry.location.lng)))
      )))
    }
    val ter = value.values.toString
    GoogleMapsServicesManager.GetGeolocations(ter, sendDetailsToBrowser)
  }
  private val roundtrips : List[RoundTripInfo] = List("findPlaces" -> findPlaces _, "placeDetail" -> getDetails _)
  abstract override def getRoundTrips = super.getRoundTrips ++ roundtrips
}
