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

import net.liftweb.util._
import Helpers._
import net.liftweb.common.Box
import net.liftweb.http.js.JsCmds
import net.liftweb.actor.LiftActor
import scala.Predef._
import net.liftweb.common.Full
import net.liftweb.http.RoundTripHandlerFunc

object MapExamples {
  val locations = List(Location(40.744715, -74.0046), Location(40.75684, -73.9966))
  val opciones = Options(Zoom(12), MapType(RoadMap), Center(locations.head))
  val map = Map("map_div", opciones)
  val markers = List(Marker(map.id, Options(Title("Location 1"), Position(locations(0)))))

  val followLocations = List(
    Location(40.7432506921339, -74.00004386901855),
    Location(40.745591692181755, -73.99815559387207),
    Location(40.74858285010718, -73.99626731872559),
    Location(40.75157387349361, -73.99386405944824),
    Location(40.75144383179616, -73.99128913879395),
    Location(40.750793619494026, -73.98940086364746),
    Location(40.75001335633857, -73.98751258850098),
    Location(40.74910303775191, -73.98562431335449),
    Location(40.748322754758846, -73.9833927154541),
    Location(40.75014340083364, -73.98116111755371),
    Location(40.75248415825436, -73.97978782653809),
    Location(40.754824833271435, -73.9778995513916),
    Location(40.75755551664094, -73.97583961486816),
    Location(40.75989601311333, -73.97429466247559)
  )
}
case class NextLocation(loc : List[Location], prev : Box[Marker], map : String, func : RoundTripHandlerFunc, markerId : String)
class FollowLocations extends LiftActor {
  override def messageHandler = {
    case NextLocation(l, prev, map, func, markerId) =>
      l match {
        case h::t =>
          val newMark = Marker(map, Options(Position(h)), markerId)
          Schedule.schedule(this, NextLocation(t, Full(newMark), map, func, markerId), 3 seconds)
          func.send((prev match {case Full(m) => m.removeMarker case _ => JsCmds.Noop}) & newMark)
        case _ =>
          func.send(JsCmds.Alert("End of Tracking!"))
      }
  }
}