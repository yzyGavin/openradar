package de.knewcleus.openradar.view;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import de.knewcleus.openradar.notify.Notifier;

public class ViewerAdapter extends Notifier implements IViewerAdapter {
	protected final ICanvas canvas;
	protected final IUpdateManager updateManager; 
	protected Rectangle2D viewerExtents = new Rectangle2D.Double();
	protected double logicalScale = 1.0;
	protected Point2D logicalOrigin = new Point2D.Double();
	protected Point2D deviceOrigin = new Point2D.Double();
	protected AffineTransform deviceToLogicalTransform = null;
	protected AffineTransform logicalToDeviceTransform = null;

	public ViewerAdapter(ICanvas canvas, IUpdateManager updateManager) {
		this.canvas = canvas;
		this.updateManager = updateManager;
		updateTransforms();
	}
	
	@Override
	public ICanvas getCanvas() {
		return canvas;
	}
	
	@Override
	public IUpdateManager getUpdateManager() {
		return updateManager;
	}

	@Override
	public Rectangle2D getViewerExtents() {
		return viewerExtents;
	}

	@Override
	public void setViewerExtents(Rectangle2D extents) {
		viewerExtents = extents;
		notify(new CoordinateSystemNotification(this));
	}
	
	@Override
	public Point2D getDeviceOrigin() {
		return deviceOrigin;
	}
	
	@Override
	public void setDeviceOrigin(double originX, double originY) {
		deviceOrigin = new Point2D.Double(originX, originY);
		updateTransforms();
	}
	
	@Override
	public void setDeviceOrigin(Point2D origin) {
		deviceOrigin = origin;
		updateTransforms();
	}

	@Override
	public double getLogicalScale() {
		return logicalScale;
	}

	@Override
	public void setLogicalScale(double scale) {
		this.logicalScale = scale;
		updateTransforms();
	}

	@Override
	public void setLogicalOrigin(double offsetX, double offsetY) {
		logicalOrigin = new Point2D.Double(offsetX, offsetY);
		updateTransforms();
	}
	
	@Override
	public void setLogicalOrigin(Point2D origin) {
		logicalOrigin = origin;
		updateTransforms();
	}
	
	@Override
	public Point2D getLogicalOrigin() {
		return logicalOrigin;
	}

	@Override
	public AffineTransform getDeviceToLogicalTransform() {
		return deviceToLogicalTransform;
	}

	@Override
	public AffineTransform getLogicalToDeviceTransform() {
		return logicalToDeviceTransform;
	}
	
	protected void updateTransforms() {
		deviceToLogicalTransform = new AffineTransform(
				logicalScale, 0,
				0, -logicalScale,
				logicalOrigin.getX() - logicalScale * deviceOrigin.getX(),
				logicalOrigin.getY() + logicalScale * deviceOrigin.getY());
		logicalToDeviceTransform = new AffineTransform(
				1.0/logicalScale, 0,
				0, -1.0/logicalScale,
				deviceOrigin.getX() - logicalOrigin.getX()/logicalScale,
				deviceOrigin.getY() + logicalOrigin.getY()/logicalScale);
		notify(new CoordinateSystemNotification(this));
	}
}