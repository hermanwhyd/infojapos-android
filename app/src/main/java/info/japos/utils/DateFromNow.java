package info.japos.utils;

import android.content.Context;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import info.japos.pp.R;
import info.japos.pp.models.DummyDate;

/**
 * Created by HWAHYUDI on 28-Feb-18.
 */

public class DateFromNow {
    private String answer;
    private Date userDate;
    private Date systemDate;
    private Context context;

    public DateFromNow(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    public String show(Date userDate){
        this.userDate = userDate;

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getDefault());
        calendar.setTime(new Date());

        systemDate = calendar.getTime();

        calculateDate();

        return answer;
    }

    public String show(DummyDate userDate){

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getDefault());
        calendar.setTime(new Date());

        systemDate = calendar.getTime();

        calendar.clear();
        calendar.set(
                userDate.getYear(),
                userDate.getMonth(),
                userDate.getDay(),
                userDate.getHour(),
                userDate.getMinute(),
                userDate.getSecond()
        );

        this.userDate = calendar.getTime();
        calculateDate();

        return answer;
    }

    /**
     * The library
     */

    private void calculateDate(){

        long diff = systemDate.getTime()-userDate.getTime();
        logDiff(diff);

        if (diff<TimeUnit.MINUTES.toMillis(1)){
            // menos que 1 minuto (agora) = funciona
            logDiff(TimeUnit.MILLISECONDS.toSeconds(diff));
            justNow();
        } else if (diff<TimeUnit.HOURS.toMillis(1)){
            // menos que 1 hora (minutos atrás) = funciona
            logDiff(TimeUnit.MILLISECONDS.toMinutes(diff));
            minutesAgo(diff);
        } else if (isToday()){
            // depois da meia noite de hoje (horas atrás)
            logDiff(TimeUnit.MILLISECONDS.toHours(diff));
            hoursAgo(diff);
        } else if (isYesterday()){
            // se a data inputada é antes da meia noite do dia atual e depois da meia noite do dia anterior = funciona
            logDiff(TimeUnit.MILLISECONDS.toDays(diff));
            yesterday();
        }  else if (diff<TimeUnit.DAYS.toMillis(7) && diff>=TimeUnit.DAYS.toMillis(1)){
            // menos que 7 dias e mais ou igual que 1 dia (dias atrás)
            logDiff(TimeUnit.MILLISECONDS.toDays(diff));
            daysAgo(diff);
        } else if (diff<TimeUnit.DAYS.toMillis(14)) {
            // menos que 14 dias = semana passada
            logDiff(TimeUnit.MILLISECONDS.toDays(diff));
            lastWeek();
        } else if (userDate.getTime()>getFirstDayOfYear()) {
            // menos que desde o primeiro dia do ano
            logDiff(diff);
            dayOfYear();
        } else {
            logDiff(diff);
            year();
        }
    }

    private void justNow(){
        set(getContext().getString(R.string.just_now));
    }

    private void minutesAgo(long diff){
        long minutesAgo = TimeUnit.MILLISECONDS.toMinutes(diff);
        if (minutesAgo==1){
            set(getContext().getString(R.string.minute_ago, String.valueOf(minutesAgo)));
        } else {
            set(getContext().getString(R.string.minutes_ago, String.valueOf(minutesAgo)));
        }
    }

    private void hoursAgo(long diff){
        long hoursAgo = TimeUnit.MILLISECONDS.toHours(diff);
        if (hoursAgo==1){
            set(getContext().getString(R.string.hour_ago, String.valueOf(hoursAgo)));
        } else {
            set(getContext().getString(R.string.hours_ago, String.valueOf(hoursAgo)));
        }
    }

    private void yesterday(){
        set(getContext().getString(R.string.yesterday));
    }

    private void daysAgo(long diff){
        // há 2 dias
        // há 6 dias
        long daysAgo = TimeUnit.MILLISECONDS.toDays(diff);
        daysAgo++;
        // pq um dia e tantas horas é igual a 1 dia, então tem que arredondar pra cima,
        // senão uma coisa do dia 20, que ocorreu as 23h, contará como 1 dia, mesmo eu estando no
        // dia 22 as 10h...
        set(getContext().getString(R.string.days_ago, String.valueOf(daysAgo)));
    }

    private void lastWeek(){
        // semana passada
        set(getContext().getString(R.string.last_week));
    }

    private void dayOfYear(){
        // 5 de jun ou 25 de mar
        Calendar currentDate = Calendar.getInstance();
        currentDate.setTime(userDate);

        int month = currentDate.get(Calendar.MONTH);
        int day = currentDate.get(Calendar.DAY_OF_MONTH);

        set(getContext().getString(R.string.day_of_year, String.valueOf(day), getMonthNameByNumber(month)));

    }

    private void year(){
        // 16/07/2015
        Calendar currentDate = Calendar.getInstance();
        currentDate.setTime(userDate);

        int year = currentDate.get(Calendar.YEAR);
        int month = currentDate.get(Calendar.MONTH);
        int day = currentDate.get(Calendar.DAY_OF_MONTH);

        month++;

        set(getContext().getString(R.string.year,String.valueOf(day),String.valueOf(month),String.valueOf(year)));
    }

    /**
     * Métodos de auxílio
     */

    private long getFirstDayOfYear(){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(systemDate);
        calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
        calendar.set(Calendar.MONTH, 0);
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        return calendar.getTime().getTime();
    }

    private boolean isToday(){

        long currentDayMidnight = getFirstSecondOfCurrentDay();
        long userDay = userDate.getTime();

        boolean isToday = userDay>=currentDayMidnight;

        return isToday;

    }

    private boolean isYesterday(){
        long currentDayMidnight = getFirstSecondOfCurrentDay();
        long lastDayMidnight = getFirstSecondOfLastDay(currentDayMidnight);
        long userDay = userDate.getTime();
        boolean isYesterday = userDay < currentDayMidnight && userDay >= lastDayMidnight;
        return isYesterday;
    }

    private long getFirstSecondOfCurrentDay(){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(systemDate);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private long getFirstSecondOfLastDay(long currentDayMidnight){
        long aDay = TimeUnit.DAYS.toMillis(1);
        return currentDayMidnight-aDay;
    }

    private String getMonthNameByNumber(int month){

        String monthForm = "";

        switch (month){
            case 0: monthForm = getContext().getString(R.string.month_january); break;
            case 1: monthForm = getContext().getString(R.string.month_february); break;
            case 2: monthForm = getContext().getString(R.string.month_march); break;
            case 3: monthForm = getContext().getString(R.string.month_april); break;
            case 4: monthForm = getContext().getString(R.string.month_may); break;
            case 5: monthForm = getContext().getString(R.string.month_june); break;
            case 6: monthForm = getContext().getString(R.string.month_july); break;
            case 7: monthForm = getContext().getString(R.string.month_august); break;
            case 8: monthForm = getContext().getString(R.string.month_september); break;
            case 9: monthForm = getContext().getString(R.string.month_october); break;
            case 10: monthForm = getContext().getString(R.string.month_november); break;
            case 11: monthForm = getContext().getString(R.string.month_december); break;
        }

        return monthForm;
    }


    /**
     * P R E G U I Ç A
     */

    private void set(String answer){
        this.answer = answer;
        log(answer);
    }

    private void logDiff(long diff){
        log("Difference >>>> ".concat(String.valueOf(diff)));
    }

    private void log(String msg){
        Log.d(this.getClass().getSimpleName(), msg);
    }
}
