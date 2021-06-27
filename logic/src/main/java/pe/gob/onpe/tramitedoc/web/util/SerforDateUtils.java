package pe.gob.onpe.tramitedoc.web.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.springframework.util.StringUtils;




public class SerforDateUtils {
	
	static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
	static SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	static SimpleDateFormat SDF_ddMMyyyy = new SimpleDateFormat("dd/MM/yyyy");
	static SimpleDateFormat SDF_yyyyMMdd = new SimpleDateFormat("yyyy-MM-dd");
	static SimpleDateFormat SDF_ddMMyyyy_HHmmss = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	static SimpleDateFormat SDF_yyyyMMdd_HHmmss = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");	
	
	//NSR: Constantes para comparación de Fechas y Horas
	public static final String COMPARA_SOLO_FECHA = "COMPARA_SOLO_FECHA";
	public static final String COMPARA_SOLO_TIEMPO = "COMPARA_SOLO_TIEMPO";
	public static final String COMPARA_TODO = "COMPARA_TODO";
	
	/**
	 * Metodo realizado para setear año, mes, dia o hora de una fecha.
	 * @param fecha			Fecha a la cual se aplicaron los cambios enviado por los parametros
	 * @param tipoCalendar Tipo de unidad de fecha con la cual se trabajara:<br /> 
	 * 							Calendar.YEAR  : AÑO<br />
	 *							Calendar.MONTH : MES<br />
	 *							Calendar.DATE  : DIA<br />
	 *							Calendar.HOUR  : HORA<br />
	 * @param numero		-El año "y" está representado por un entero igual a ("y" - 1900).<br /> 
	 * 						Por ejemplo el año 2012 se representa como 112 (2012 - 1900).<br /> 
	 * 						-Los meses son representados por números entre 0 y 11, donde enero es 0 y diciembre es 11.<br />
	 * 						-Los días y minutos se representan de forma corriente. Entre 1 - 31 y 0 - 59 respectivamente.<br />
	 * 						-Las horas van entre 0 y 23, donde la medianoche es 0 y el medio día 12.
	 * @return			   La fecha despues de setear lo enviado como parametro.
	 */
	public static Date setDate(Date fecha, int tipoCalendar, int numero){
		Calendar cal = Calendar.getInstance();
		cal.setTime(fecha);
		cal.set(tipoCalendar, numero);
		return cal.getTime();
	}
	public static Date toDate(Object objeto){
		try{
			if( objeto == null ){
				return null;
			}else{
				return (Date) objeto;
			}
		}catch(Exception e){
			return null;
		}
	}
	
	public static String getCurrentFormatDate(String formato){
		DateBean fb=new DateBean();
		return fb.getFormatDate(formato);
	}
	
	public static boolean isDefaultDate(Date fecha)
	{
		String fechaAsString = sdf.format(fecha);
		
		return "00010101".equals(fechaAsString);
	}
	
	public static Date getDefaultDate(){
		Calendar cal = Calendar.getInstance();
		cal.set(1, 0, 1, 0, 0, 0);
		return cal.getTime();
	}	
	
	public static Date getCurrentDate(){
		return (new DateBean()).getCalendar().getTime();
	}
	
	/**
	 * return Integer
	 * Devuelve una fecha como entero en formato yyyyMMdd 
	 */
	public static Integer getCurrentIntegerDate(){
		return new Integer(getCurrentFormatDate("yyyyMMdd"));
	}
	
	public static Date addDay(Date fecha, int dias){
		DateBean fb=new DateBean();
		fb.setFecha(fecha);
		Calendar cal=Calendar.getInstance();
		cal.setTime(fecha);
		cal.add(Calendar.DAY_OF_MONTH, dias);
		return cal.getTime();
	}

	public static Date addMonth(Date fecha, int meses){
		DateBean fb=new DateBean();
		fb.setFecha(fecha);
		Calendar cal=Calendar.getInstance();
		cal.setTime(fecha);
		cal.add(Calendar.MONTH, meses);
		return cal.getTime();
	}
	
	public static Date addYear(Date fecha, int anios){
		DateBean fb=new DateBean();
		fb.setFecha(fecha);
		Calendar cal=Calendar.getInstance();
		cal.setTime(fecha);
		cal.add(Calendar.YEAR, anios);
		return cal.getTime();
	}

	public static int compareDate(Date fecha1,Date fecha2){
		if (fecha1==null)
			fecha1=getDateFromInteger(10010101);
		if (fecha2==null)
			fecha2=getDateFromInteger(10010101);	
		Calendar c1=Calendar.getInstance();
		Calendar c2=Calendar.getInstance();
		c1.setTime(fecha1);
		c2.setTime(fecha2);
		return c1.compareTo(c2);
	}
	
	/**
	 * 
	 * @param date
	 * @return 
	 */
	public static Date getDateFromInteger(Integer date) {
		if( date == null || date <=0)
			return null;
		try {
			return sdf.parse(date.toString());	
		} catch (Exception e) {
			return null;
		}				
	}
	
	/**
	 * @param fecha1
	 * @param fecha2
	 * @return verdadero si las dos fechas, horas, minutos, segundos y ms son iguales
	 */
	public static boolean isEqualTo(Date fecha1, Date fecha2){
		
		return compareDate(fecha1, fecha2) == 0;
	}
	
	/**
	 * Retorna TRUE si las fechas son iguales, 3 tipos de comparacion que se indica en el 3er parametro
	 * COMPARA_SOLO_FECHA si las fechas son iguales retorna true.<br> 
	 * COMPARA_SOLO_TIEMPO si las horas, minutos, segundo y milisegundos son iguales retorna true.<br> 
	 * COMPARA_TODO Si la fecha y hora son iguales retorna true<br>
	 * 
	 * @param fecha1
	 * @param fecha2
	 * @param flag
	 * @return true si las dos fechas son iguales de acuerdo al parametro de comparación
	 * @author nsullca
	 */
	public static boolean sonIguales(Date fecha1, Date fecha2, String flag){
		if (COMPARA_SOLO_FECHA.equals(flag)  ){
			return compareDate(getOnlyDateFrom(fecha1),getOnlyDateFrom(fecha2)) == 0;
		}else if (COMPARA_SOLO_TIEMPO.equals(flag)){
			return compareDate(getOnlyTime(fecha1),getOnlyTime(fecha2)) == 0;
		}else if (COMPARA_TODO.equals(flag) ){
			return compareDate(fecha1,fecha2) == 0;
		}else{
			return compareDate(fecha1,fecha2) == 0;
		}
	}

	public static boolean esFecha1MayorQueFecha2(Date fecha1, Date fecha2, String flag){
		if (COMPARA_SOLO_FECHA.equals(flag)  ){
			return compareDate(getOnlyDateFrom(fecha1),getOnlyDateFrom(fecha2)) > 0;
		}else if (COMPARA_SOLO_TIEMPO.equals(flag)){
			return compareDate(getOnlyTime(fecha1),getOnlyTime(fecha2)) > 0;
		}else if (COMPARA_TODO.equals(flag) ){
			return compareDate(fecha1,fecha2) > 0;
		}else{
			return compareDate(fecha1,fecha2) > 0;
		}
	}
	public static boolean esFecha1MayorIgualQueFecha2(Date fecha1, Date fecha2, String flag){
		if (COMPARA_SOLO_FECHA.equals(flag)  ){
			return compareDate(getOnlyDateFrom(fecha1),getOnlyDateFrom(fecha2)) >= 0;
		}else if (COMPARA_SOLO_TIEMPO.equals(flag)){
			return compareDate(getOnlyTime(fecha1),getOnlyTime(fecha2)) >= 0;
		}else if (COMPARA_TODO.equals(flag) ){
			return compareDate(fecha1,fecha2) >= 0;
		}else{
			return compareDate(fecha1,fecha2) >= 0;
		}
	}
	public static boolean esFecha1MenorQueFecha2(Date fecha1, Date fecha2, String flag){
		if (COMPARA_SOLO_FECHA.equals(flag)  ){
			return compareDate(getOnlyDateFrom(fecha1),getOnlyDateFrom(fecha2)) < 0;
		}else if (COMPARA_SOLO_TIEMPO.equals(flag)){
			return compareDate(getOnlyTime(fecha1),getOnlyTime(fecha2)) < 0;
		}else if (COMPARA_TODO.equals(flag) ){
			return compareDate(fecha1,fecha2) < 0;
		}else{
			return compareDate(fecha1,fecha2) < 0;
		}
	}
	public static boolean esFecha1MenorIgualQueFecha2(Date fecha1, Date fecha2, String flag){
		if (COMPARA_SOLO_FECHA.equals(flag)  ){
			return compareDate(getOnlyDateFrom(fecha1),getOnlyDateFrom(fecha2)) <= 0;
		}else if (COMPARA_SOLO_TIEMPO.equals(flag)){
			return compareDate(getOnlyTime(fecha1),getOnlyTime(fecha2)) <= 0;
		}else if (COMPARA_TODO.equals(flag) ){
			return compareDate(fecha1,fecha2) <= 0;
		}else{
			return compareDate(fecha1,fecha2) <= 0;
		}
	}

	/**
	 * 
	 * @param date
	 * @return Retorna una fecha en entero de la forma yyyyMMdd
	 */
	public static Integer getIntegerFromDate(Object date) {
		
		if( date == null)
			return 0;

		return new Integer (sdf.format(date)); 
	}
	
	//feriados es una lista de fechas en formado yyyyMMdd
	public static Date addUtilDay(Date fecha, int dias, List<String> feriados){
		DateBean fb=new DateBean();
		fb.setFecha(fecha);
		int indFeriado=0;
		String feriado=feriados!=null && !feriados.isEmpty()?feriados.get(indFeriado):"";
		Calendar cal=Calendar.getInstance();
		cal.setTime(fecha);
		for(int i=1;i<=dias;i++){
			boolean isFeriado=!"".equals(feriado);
			boolean fds=true;
			while(isFeriado || fds){
				cal.add(Calendar.DAY_OF_MONTH, dias);
				DateBean fechaBeanSum=new DateBean();
				fechaBeanSum.setFecha(cal.getTime());
				if (!fechaBeanSum.getFormatDate("yyyyMMdd").equals(feriado) && isFeriado){
					isFeriado=false;
					if (!"".equals(feriado)){
						if (++indFeriado<feriados.size())
							feriado=feriados.get(indFeriado);
					}
				}
				if (cal.get(Calendar.DAY_OF_WEEK)!=Calendar.SATURDAY && cal.get(Calendar.DAY_OF_WEEK)!=Calendar.SUNDAY){
					fds=false;
				}
			}
		}
		return cal.getTime();
	}
	
	public static Date addUtilDay(Date fecha, int dias){
		return addUtilDay(fecha, dias, new ArrayList<String>());
	}
	
	//tipoFecha es segun los valores en la clase Calendar
	public static Long getDifference(Date d1, Date d2, int tipoFecha){
		Calendar c1=Calendar.getInstance();
		Calendar c2=Calendar.getInstance();
		if (SerforDateUtils.compareDate(d1, d2)>=0){
			c1.setTime(d1);
			c2.setTime(d2);
		}else{
			c1.setTime(d2);
			c2.setTime(d1);
		}
		
		return DateBean.getDiferencia(c1, c2, tipoFecha);
	}
	
	/**
	 *  Devuelve el numero de dias calendarios desde fecha1 hasta fecha2<br>
	 *  Si fecha1 = fecha2 devuelve 0<br>
	 *  Si fecha1 > fecha2 devuelve un numero mayor que 0 que representa el numero de dias calendarios que fecha 1 excede a fecha 2<br>
	 *  Si fecha1 < fecha2 devuelve un numero menor que 0 que representa el numero de dias calendarios que faltan para que fecha 1 sea igual a fecha2<br>
	 * @param fecha1
	 * @param fecha2
	 * @return Long diferencia de dias
	 */
	
	public static Long getDiferenciaDiasCalendarios(Date fecha1, Date fecha2){
		Calendar c1=Calendar.getInstance();
		Calendar c2=Calendar.getInstance();
		if(fecha1==null || fecha2==null){
			throw new IllegalArgumentException("El(los) argumento(s) no puede(n) ser nulo(s)");
		}
		c1.setTime(getOnlyDateFrom(fecha1));
		c2.setTime(getOnlyDateFrom(fecha2));
		return DateBean.getDiferencia(c1, c2, Calendar.DAY_OF_MONTH);
	}
	
	public static Date getDate(String date){
		return getDate(date, "dd/MM/yyyy");
	}
	
	public static Date getDateyyyMMdd(String date){
		return getDate(date, "yyyyMMdd");
	}
	
	public static Date getDate(String date, String format){
		DateBean fb=new DateBean();
		fb.setFecha(date, format);
		return fb.getCalendar().getTime();
	}
	
	public static String getFormatDate(Date date, String format){
		DateBean fb=new DateBean();
		if (date==null)
			return "";
		fb.setFecha(date);
		return fb.getFormatDate(format);
	}
	
//	public static Integer getAnho(Date date){
//		DateBean fb=new DateBean();
//		if (date==null)
//			return new Integer(0);
//		fb.setFecha(date);
//		String anho=fb.getAnho();
//		if (SunatStringUtils.isNumeric(anho))
//			return SunatNumberUtils.toInteger(anho);
//		else 
//			return new Integer(0);
//	}
	
	/* PAS20155E220000054 INICIO GGRANADOS */
	/**
	 * Devuelve los 2 ultimos digitos del año de la fecha
	 * enviada como parametro
	 * @param date
	 * @return
	 */
//	public static Integer getAnho2Digitos(Date date){
//		DateBean fb=new DateBean();
//		if (date==null)
//			return new Integer(0);
//		fb.setFecha(date);
//		String anho=fb.getAnho().substring(2, 4);
//		if (SunatStringUtils.isNumeric(anho))
//			return SunatNumberUtils.toInteger(anho);
//		else 
//			return new Integer(0);
//	}
	

	public static Date getDateFromUnknownFormat(String strDate) {
		if( ! SerforStringUtils.isEmpty(strDate) )
		{
			if(SerforStringUtils.length(strDate)==10){
				try {
					return SDF_ddMMyyyy.parse(strDate);	
				} catch (ParseException e) {
				}
				
				try {
					return SDF_yyyyMMdd.parse(strDate);	
				} catch (ParseException e) {
				}
				
				
			}else{
				try {
					return SDF_ddMMyyyy_HHmmss.parse(strDate);	
				} catch (ParseException e) {
				}
				
				try {
					return SDF_yyyyMMdd_HHmmss.parse(strDate);	
				} catch (ParseException e) {
				}
			}
		}
		return null;
	}
	/**
	 * Retorna un Date solo con la fecha del parametro y con 0 Horas, 0 minutos, 0 segundos, 0 ms 
	 * @param date
	 * @return Date
	 */
    public static Date getOnlyDateFrom(Date date){
    	Date ret = null;
    	if(date!=null){
	    	Calendar cal = Calendar.getInstance();
	    	cal.setTime(date);
			cal.set(Calendar.HOUR_OF_DAY,0);		
			cal.set(Calendar.MINUTE,0);		
			cal.set(Calendar.SECOND,0);
			cal.set(Calendar.MILLISECOND,0);
			ret = cal.getTime();
    	}
    	return ret;
    }
    
    /**
	 * Retorna un Date solo con el anio del parametro y con 0 Horas, 0 minutos, 0 segundos, 0 ms 
	 * @param date
	 * @return Date
	 */
    public static Date getOnlyDateFrom(Integer anio){
    	GregorianCalendar calendarInicial = new GregorianCalendar();
		calendarInicial.setTime(new Date());
		calendarInicial.set(Calendar.YEAR, anio);
		calendarInicial.set(Calendar.MONTH, 00);
		calendarInicial.set(Calendar.DAY_OF_YEAR, 1);
    	return getOnlyDateFrom(calendarInicial.getTime());
    }
    

	/**
	 * Retorna un Date solo con la hora, minuto, segundos y ms del parametro. La fecha 01/01/0001 
	 * @param date
	 * @return Date
	 */
    public static Date getOnlyTime(Date date){
    	Date ret = null;
    	if(date!=null){
	    	Calendar cal = Calendar.getInstance();
	    	cal.setTime(date);
	    	
			cal.set(Calendar.YEAR, 1);		
			cal.set(Calendar.MONTH, 0);		
			cal.set(Calendar.DAY_OF_MONTH, 1);		
			ret = cal.getTime();
    	}
    	return ret;
	}
	
	//JC - inicio - devuelve la diferencia expresada en DD HH:MM
	public static String getDiferenciaDiasHorasMinutosCalendarios(Date fecha1, Date fecha2){
		Calendar c1=Calendar.getInstance();
		Calendar c2=Calendar.getInstance();
		
		if(fecha1==null || fecha2==null){
			throw new IllegalArgumentException("El(los) argumento(s) no puede(n) ser nulo(s)");
		}
		
		if(esFecha1MenorQueFecha2(fecha1, fecha2, "COMPARA_TODO")){
			throw new IllegalArgumentException("El argumento final no puede ser menor que el argumento inicial");
		}		

		c1.setTime(fecha1);
		c2.setTime(fecha2);
		
		int p_ent = 0;
		double p_dec= 0.0;
		
		Long dias    = null;
		Long horas   = null;
		Long minutos = null;
		String resultado = null;
		
		minutos = DateBean.getDiferencia(c1, c2, Calendar.MINUTE);
		
		//--calculo de dias
		double diasTmp = (minutos*1.0 / 1440.0);
		p_ent = (int)diasTmp;
		p_dec = diasTmp - p_ent;
		dias = new Long(p_ent);

		//--calculo de horas
		double horasTmp = p_dec * 24;
		p_ent = (int)horasTmp;
		p_dec = horasTmp - p_ent;
		horas = new Long(p_ent);		
		
		//--calculo de minutos
		double minutosTmp = p_dec * 60;
		minutos = Math.round(minutosTmp);

		//--resultado
		if(dias < 0 || horas < 0 || minutos < 0) resultado = new String("00 00:00");
		resultado = dias.toString() + " " + String.format("%02d", horas) + ":" + String.format("%02d", minutos);
		
		return resultado;
	}
	//JC - fin - devuelve la diferencia expresada en DD HH:MM	
	
    public static Date parseXsdDate(final String xsdDateString) {
	    String xsdDateCopy = xsdDateString;

	    if (xsdDateCopy == null || xsdDateCopy.trim().length() == 0) {
	      throw new IllegalArgumentException("El argumento no puede estar vacio o ser null");
	    }

	    DatatypeFactory factory = null;

	    try {
	      factory = DatatypeFactory.newInstance();
	    } catch (DatatypeConfigurationException e) {
	    	throw new IllegalStateException(e);
		}

	    XMLGregorianCalendar xmlCal = factory.newXMLGregorianCalendar(xsdDateCopy);
	    //GregorianCalendar cal = xmlCal.toGregorianCalendar();
	    //20111115 para manejar fechas anteriores al calendario gregoriano (1582)
	    //20111116 para que siga soportando el timezone 
	    TimeZone tz = xmlCal.getTimeZone(DatatypeConstants.FIELD_UNDEFINED);
	    Locale locale = java.util.Locale.getDefault();
	    GregorianCalendar cal = new GregorianCalendar(tz,locale);
	    cal.clear();
	    cal.set(Calendar.YEAR, xmlCal.getEonAndYear().abs().intValue());
	    cal.set(Calendar.MONTH, xmlCal.getMonth()-1);
	    cal.set(Calendar.DAY_OF_MONTH, xmlCal.getDay());
	    cal.set(Calendar.HOUR, xmlCal.getHour());
	    cal.set(Calendar.MINUTE, xmlCal.getMinute());
	    cal.set(Calendar.SECOND, xmlCal.getSecond());
	    
	    return cal.getTime();
	}
    
	public static void main(String[] args) {
		System.out.println( getDateFromUnknownFormat("2010-07-23 12:07:33") );
	}
	
	
	public static int parseFechaToInteger(String fecha) {
		return SerforDateUtils.parseFechaToInteger(fecha, DateBean.FORMATO_DEFAULT);
	}
	
	public static int parseFechaToInteger(String fecha, String formato) {

		// NOTA: este método no puede ser reemplazado por SunatDateUtils#getIntegerFromDate
		
		
		if ( DateBean.FORMATO_DEFAULT.equals( formato ) ) {
			
			String dia = fecha.substring(0, 2);
			String mes = fecha.substring(3, 5);
			String anio = fecha.substring(6, 10);

			// retorna la representancion entera de una fecha aaaaMMdd
			return Integer.parseInt(anio + mes + dia);			
		}
		
		// agregar if para otros formatos 

		// formato no soportado
		return -1;
	}	
	// FIN RIN15	
	
	
	/**
	 * Este método quita los minutos/segundos/milisegundos del parámetro fecha,
 	 * y si es fecha por default retorna también EMPTY.
	 * @param fecha
	 * @return
	 */      
	 
	


}
