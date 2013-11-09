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

import net.liftweb.common.Box
import net.liftweb.http.js.{JsCmd, JsExp}
import net.liftweb.util.Helpers.randomString
import net.liftweb.http.js.JE.{JsRaw, JsVar}
import net.liftweb.http.js.JsCmds.Function
import net.liftweb.http.SHtml
import xml.Node

// MapEntity is required to assign ids to all of the items to be created as javascript instances
trait MapEntity {
  lazy val id = "ME" + randomString(6)
}

// Location in the map
case class Location(lat: Double, lng: Double) extends JsExp {
  override lazy val toJsCmd = s"new google.maps.LatLng(${lat.toString}, ${lng.toString})"
  override def toString = toJsCmd
}

// LatLngBounds
case class LatLngBounds(southwest: Location, northeast: Location) extends JsExp {
  override lazy val toJsCmd = s"new google.maps.LatLngBounds(${southwest}, ${northeast})"
  override def toString = toJsCmd
}

// MapIndicator represents an item inside the map that indicates a place, street, region and  so on
abstract class MapIndicator extends JsCmd

// MapOption is an option or property to be set in a Map
class MapOption(val property: String, val value: Any) extends JsExp {
  override lazy val toJsCmd = s"${property}: ${value.toString}"
  override def toString = toJsCmd
  lazy val toProperty = s"""${property}: "${value.toString}" """
}

// MapConstant: a constant for the Map
class MapConstant(val value: String)

// Map represents the map instance to be placed in the html with the required options
case class Map(elemId: String, options: Options) extends JsExp with MapEntity {
  override lazy val toJsCmd = s"""new google.maps.Map(document.getElementById("${elemId}"), ${options.id});"""
  def setCenter(loc : Location) : JsCmd = JsRaw(s"${id}.setCenter(${loc}});").cmd
}

// Marker to put on the Map
case class Marker(mapVar: String, options: Options, optionalId : String = "") extends MapIndicator with JsExp with MapEntity {
  lazy val finalId = if(optionalId != "") optionalId else id
  override lazy val toJsCmd = {
    val instance = s"${finalId} = new google.maps.Marker(${options.toJsCmd}); ${finalId}.setMap(${mapVar})"
    optionalId match {
      case "" => s"var ${instance}"
      case _ => instance
    }
  }
  lazy val removeMarker: JsCmd = JsRaw(s"${finalId}.setMap(null)").cmd
}

// Options of the Map
case class Options(mapOptions: MapOption*) extends JsExp with MapEntity {
  override lazy val toJsCmd = mapOptions mkString("{", ",", "}")
  override def toString = toJsCmd
}

// Animation for map items
case class Animation(override val value: Any) extends MapOption("animation", value)

// Draggable if we want the item to be draggable
case class Draggable(override val value: Boolean) extends MapOption("draggable", value)

// Position
case class Position(override val value: Location) extends MapOption("position", value)

// The main title of the Map
case class Title(override val value: String) extends MapOption("title", value) {
  override lazy val toJsCmd = toProperty
  override def toString = toJsCmd
}

// Rectangle to be placed inside the map
case class Rectangle(mapVar: String, bounds: LatLngBounds, editable: Boolean) extends JsExp with MapEntity {
  override lazy val toJsCmd = {
    s"var ${id} = new google.maps.Rectangle({bounds: ${bounds}, editable: ${editable}}); ${id}.setMap(${mapVar})"
  }
  lazy val removeMarker: JsCmd = JsRaw(s"${id}.setMap(null)").cmd
}

// Zoom to be set in the map
case class Zoom(override val value: Int) extends MapOption("zoom", value)

// Center of the map (location)
case class Center(override val value: Location) extends MapOption("center", value)

// MapType: this value comes from the Google API
case class MapType(override val value: MapConstant) extends MapOption("mapTypeId", value)

// Content to be placed in map elements, this case text
case class Content(override val value: String) extends MapOption("content", value) {
  override lazy val toJsCmd = toProperty
  override def toString = toJsCmd
}

// ContentNode to be placed in map elements, this case Node -> Html
case class ContentNode(override val value: Node) extends MapOption("content", value)

//Marker and map events
case class MapEvent(name: String, argument: EventParameter*) {
  override lazy val toString = name
}

object MouseEvent extends MapEvent("MouseEvent")

object EventClick extends MapEvent("click", LatLng)

object EmptyEventClick extends MapEvent("click")

object PositionChanged extends MapEvent("position_changed")

case class EventParameter(name: String, jsRaw: String)

// Definition for click.latLng
object LatLng extends EventParameter("click", "click.latLng")

//Listener: this creates a callback to call scala code when the specified event happen in the page
case class Listener(listen: MapEntity, event: MapEvent, func: String => JsCmd) extends MapEntity with JsCmd {
  def getAjaxcall: JsCmd = Function(id, List("param1"), SHtml.ajaxCall(JsVar("param1"), func)._2.cmd)

  def toJsCmd = {
    val params = event.argument.map(_.name).mkString(",")
    val paramsj = event.argument.map(_.jsRaw).mkString(",")
    (getAjaxcall & JsRaw(s"google.maps.event.addListener(${listen.id}, '${event.name}', function(${params}) {${id}(${paramsj})})").cmd).toJsCmd
  }
}

//Info window
case class InfoWindow(options: Options) extends JsExp with MapEntity {
  override lazy val toJsCmd = s"new google.maps.InfoWindow(${options.toJsCmd})"
  override def toString = toJsCmd
  def open(item: MapEntity): JsCmd = JsRaw(s"${toJsCmd}.open(${item.id}.get('map'), ${item.id})").cmd
}

//Map Type Id
trait MapTypeJsExp extends JsExp {
  self: MapConstant =>
  override lazy val toJsCmd = s"google.maps.MapTypeId.${value}"
  override def toString = toJsCmd
}

object Hybrid extends MapConstant("HYBRID") with MapTypeJsExp

object RoadMap extends MapConstant("ROADMAP") with MapTypeJsExp

object Satellite extends MapConstant("SATELLITE") with MapTypeJsExp

object Terrain extends MapConstant("TERRAIN") with MapTypeJsExp

// Geolocation
case class AddressComponentcc(longName: String, shortName: String, types: List[AddressComponentType], childComponent: Box[AddressComponent])

//Address components types
case class AddressComponentType(name: String)

object StreetNumber extends AddressComponentType("street_number")

object Route extends AddressComponentType("route")

object Locality extends AddressComponentType("locality")

object AdministrativeArea extends AddressComponentType("administrative_area_level")

object Political extends AddressComponentType("political")

object Country extends AddressComponentType("country")

object PostalCode extends AddressComponentType("postal_code")