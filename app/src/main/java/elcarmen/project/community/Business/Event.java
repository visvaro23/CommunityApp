package elcarmen.project.community.Business;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Event {
    int id;
    int id_community;
    String title;
    String description;
    Date dateEvent;
    Date start;
    Date end;
    String photo;
    boolean approved;

    public Event(int id, int id_community, String title, String description, String dateEvent, String start, String end, String photo, boolean approved) {
        this.id = id;
        this.id_community = id_community;
        this.title = title;
        this.description = description;

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        DateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.ENGLISH);

        String[] tStart = start.split("T")[1].split(":");
        String timeStart = tStart[0] + ":" + tStart[1];
        String[] tEnd = end.split("T")[1].split(":");
        String timeEnd = tEnd[0] + ":" + tEnd[1];
        Date date = new Date();
        Date timeS = new Date();
        Date timeE = new Date();
        try{
            date = dateFormat.parse(dateEvent);
            timeS = timeFormat.parse(timeStart);
            timeE = timeFormat.parse(timeEnd);
        }catch(Exception e){
            e.printStackTrace();
        }
        this.dateEvent = date;
        this.start = timeS;
        this.end = timeE;
        this.photo = photo;
        this.approved = approved;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId_community() {
        return id_community;
    }

    public void setId_community(int id_community) {
        this.id_community = id_community;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDateEvent() {
        return dateEvent;
    }

    public void setDateEvent(Date dateEvent) {
        this.dateEvent = dateEvent;
    }

    public String getFecha(){
        Calendar cal = Calendar.getInstance();
        cal.setTime(dateEvent);
        return cal.get(Calendar.DATE) + "/" + (cal.get(Calendar.MONTH) + 1);
    }

    public int getMonth(){
        Calendar cal = Calendar.getInstance();
        cal.setTime(dateEvent);
        return cal.get(Calendar.MONTH);
    }

    public int getYear(){
        Calendar cal = Calendar.getInstance();
        cal.setTime(dateEvent);
        return cal.get(Calendar.YEAR);
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public String getHours(){
        Calendar cal = Calendar.getInstance();
        cal.setTime(start);
        String horario = cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE) + " - ";
        cal.setTime(end);
        horario += cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE);
        return horario;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }
}