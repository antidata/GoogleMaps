package com.github.antidata.snippet

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

import scala.xml.{NodeSeq, Text}
import net.liftweb.util._
import net.liftweb.common._
import java.util.Date
import com.github.antidata.lib._
import Helpers._
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.SHtml
import com.github.antidata.GoogleMaps._
import com.github.antidata.GoogleMaps.Listener
import com.github.antidata.GoogleMaps.Location

class HelloWorld {
  lazy val date: Box[Date] = DependencyFactory.inject[Date] // inject the date

  def render() : NodeSeq = {
    GoogleMapsManager SetMap(MapExamples.map, MapExamples.markers,
      List(Listener(MapExamples.map, EventClick, mapClick _)))
  }

  // Here we get the coordinates of the map where the user has recently clicked
  private def mapClick(s : String) : JsCmd = {
    val marker = Marker(MapExamples.map.id, Options(Title("Lift integration with GoogleMaps API"), Position(CoordinatesParser(s).openOr(Location(0,0)))))
    marker &
      Listener(marker, EmptyEventClick, s => InfoWindow(Options(Content(s"Here goes the info from your Datastore, marker created at ${date.openOr("now").toString}"))).open(marker))
  }

  def setRectangle = "#setRectangle [onclick]" #> SHtml.ajaxInvoke(createRectangle _)

  def createRectangle : JsCmd = {
    Rectangle(MapExamples.map.id, LatLngBounds(Location(40.744715, -74.0046), Location(40.75684, -73.9966)), false)
  }
}
