package pe.gob.onpe.tramitedoc.web.util;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DateBean
  implements Serializable
{
  private static final long serialVersionUID = -8770487627374010757L;
  private Calendar cal;
  public static String FORMATO_DEFAULT = "dd/MM/yyyy";
  public static String FORMATO_CUSCAR = "yyyy-MM-dd'T'HH:mm:ss";
  public static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

  public DateBean()
  {
    this.cal = Calendar.getInstance();
  }

  public DateBean(java.sql.Date fecha)
  {
    this.cal = Calendar.getInstance();
    setFecha(fecha);
  }

  public DateBean(Timestamp fecha)
  {
    this.cal = Calendar.getInstance();
    setFecha(fecha);
  }

  public DateBean(String fecha)
  {
    this.cal = Calendar.getInstance();
    setFecha(fecha);
  }

  public DateBean(String fecha, String fmt)
  {
    this.cal = Calendar.getInstance();
    setFecha(fecha, fmt);
  }

  public DateBean(String fecha, String fmt, boolean lenient)
  {
    this.cal = Calendar.getInstance();
    setFecha(fecha, fmt, lenient);
  }

  public void setFecha(java.sql.Date fecha)
  {
    this.cal.setTime(fecha);
  }

  public void setFecha(java.util.Date fecha)
  {
    this.cal.setTime(fecha);
  }

  public void setFecha(Timestamp fecha)
  {
    this.cal.setTime(fecha);
  }

  public void setFecha(String fecha)
  {
    setFecha(fecha, FORMATO_DEFAULT);
  }

  public void setFecha(String fecha, String formato)
  {
    setFecha(fecha, formato, true);
  }

  public void setFecha(String fecha, String formato, boolean lenient)
  {
    SimpleDateFormat formatter = new SimpleDateFormat(formato);
    formatter.setLenient(lenient);
    ParsePosition pos = new ParsePosition(0);
    setFecha(formatter.parse(fecha, pos));
  }

  public String getDia()
  {
    return (this.cal.get(5) < 10 ? "0" : "") + 
      new Integer(this.cal.get(5)).toString();
  }

  public String getMes()
  {
    return (this.cal.get(2) + 1 < 10 ? "0" : "") + 
      new Integer(this.cal.get(2) + 1).toString();
  }

  public String getAnho()
  {
    return new Integer(this.cal.get(1)).toString();
  }

  public String getHora24()
  {
    return new Integer(this.cal.get(11)).toString();
  }

  public String getHora()
  {
    return new Integer(this.cal.get(10)).toString();
  }

  public String getMinuto()
  {
    return new Integer(this.cal.get(12)).toString();
  }

  public String getSegundo()
  {
    return new Integer(this.cal.get(13)).toString();
  }

  public String getMiliSegundo()
  {
    return new Integer(this.cal.get(14)).toString();
  }

  public String getMesletras()
  {
    return getMesletras(this.cal.get(2));
  }

  public String getMesletras(int mes)
  {
    String dmes = "---";
    switch (mes) {
    case 0:
      dmes = "Enero";
      break;
    case 1:
      dmes = "Febrero";
      break;
    case 2:
      dmes = "Marzo";
      break;
    case 3:
      dmes = "Abril";
      break;
    case 4:
      dmes = "Mayo";
      break;
    case 5:
      dmes = "Junio";
      break;
    case 6:
      dmes = "Julio";
      break;
    case 7:
      dmes = "Agosto";
      break;
    case 8:
      dmes = "Setiembre";
      break;
    case 9:
      dmes = "Octubre";
      break;
    case 10:
      dmes = "Noviembre";
      break;
    case 11:
      dmes = "Diciembre";
    }

    return dmes;
  }

  public String getMesletrasabr()
  {
    return getMesletras().substring(0, 3);
  }

  public String getMesletrasabr(int mes)
  {
    return getMesletras(mes).substring(0, 3);
  }

  public String getDiasemana()
  {
    return getDiasemana(this.cal.get(7));
  }

  public String getDiasemana(int dia)
  {
    String diasemana = "---";
    switch (dia) {
    case 1:
      diasemana = "Domingo";
      break;
    case 2:
      diasemana = "Lunes";
      break;
    case 3:
      diasemana = "Martes";
      break;
    case 4:
      diasemana = "Miercoles";
      break;
    case 5:
      diasemana = "Jueves";
      break;
    case 6:
      diasemana = "Viernes";
      break;
    case 7:
      diasemana = "Sabado";
    }

    return diasemana;
  }

  public String getDiasemanaabr()
  {
    return getDiasemana().substring(0, 3);
  }

  public int getMaximo(int campo)
  {
    return this.cal.getActualMaximum(campo);
  }

  public java.sql.Date getSQLDate()
  {
    return new java.sql.Date(this.cal.getTime().getTime());
  }

  public String getFormatDate(String formato)
  {
    return getFormatDate(formato, false, false);
  }

  public String getFormatCUSCAR()
  {
    return getFormatDate(FORMATO_CUSCAR, false, false);
  }

  public String getFormatDate(String formato, boolean caps)
  {
    return getFormatDate(formato, caps, false);
  }

  public String getFormatDate(String formato, boolean caps, boolean may)
  {
    SimpleDateFormat formatter = new SimpleDateFormat(formato);
    java.util.Date fecha = this.cal.getTime();
    return aEspanhol(formatter.format(fecha), caps, may);
  }

  public Calendar getCalendar()
  {
    return this.cal;
  }

  private String aEspanhol(String fecha, boolean caps, boolean may)
  {
    String[] diaing = { "monday", "tuesday", "wednesday", "thursday", 
      "friday", "saturday", "sunday" };
    String[] diaesp = { "lunes", "martes", "miercoles", "jueves", 
      "viernes", "sabado", "domingo" };

    String[] mesing = { "january", "february", "march", "april", "may", 
      "june", "july", "august", "september", "october", "november", 
      "december" };
    String[] mesesp = { "enero", "febrero", "marzo", "abril", "mayo", 
      "junio", "julio", "agosto", "setiembre", "octubre", 
      "noviembre", "diciembre" };

    for (int i = 0; i < diaing.length; i++) {
      int p = fecha.toLowerCase().indexOf(diaing[i]);
      if (p != -1) {
        String s = caps ? capitalize(diaesp[i]) : diaesp[i];
        s = may ? s.toUpperCase() : s;
        fecha = new StringBuffer(fecha).replace(p, diaing[i].length(), 
          s).toString();
        break;
      }
    }
    for (int i = 0; i < mesing.length; i++) {
      int p = fecha.toLowerCase().indexOf(mesing[i]);
      if (p != -1) {
        String s = caps ? capitalize(mesesp[i]) : mesesp[i];
        s = may ? s.toUpperCase() : s;
        fecha = new StringBuffer(fecha).replace(p, 
          p + mesing[i].length(), s).toString();
        break;
      }
    }
    return fecha;
  }

  private String capitalize(String cad)
  {
    return cad.length() > 1 ? cad.substring(0, 1).toUpperCase() + 
      cad.substring(1).toLowerCase() : cad.substring(0)
      .toUpperCase();
  }

  public Timestamp getTimestamp()
  {
    return new Timestamp(getSQLDate().getTime());
  }

  public static boolean validaFormato(String fecha, String fmt)
  {
    boolean ok = true;
    int m = 0; int a = 0; int d = 0; int p = 0;
    if (fecha.length() != fmt.length()) {
      return false;
    }
    if (fecha.length() == 10)
      p = fmt.indexOf("yyyy");
    else {
      p = fmt.indexOf("yy");
    }
    if (p != -1) {
      String yy = fecha.substring(p, p + 
        (fecha.length() == 10 ? "yyyy" : "yy").length());
      try {
        a = Integer.parseInt(yy);
      } catch (NumberFormatException e) {
        ok = false;
      }
    }
    if (ok) {
      p = fmt.indexOf("MM");
      if (p != -1) {
        String mm = fecha.substring(p, p + "MM".length());
        try {
          m = Integer.parseInt(mm);
          if ((m < 1) || (m > 12))
            ok = false;
        } catch (NumberFormatException e) {
          ok = false;
        }
      }
      if (ok) {
        p = fmt.indexOf("dd");
        if (p != -1) {
          String dd = fecha.substring(p, p + "dd".length());
          try {
            d = Integer.parseInt(dd);
            if (((m == 4) || (m == 6) || (m == 9) || (m == 11)) && 
              (d == 31)) {
              ok = false;
            }
            if (m == 2) {
              int g = a % 4;
              if (d > 29)
                ok = false;
              if ((d == 29) && (g != 0))
                ok = false;
            }
          } catch (NumberFormatException e) {
            ok = false;
          }
        }
      }
    }
    return ok;
  }

  public String getOtraFecha(String fecha, int tiempo, int tipotiempo)
  {
    return getOtraFecha(fecha, FORMATO_DEFAULT, tiempo, tipotiempo);
  }

  public String getOtraFecha(String fecha, String formatosalida, int tiempo, int tipotiempo)
  {
    setFecha(fecha);
    this.cal.add(tipotiempo, tiempo);
    return getFormatDate(formatosalida);
  }

  public static long getDiferencia(Calendar f1, Calendar f2, int tipotiempo)
  {
    long msecs = f1.getTime().getTime() - f2.getTime().getTime();

    if (tipotiempo == 1)
      return calcAnhos(f1, f2);
    if (tipotiempo == 2)
      return calcMeses(f1, f2);
    if (tipotiempo == 5)
      return calcDias(msecs);
    if (tipotiempo == 10)
      return calcHoras(msecs);
    if (tipotiempo == 12)
      return calcMinutos(msecs);
    if (tipotiempo == 13)
      return calcSegundos(msecs);
    if (tipotiempo == 14)
      return calcMilisegundos(msecs);
    return msecs;
  }

  public static boolean estaEntre(Calendar f1, Calendar f2, Calendar f3)
  {
    return (f3.after(f1)) && (f3.before(f2));
  }

  private static long calcMilisegundos(long val)
  {
    return val;
  }

  private static long calcSegundos(long val)
  {
    return calcMilisegundos(val) / 1000L;
  }

  private static long calcMinutos(long val)
  {
    return calcSegundos(val) / 60L;
  }

  private static long calcHoras(long val)
  {
    return calcMinutos(val) / 60L;
  }

  private static long calcDias(long val)
  {
    return calcHoras(val) / 24L;
  }

  private static long calcMeses(Calendar f1, Calendar f2)
  {
    Calendar f3 = Calendar.getInstance();
    f3.setTimeInMillis(f2.getTimeInMillis());
    int a = 0;

    f3.add(2, 1);
    while ((f3.before(f1)) || (f3.equals(f1))) {
      a++;
      f3.add(2, 1);
    }

    return a;
  }

  private static long calcAnhos(Calendar f1, Calendar f2)
  {
    return calcMeses(f1, f2) / 12L;
  }

  public static int compareDate(java.util.Date fecha1, java.util.Date fecha2)
  {
    if (fecha1 == null)
      fecha1 = getDateFromInteger(Integer.valueOf(10010101));
    if (fecha2 == null)
      fecha2 = getDateFromInteger(Integer.valueOf(10010101));
    Calendar c1 = Calendar.getInstance();
    Calendar c2 = Calendar.getInstance();
    c1.setTime(fecha1);
    c2.setTime(fecha2);
    return c1.compareTo(c2);
  }

  public static java.util.Date getDateFromInteger(Integer date)
  {
    if ((date == null) || (date.intValue() <= 0))
      return null;
    try {
      return sdf.parse(date.toString()); } catch (Exception e) {
    }
    return null;
  }

  public static java.util.Date getDate(String date)
  {
    return getDate(date, "dd/MM/yyyy");
  }

  public static java.util.Date getDate(String date, String format)
  {
	  DateBean fb = new DateBean();
    fb.setFecha(date, format);
    return fb.getCalendar().getTime();
  }

  public static String calcularDiferenciaExcata(java.util.Date fechaFin, java.util.Date fechaIni)
  {
    Calendar calFin = Calendar.getInstance();
    Calendar calIni = Calendar.getInstance();
    if ((getDate("01/01/0001").equals(fechaIni)) || (getDate("01/01/0001").equals(fechaFin))) {
      return "-";
    }
    if ((fechaIni == null) || (fechaFin == null)) {
      return "";
    }
    if (compareDate(fechaFin, fechaIni) >= 0) {
      calFin.setTime(fechaFin);
      calIni.setTime(fechaIni);
    } else {
      calFin.setTime(fechaIni);
      calIni.setTime(fechaFin);
    }

    return transformadarLongToString(getDiferencia(calFin, calIni, 12));
  }

  private static String transformadarLongToString(long minutos)
  {
    String signo = "";
    if (minutos < 0L) {
      signo = " - ";
      minutos *= -1L;
    }
    if (minutos < 60L) {
      return signo + minutos + "mm";
    }

    long horas = minutos / 60L;
    minutos %= 60L;

    if (horas < 24L) {
      return signo + horas + "hh " + minutos + "mm";
    }

    long dias = horas / 24L;
    horas %= 24L;

    if (dias < 30L) {
      return signo + dias + "DD " + horas + "hh " + minutos + "mm";
    }

    long meses = dias / 30L;
    dias %= 30L;

    if (meses < 12L) {
      return signo + meses + "MM " + dias + "DD " + horas + "hh " + minutos + "mm";
    }

    long anios = meses / 12L;
    meses %= 12L;

    return signo + anios + "AA " + meses + "MM " + dias + "DD " + horas + "hh " + minutos + "mm";
  }
}
