package ca.taylorlloyd.smartmirror.event;

import android.location.Location;

/**
 * Created by taylor on 2016-03-17.
 */
public class LocationEvent {
    public Location location;
    public LocationEvent(Location location) {
        this.location = location;
    }
}
