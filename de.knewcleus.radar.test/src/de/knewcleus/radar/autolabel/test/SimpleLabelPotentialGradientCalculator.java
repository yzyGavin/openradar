package de.knewcleus.radar.autolabel.test;

import static java.lang.Math.abs;
import static java.lang.Math.signum;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import de.knewcleus.radar.autolabel.ILabelPotentialGradientCalculator;
import de.knewcleus.radar.autolabel.Label;
import de.knewcleus.radar.autolabel.PotentialGradient;

public class SimpleLabelPotentialGradientCalculator implements ILabelPotentialGradientCalculator {
	protected static final double EPSILON = 1E-10;
	protected static final double potAngleMax = 5E2;
	protected static final double angle0 = 0.75*Math.PI;
	protected static final double angleMax = 0.3*Math.PI;
	protected static final double potDistanceMax = 5E2;

	@Override
	public PotentialGradient getPotentialGradient(Label label, Point2D p) {
		final SimpleLabel vehicleLabel=(SimpleLabel)label;
		final PointObject vehicleSymbol=vehicleLabel.getAssociatedObject();
		final Rectangle2D symbolBounds=vehicleSymbol.getBounds2D();
		final double dx,dy;
		final double vx,vy;
		
		dx=p.getX()-symbolBounds.getCenterX();
		dy=p.getX()-symbolBounds.getCenterY();
		
		vx=vehicleSymbol.getVx();
		vy=vehicleSymbol.getVy();
		
		final double dvx,dvy; // position relative to the trueCourse vector
		
		final double r=Math.sqrt(dx*dx+dy*dy);
		final double v=1.0;
	
		dvx=dx*vx+dy*vy;
		dvy=dx*vy-dy*vx;
		
		/*
		 * Calculate the angle contribution to the gradient.
		 * 
		 * The angle contribution to the weight is found as follows
		 * 
		 * w=wmax*((angle-a0)/amax)^2
		 * angle=abs(atan(y/x))
		 */
		
		// d/dt atan(t)=cos^2(atan(t))
		
		// dw/dx=wmax * d/dx ((angle-a0)/amax)^2
		// dw/dy=wmax * d/dy ((angle-a0)/amax)^2
		// d/dx ((angle-a0)/amax)^2 = 2*(angle-a0)/amax^2 * d/dx angle
		// d/dy ((angle-a0)/amax)^2 = 2*(angle-a0)/amax^2 * d/dy angle
		
		// d/dx angle=d/dx abs(atan(y/x)) = (d/dx atan(y/x)) * d/da abs(a) | a=atan(y/x)
		// d/dy angle=d/dy abs(atan(y/x)) = (d/dy atan(y/x)) * d/da abs(a) | a=atan(y/x)
		
		// d/dx atan(y/x) = (d/dx y/x) * d/dt atan(t) | t=y/x = -y/x^2 * cos^2(atan(y/x))
		// d/dy atan(y/x) = (d/dy y/x) * d/dt atan(t) | t=y/x =  1/x   * cos^2(atan(y/x))
		
		// dw/dx = 2 * wmax * (angle-a0)/amax^2 * sig(atan(y/x)) * cos^2(atan(y/x) * (-y/x^2)
		// dw/dy = 2 * wmax * (angle-a0)/amax^2 * sig(atan(y/x)) * cos^2(atan(y/x)) * 1/x
	
		final double angleForceX,angleForceY;
		
		if (abs(dvx)<EPSILON || v<EPSILON) {
			angleForceX=angleForceY=0.0;
		} else {
			final double angle=abs(Math.atan2(dvy,dvx));
			
			final double cangle=Math.cos(angle);
			final double cangle2=cangle*cangle;
			
			final double angleForce;
			
			angleForce=2.0 * potAngleMax * (angle-angle0)/(angleMax*angleMax) * signum(angle) * cangle2;
			
			final double angleForceVX, angleForceVY;
			
			angleForceVX=-dvy * angleForce / (dvx*dvx);
			angleForceVY=       angleForce / dvx;
			
			/* Transform from velocity relative frame to global frame */
			angleForceX=(vx*angleForceVX+vy*angleForceVY)/v;
			angleForceY=(vy*angleForceVX-vx*angleForceVY)/v;
		}
		
		/*
		 * Calculate the distance contribution.
		 * 
		 * w=wmax*(4*((r-rmean)/rd)^2-1)
		 */
		
		// dw/dx = dr/dx * dw/dr
		// dw/dy = dr/dy * dw/dr
		// dw/dr = 8*wmax*(r-rmean)/rd^2
		// dr/dx = x/r
		// dr/dy = y/r
		
		final double distanceForce;
		
		distanceForce=-8.0*potDistanceMax*(r-PointObject.meanLabelDist)/(PointObject.labelDistRange*PointObject.labelDistRange);
		
		final double distanceForceX,distanceForceY;
		
		if (r<EPSILON) {
			distanceForceX=distanceForce;
			distanceForceY=0;
		} else {
			distanceForceX=dx/r*distanceForce;
			distanceForceY=dy/r*distanceForce;
		}
		
		
		return new PotentialGradient(angleForceX+distanceForceX,angleForceY+distanceForceY);
	}
}
