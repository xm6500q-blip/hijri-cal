package com.example.hijri;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.icu.util.IslamicCalendar;
import android.widget.RemoteViews;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class HijriWidget extends AppWidgetProvider {
  static final String[] M = {"محرم","صفر","ربيع الأول","ربيع الآخر","جمادى الأولى","جمادى الآخرة","رجب","شعبان","رمضان","شوال","ذو القعدة","ذو الحجة"};
  static double sin(double x){return Math.sin(Math.toRadians(x));}
  static double cos(double x){return Math.cos(Math.toRadians(x));}
  static double acos(double x){return Math.toDegrees(Math.acos(Math.max(-1,Math.min(1,x))));}

  // وقت المغرب بالساعات (مكة افتراضياً)
  static double maghrib(Calendar now) {
    double lat = 21.4225, lng = 39.8262;
    double tz = (now.get(Calendar.ZONE_OFFSET) + now.get(Calendar.DST_OFFSET)) / 3600000.0;
    double jd = now.getTimeInMillis() / 86400000.0 + 2440587.5;
    double D = jd - 2451545, g = 357.529 + 0.98560028 * D, q = 280.459 + 0.98564736 * D;
    double L = q + 1.915 * sin(g) + 0.02 * sin(2 * g), e = 23.439 - 3.6e-7 * D;
    double decl = Math.toDegrees(Math.asin(sin(e) * sin(L)));
    double ra = Math.toDegrees(Math.atan2(cos(e) * sin(L), cos(L))) / 15;
    ra = ((ra % 24) + 24) % 24;
    double eqt = q / 15 - ra;
    double E = (((eqt + 12) % 24) + 24) % 24 - 12;
    double mid = 12 + tz - lng / 15 - E;
    return mid + acos((-sin(0.833) - sin(decl) * sin(lat)) / (cos(decl) * cos(lat))) / 15;
  }

  @Override
  public void onUpdate(Context ctx, AppWidgetManager mgr, int[] ids) {
    Calendar now = Calendar.getInstance();
    double hours = now.get(Calendar.HOUR_OF_DAY) + now.get(Calendar.MINUTE) / 60.0;
    Calendar eff = (Calendar) now.clone();
    if (hours >= maghrib(now)) eff.add(Calendar.DAY_OF_MONTH, 1);

    IslamicCalendar ic = new IslamicCalendar();
    ic.setCalculationType(IslamicCalendar.CalculationType.ISLAMIC_UMALQURA);
    ic.setTimeInMillis(eff.getTimeInMillis());
    String hijri = ic.get(IslamicCalendar.DAY_OF_MONTH) + " " + M[ic.get(IslamicCalendar.MONTH)] + " " + ic.get(IslamicCalendar.YEAR);
    String day = new SimpleDateFormat("EEEE", new Locale("ar")).format(now.getTime());

    String pkg = ctx.getPackageName();
    int layout = ctx.getResources().getIdentifier("hijri_widget", "layout", pkg);
    int idHijri = ctx.getResources().getIdentifier("w_hijri", "id", pkg);
    int idDay = ctx.getResources().getIdentifier("w_day", "id", pkg);
    int idRoot = ctx.getResources().getIdentifier("w_root", "id", pkg);

    Intent open = ctx.getPackageManager().getLaunchIntentForPackage(pkg);
    PendingIntent pi = PendingIntent.getActivity(ctx, 0, open, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

    for (int id : ids) {
      RemoteViews v = new RemoteViews(pkg, layout);
      v.setTextViewText(idHijri, hijri);
      v.setTextViewText(idDay, day);
      v.setOnClickPendingIntent(idRoot, pi);
      mgr.updateAppWidget(id, v);
    }
  }
}
