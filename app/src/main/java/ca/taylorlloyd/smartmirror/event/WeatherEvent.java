package ca.taylorlloyd.smartmirror.event;

/**
 * Created by taylor on 2016-03-18.
 */
public class WeatherEvent {
    public double currentTemperature;
    public double highTemperature;
    public double lowTemperature;
    public String icon;

    public WeatherEvent(double currentTemperature, double highTemperature, double lowTemperature, String icon) {
        this.currentTemperature = currentTemperature;
        this.highTemperature = highTemperature;
        this.lowTemperature = lowTemperature;
        this.icon = icon;
    }
}
