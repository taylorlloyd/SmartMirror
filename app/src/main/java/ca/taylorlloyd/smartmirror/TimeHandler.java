package ca.taylorlloyd.smartmirror;

import com.squareup.otto.*;
import java.util.Calendar;
import java.text.SimpleDateFormat;

import ca.taylorlloyd.smartmirror.event.*;

public class TimeHandler {
    private Bus bus;
    public TimeHandler(Bus bus) {
        this.bus = bus;
        bus.register(this);
    }

    @Subscribe
    public void onUpdate(UpdateRequest req) {
        //Recalculate the Date and Time
        Calendar c = Calendar.getInstance();
        SimpleDateFormat dateFmt = new SimpleDateFormat("EEEE MMMM d");
        SimpleDateFormat timeFmt = new SimpleDateFormat("h:mm a");
        String dateStr = dateFmt.format(c.getTime());
        String timeStr = timeFmt.format(c.getTime());

        bus.post(new TimeEvent(timeStr, dateStr));
    }

}
