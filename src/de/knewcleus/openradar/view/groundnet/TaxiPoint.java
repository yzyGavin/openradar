/**
 * Copyright (C) 2012 Wolfram Wagner 
 * 
 * This file is part of OpenRadar.
 * 
 * OpenRadar is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * OpenRadar is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * OpenRadar. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Diese Datei ist Teil von OpenRadar.
 * 
 * OpenRadar ist Freie Software: Sie können es unter den Bedingungen der GNU
 * General Public License, wie von der Free Software Foundation, Version 3 der
 * Lizenz oder (nach Ihrer Option) jeder späteren veröffentlichten Version,
 * weiterverbreiten und/oder modifizieren.
 * 
 * OpenRadar wird in der Hoffnung, dass es nützlich sein wird, aber OHNE JEDE
 * GEWÄHRLEISTUNG, bereitgestellt; sogar ohne die implizite Gewährleistung der
 * MARKTFÄHIGKEIT oder EIGNUNG FÜR EINEN BESTIMMTEN ZWECK. Siehe die GNU General
 * Public License für weitere Details.
 * 
 * Sie sollten eine Kopie der GNU General Public License zusammen mit diesem
 * Programm erhalten haben. Wenn nicht, siehe <http://www.gnu.org/licenses/>.
 */
package de.knewcleus.openradar.view.groundnet;

import java.awt.geom.Point2D;

public class TaxiPoint {

    private int index;
    private double lat;
    private double lon;
    private double ctrlLat;
    private double ctrlLon;
    private boolean isOnRunway;
    private String holdPointType;
    private int currentPaintStyle=0;
    
    public TaxiPoint(String index, String lat, String lon, boolean isOnRunway, String holdPointType) {
        this.index = Integer.parseInt(index); 
        this.lat = parseLocation(lat);
        this.lon = parseLocation(lon);
        this.isOnRunway = isOnRunway;
        this.holdPointType = holdPointType;
    }

    public TaxiPoint(String code, String lat, String lon, String ctrlLat, String ctrlLon, int currentPaintStyle) {
        this.index = Integer.parseInt(code); 
        this.lat = Double.parseDouble(lat);
        this.lon = Double.parseDouble(lon);
        this.ctrlLat = Double.parseDouble(lat);
        this.ctrlLon = Double.parseDouble(lon);
        this.isOnRunway = false;
        this.holdPointType = "";
        
    }

    private double parseLocation(String location) {
        int sign = location.startsWith("S") || location.startsWith("W")?-1:1;
        int degree = Integer.parseInt(location.substring(1,location.indexOf(" ")));
        double minutes = Double.parseDouble(location.substring(location.indexOf(" ")));
        
        return (double)sign * degree + minutes/60d;
    }

    public int getIndex() {
        return index;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public boolean isOnRunway() {
        return isOnRunway;
    }

    public String getHoldPointType() {
        return holdPointType;
    }

    public Point2D getGeoPoint2D() {
        return new Point2D.Double(lon,lat);
    }

    public double getCtrlLat() {
        return ctrlLat;
    }

    public double getCtrlLon() {
        return ctrlLon;
    }

    public int getPaintStyle() {
        return currentPaintStyle;
    }

}
