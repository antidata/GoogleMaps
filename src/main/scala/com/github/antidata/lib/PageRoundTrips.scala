package com.github.antidata.lib

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

import net.liftweb.http.RoundTripInfo

abstract class PageRoundTrips {
  protected def getRoundTrips : List[RoundTripInfo]
}

class EmptyRoundTrip extends PageRoundTrips {
  protected def getRoundTrips : List[RoundTripInfo] = Nil
}
