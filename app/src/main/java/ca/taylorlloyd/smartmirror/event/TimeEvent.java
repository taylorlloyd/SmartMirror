package ca.taylorlloyd.smartmirror.event;


public class TimeEvent {
    public String timeStr;
    public String dateStr;
    public TimeEvent(String timeStr, String dateStr) {
        this.timeStr = timeStr;
        this.dateStr = dateStr;
    }
}
