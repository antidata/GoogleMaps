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

import scala.xml.NodeSeq
import net.liftweb.http.js.JsCmds.{SetExp, OnLoad, JsCrVar, Script}
import net.liftweb.http.js.JE.{JsRaw, Str}
import net.liftweb.http.js.JsCmd
import scala.util.parsing.combinator._
import net.liftweb.common.{Box, Empty, Full}

/**
 * This manager will create the NodeSeq and JScript to put on the page
 */
object GoogleMapsManager {
  /**
   * Create the map nodeseq to put in the html
   * @param map The Map that will appear in the page
   * @param markers The markers to be included in the map
   * @param listeners Listeners to be included in the page
   * @param assign When we need to assign the map instance to other variables
   * @return NodeSeq to manage the map and ajax calls
   */
  def SetMap(map: Map, markers: List[Marker], listeners: List[Listener], assign : String*): NodeSeq = {
    {
      Script(JsCrVar(map.id, Str("")))
    } ++ {
      Script(OnLoad(generate(map, markers, assign:_*) & listeners.foldLeft(JsRaw("").cmd)((res, list) => res & list)))
    }
  }

  /**
   * Creates the JsExp required to set the map on the page and its markers
   * @param map The map to be placed in the page
   * @param mkrs Markers to be placed in the map
   * @param assign When we need to assign the map instance to other variables
   * @return Js to set the map on the page
   */
  private def generate(map: Map, mkrs: List[Marker], assign : String*): JsCmd = {
    SetExp(JsRaw(map.options.id), map.options) & SetExp(JsRaw(map.id), map) &
      mkrs.foldLeft(JsRaw("").cmd)((res, mark) => res & mark.cmd) &
      assign.foldLeft(JsRaw("").cmd)((res, _var) => res & SetExp(JsRaw(_var),JsRaw(map.id)))
  }
}

/**
 * This is used to parse the string pair (lat, long)
 */
object CoordinatesParser extends RegexParsers { parser =>
  def number : Parser[Double] = """[-+]?\d+(\.\d*)?""".r ^^ { _.toDouble }
  def latlong : Parser[(Double, Double)] = ("(") ~> number ~ (",") ~ number <~ (")") ^^ {
    case number ~ comma ~ number2  => (number, number2)
  }
  def apply(input : String) : Box[Location] = {
    val parsed = parseAll(parser.latlong, input)
    if(parsed.isEmpty)
      Empty
    else
      Full(Location(parsed.get._1, parsed.get._2))
  }
}

