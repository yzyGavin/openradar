/**
 * Copyright (C) 2012,2013 Wolfram Wagner
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
 * GEWÄHELEISTUNG, bereitgestellt; sogar ohne die implizite Gewährleistung der
 * MARKTFÄHIGKEIT oder EIGNUNG FÜR EINEN BESTIMMTEN ZWECK. Siehe die GNU General
 * Public License für weitere Details.
 *
 * Sie sollten eine Kopie der GNU General Public License zusammen mit diesem
 * Programm erhalten haben. Wenn nicht, siehe <http://www.gnu.org/licenses/>.
 */
package de.knewcleus.openradar.rpvd;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import de.knewcleus.openradar.gui.Palette;
import de.knewcleus.openradar.gui.contacts.GuiRadarContact.State;
import de.knewcleus.openradar.view.Converter2D;
/**
 * This class paints the radar contact information into the radar screen.
 *
 * @author Wolfram Wagner
 */
public class RadarContactTextPainter {

    private enum Case { TOP_LEFT, TOP_RIGHT, BOTTOM_RIGHT, BOTTOM_LEFT }

    private TrackDisplayState trackDisplayState;
    private Point2D currentDevicePosition;
    private Line2D line;
    private volatile RoundRectangle2D background;
    private volatile Rectangle2D displayExtents;

    private final static double LENGTH = 70d;
    private final static double SPACE = 2d;

    private volatile Point2D anchor = null;
    private volatile double newX ;
    private volatile double newY ;
    private volatile Rectangle2D boundsLine1 = null;
    private volatile Rectangle2D boundsLine2 = null;
    private volatile Rectangle2D boundsText = null;

    protected volatile Path2D textContainer = new Path2D.Double();
    protected volatile Point2D textOrigin = new Point2D.Double();
    protected volatile FontMetrics fontMetrics = null;
    protected volatile Rectangle2D lastTextDisplayExtents = new Rectangle2D.Double();

    public RadarContactTextPainter(TrackDisplayState trackDisplayState) {
        this.trackDisplayState = trackDisplayState;
    }

    public synchronized Rectangle2D getDisplayExtents(Point2D currentDevicePosition) {
        this.currentDevicePosition = currentDevicePosition;
        if(boundsText==null) {
            // intial case, we don't know about the fontMetricts,so we construct a dummy
            background = new RoundRectangle2D.Double(currentDevicePosition.getX()-34d,currentDevicePosition.getY()-16d, 60+8, 24+8,10,10);
            line = new Line2D.Double(currentDevicePosition.getX(),currentDevicePosition.getY(),currentDevicePosition.getX()+24d,currentDevicePosition.getY()-24d);
        } else {
            // construct the objects at their new place
            constructBackgroundShapes(currentDevicePosition);
        }
        displayExtents = background.getBounds2D();
        Rectangle2D.union(displayExtents, line.getBounds2D(), displayExtents);
        return displayExtents;
    }

    public synchronized void paint(Graphics2D g2d, boolean hightlighted) {
        Font font = new Font("Arial", Font.PLAIN, 10);
        g2d.setFont(font);
        if(fontMetrics==null) {
            fontMetrics = g2d.getFontMetrics();
        }

//        g2d.setColor(Color.blue);
//        g2d.draw(displayExtents);
        // create composite for transparency of background
        double vspeed = trackDisplayState.guiContact.getVerticalSpeedD();
//        String sVspeed = "";
//        if(vspeed<-500) sVspeed = "--";
//        else if(vspeed<-50) sVspeed = "-";
//        else if(vspeed>50) sVspeed = "+";
//        else if(vspeed>500) sVspeed = "++";

        String textLine1 = String.format("%s %2s",trackDisplayState.guiContact.getCallSign(),trackDisplayState.guiContact.getMagnCourse());
        String textLine2 = String.format("%1s %2s %3s", trackDisplayState.guiContact.getFlightLevel(),trackDisplayState.guiContact.getAirSpeed(), "");//trackDisplayState.guiContact.getVerticalSpeed());
        boundsLine1 = fontMetrics.getStringBounds(textLine1, g2d);
        boundsLine2 = fontMetrics.getStringBounds(textLine2, g2d);
        boundsText = new Rectangle2D.Double(boundsLine1.getX(), boundsLine1.getY()-boundsLine1.getHeight(), Math.max(boundsLine1.getWidth(), boundsLine2.getWidth()), boundsLine1.getHeight()+boundsLine2.getHeight());

        if(anchor==null) constructBackgroundShapes(currentDevicePosition);

        AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,hightlighted ? 1f : 0.60f);
        g2d.setComposite(ac);
        g2d.setColor(hightlighted ? Color.white : Palette.LANDMASS);
        g2d.fill(background);
        lastTextDisplayExtents = background.getBounds2D();

        ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1f);
        g2d.setComposite(ac);

        if(trackDisplayState.guiContact.isSelected()) {
            // SELECTED
            g2d.setColor(Palette.RADAR_SELECTED);

        } else if(!trackDisplayState.guiContact.isActive()) {
            // INCACTIVE GHOSTS
            g2d.setColor(Palette.RADAR_GHOST);

        } else if(trackDisplayState.guiContact.isNeglect()) {
            // BAD GUYS
            g2d.setColor(Palette.RADAR_GHOST);

        } else if(trackDisplayState.guiContact.getState()==State.IMPORTANT) {
            // CONTROLLED left column
            g2d.setColor(Palette.RADAR_CONTROLLED);

        } else if(trackDisplayState.guiContact.getState()==State.CONTROLLED) {
            // WATCHED middle column
            g2d.setColor(Palette.RADAR_IMPORTANT);
        } else {
            // UNCONTROLLED right column
            g2d.setColor(Palette.RADAR_UNCONTROLLED);
//            // SELECTED
//            g2d.setColor(Palette.RADAR_SELECTED);
//            //g2d.setColor(new Color(205,255,255));
//            g2d.setColor(Palette.YELLOW);
//        } else if(!trackDisplayState.guiContact.isActive()) {
//            // INCACTIVE GHOSTS
//            g2d.setColor(Color.GRAY);
//        } else if(trackDisplayState.guiContact.isNeglect()) {
//            // BAD GUYS
//            g2d.setColor(Color.GRAY);
//        } else if(trackDisplayState.guiContact.getState()==State.IMPORTANT) {
//            // IMPORTANT
//            g2d.setColor(Palette.GREEN);
//            g2d.setColor(Palette.LIGHTBLUE);
//        } else if(trackDisplayState.guiContact.getState()==State.CONTROLLED) {
//            // CONTROLLED
//            g2d.setColor(Palette.YELLOW);
//            g2d.setColor(new Color(90,255,90));
//        } else {
//            g2d.setColor(Palette.GREEN);
//            g2d.setColor(new Color(205,255,255));
        }


        line = new Line2D.Double(currentDevicePosition.getX(),currentDevicePosition.getY(),anchor.getX(),anchor.getY());
        g2d.draw(line);

        if(hightlighted) {
            // SELECTED
            g2d.setColor(Color.black);
        }
        g2d.drawString(textLine1,(float)(newX+SPACE),(float)(newY+boundsLine1.getHeight()));
        g2d.drawString(textLine2,(float)(newX+SPACE),(float)(newY+boundsText.getHeight()));

        drawArrow(g2d,vspeed, newX-SPACE+boundsLine2.getWidth(),newY+boundsText.getHeight()+2,boundsLine2.getHeight());
        if(trackDisplayState.guiContact.hasFgComSupport()) {
            drawFgComAntenna(g2d, newX + boundsLine1.getWidth() + 4, newY +2);
        }
    }

    private void constructBackgroundShapes(Point2D currentDevicePosition) {

        double trueCourse = trackDisplayState.guiContact.getGroundSpeedD()>1 ? trackDisplayState.guiContact.getTrueCourseD() : 0d;
        Case layout = getCase(trueCourse);


        // first text line will start at (dx, dy)
        anchor = Converter2D.getMapDisplayPoint(currentDevicePosition, trueCourse + 135, LENGTH);
        newX = -1;
        newY = -1;

        switch(layout) {
        case TOP_LEFT:
            newX = anchor.getX()+SPACE;
            newY = anchor.getY()+SPACE;
            break;
        case TOP_RIGHT:
            newX = anchor.getX()-boundsText.getWidth()-SPACE;
            newY = anchor.getY()+SPACE;
            break;
        case BOTTOM_RIGHT:
            newX = anchor.getX()-boundsText.getWidth()+SPACE;
            newY = anchor.getY()-boundsText.getHeight()-2*SPACE;
            break;
        case BOTTOM_LEFT:
            newX = anchor.getX()+SPACE;
            newY = anchor.getY()-boundsText.getHeight()-2*SPACE;
            break;
        }

        background = new RoundRectangle2D.Double(newX-SPACE,newY-SPACE, boundsText.getWidth()+8*SPACE, boundsText.getHeight()+4*SPACE,10d,10d);
    }

    private Case getCase(double trueCourseD) {
        if(trueCourseD>0 && trueCourseD<=90) return Case.TOP_LEFT;
        if(trueCourseD>90 && trueCourseD<=180) return Case.TOP_RIGHT;
        if(trueCourseD>180 && trueCourseD<=270) return Case.BOTTOM_RIGHT;
        return Case.BOTTOM_LEFT;
    }

    public synchronized boolean contains(Point2D devicePoint) {
        return background.contains(devicePoint) || line.contains(devicePoint);
    }

    private void drawArrow(Graphics2D g2d, double vSpeed, double x, double y, double length) {
        int xOffset = 0;

        if(Math.abs(vSpeed)>100) {

            Point2D tipPoint = new Point2D.Double(Math.round(x+xOffset), Math.round(vSpeed<0 ? y : y-length));
            Point2D otherPoint = new Point2D.Double(Math.round(x+xOffset), Math.round(vSpeed<0 ? y-length:y));
            double heading = vSpeed>0 ? 0 : 180;
            Point2D point1 = Converter2D.getMapDisplayPoint(tipPoint, heading-180+25, 6);
            Point2D point2 = Converter2D.getMapDisplayPoint(tipPoint, heading-180-25, 6);

            g2d.draw(new Line2D.Double(otherPoint, tipPoint));

            Path2D path = new Path2D.Double();
            path.append(new Line2D.Double(tipPoint, point1),false);
            path.append(new Line2D.Double(point1, point2),true);
            path.append(new Line2D.Double(point2, tipPoint),true);
//            path.closePath();
            g2d.draw(path);
            g2d.fill(path);
        }
    }

    private void drawFgComAntenna(Graphics2D g2d, double x, double y) {
        // antenna
        Point2D tipPoint = new Point2D.Double(Math.round(x),Math.round(y));
//        Point2D point1 = Converter2D.getMapDisplayPoint(tipPoint, 180+8, 9);
//            g2d.draw(new Line2D.Double(tipPoint, point1));
//            g2d.fill(new Ellipse2D.Double(tipPoint.getX()-1, tipPoint.getY()-1,3,3));

        // headset
        Point2D center = new Point2D.Double(Math.round(x+4),Math.round(y+5));
        Point2D point2 = Converter2D.getMapDisplayPoint(center, 90, 3);
        Point2D point3 = Converter2D.getMapDisplayPoint(center, 270, 3);

        g2d.draw(new Arc2D.Double(tipPoint.getX(), tipPoint.getY(),8,10,0,270,Arc2D.OPEN));
        g2d.fill(new Ellipse2D.Double(point2.getX()-1, point2.getY()-1,3,3));
        g2d.fill(new Ellipse2D.Double(point3.getX()-1, point3.getY()-1,3,3));
    }
}
