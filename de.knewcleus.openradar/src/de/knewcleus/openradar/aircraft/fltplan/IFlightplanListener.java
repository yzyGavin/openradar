package de.knewcleus.openradar.aircraft.fltplan;

public interface IFlightplanListener {
	public abstract void flightplanAvailable(Flightplan flightplan);
	public abstract void flightplanUpdate(Flightplan flightplan);
	public abstract void flightplanCancelled(Object reference);
}