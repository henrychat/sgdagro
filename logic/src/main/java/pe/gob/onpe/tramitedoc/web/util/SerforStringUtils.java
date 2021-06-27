package pe.gob.onpe.tramitedoc.web.util;



import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.util.StringUtils;



/**
 * 
 * @author 
 *
 */
public class SerforStringUtils {

	public static boolean isLengthEqualsToNumber(String string, int length)
	{		
		return (string != null) && (string.length() == length); 
	}

	/**
	 * Verifica si la longitud de la cadena string es mayor que el numero dado por length
	 * @param string
	 * @param length
	 * @return
	 */
	public static boolean isLengthGreaterThanNumber(String string, int length)
	{		
		return (string != null) && (string.length() > length); 
	}

	
	/**
	 * Verifica si la longitud de la cadena string es mayor o igual que el numero dado por length
	 * @param string
	 * @param length
	 * @return
	 */
	public static boolean isLengthGreaterOrEqualsThanNumber(String string, int length)
	{		
		return (string != null) && (string.length() >= length); 
	}

	/**
	 * Verifica si la longitud de la cadena string es menor que el numero dado por length
	 * @param string
	 * @param length
	 * @return
	 */
	public static boolean isLengthLessThanNumber(String cadena, int longitud)
	{		
		return (cadena != null) && (cadena.length() < longitud); 
	}






	public static int length(String str)
	{
		if(str == null)
			return 0;
		
		return str.length();
	}
	
	public static boolean isStringInList(String string, String list)
	{
		if(list == null || string == null)
				return false;
		
		 String arrStr [] = list.split(",");
		 List<String> listStr = Arrays.asList(arrStr);
		 
		 return (listStr.contains(string));

	}
	
	/**
	 * Compara la cadena valorABuscar con el listado de valores enviados como parametros
	 * @param valorABuscar
	 * @param listDeCadenasCompara
	 * @return
	 */
	public static boolean isStringInList(String valorABuscar, String ... listDeCadenasCompara){
		if(listDeCadenasCompara == null || valorABuscar == null){
			return false;
		}				
		
		for (String  cadenaComparar : listDeCadenasCompara) {
			if(valorABuscar.equals(cadenaComparar)){
				return true;
			}
		}
		
		return false;
	}
	



	/**
	 * TODO implementar esta funcion
	 * @param char1
	 * @param first
	 * @param last
	 * @return
	 */
	public static boolean isCharBetween(char char1, char first, char last)
	{		
		return char1 >=first && char1<=last;
	}

	/**
	 * TODO implementar
	 * @param str
	 * @param first
	 * @param last
	 * @return
	 */
	public static boolean isCharBetween(String str, char first, char last)
	{
		if( length(str) == 1 ) 
			return isCharBetween(str.toCharArray()[0],first, last );
		
		return false;
	}

	

	


	public static boolean include(String valor, String[] lista){
		if (valor==null) return false;
		for (int i=0;i<lista.length;i++){
			if (valor.equals(lista[i]))
				return true;
		}
		return false;
	}
	
	/**
	 * 
	 * @param aEmailAddress
	 * @return
	 */
//	public static boolean isValidEmailAddress(String aEmailAddress){
//	    
//		if (aEmailAddress == null) return false;
//	    
//	    boolean result = true;
//	    
//	    try {
//	      InternetAddress emailAddr = new InternetAddress(aEmailAddress);
//	      emailAddr.validate();
//	      
//	    } catch (AddressException ex){
//	      result = false;
//	    }
//	    
//	    return result;
//	}
	
	
	
	
	 public static boolean parseFormat(String formato,String input){
		 Pattern p=Pattern.compile(formato);
		 Matcher m = p.matcher(input);
		 return m.matches();
		 
	 }
	 


	 public static int indexOf(String string, String arg){
		 
		 if(string != null)
			 return string.indexOf(arg);
		 
		 return -1;
		 
	 }
	 
	public static String toStringObj(Object obj ) {
		
		if( obj == null ) return null;
		
		if(obj  instanceof String ) return (String)obj;
		else if(obj  instanceof Integer ) return ((Integer)obj).toString();
		else if(obj  instanceof Long ) return ((Long)obj).toString();
		else if(obj  instanceof BigDecimal ) return ((BigDecimal)obj).toString();
		else obj.toString();
		
		return null;
	}
	
	public static String toNotNull(String s){
		if(s==null) return "";
		else return s;
	}
	

	
	
	public static boolean contains( String texto, String cadena) {
		texto = texto == null ? "" : texto;
		cadena = cadena == null ? "" : cadena;
		return texto.toLowerCase().contains( cadena.toLowerCase());
	}


	public static String[] getArray(String cadena, String separador ) {
		int total = 0;
		for( int i=0; i<cadena.length(); i++) {
			if(cadena.substring(i,i+1).equals(separador)) {
				total = total + 1;
			}
		}

		String[] arrRpta = new String[total+1];
		String dato = "";
		int tamano = 0;
		for( int i=0; i<cadena.length(); i++) {
			if(cadena.substring(i,i+1).equals(separador)) {
				arrRpta[tamano] = dato;
				tamano = tamano + 1;
				dato = "";
			}
			else {
				dato = dato.concat(cadena.substring(i,i+1)); 
			}
		}
		arrRpta[tamano] = dato;
		return arrRpta;
	}
	
	/**
	 * Quita caracteres especiales a la cadena recibida como parametro
	 * @param cadena
	 * @return
	 */
	public static String quitarCaracteresEspeciales(String cadena){
		String cadenaAEvaluar = cadena == null ? "" : cadena;
		return Normalizer.normalize(cadenaAEvaluar, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
	}
	// INICIO RIN15
		public static String parseToJSFormat(String cadenaConTildes) {
			if (cadenaConTildes == null) {
				return null;
			}
			StringBuilder sb = new StringBuilder((int) (1.4 * cadenaConTildes.length()));

			char c = 'c';
			int nro = cadenaConTildes.length();
			for (int i = 0; i < nro; i++) {
				c = cadenaConTildes.charAt(i);
				if (c == 'á') {
					sb.append("\\xE1");
				} else if (c == 'é') {
					sb.append("\\xE9");
				} else if (c == 'í') {
					sb.append("\\xED");
				} else if (c == 'ó') {
					sb.append("\\xF3");
				} else if (c == 'ú') {
					sb.append("\\xFA");
				} else if (c == 'ñ') {
					sb.append("\\xF1");
				} else if (c == 'Á') {
					sb.append("\\xC1");
				} else if (c == 'É') {
					sb.append("\\xC9");
				} else if (c == 'Í') {
					sb.append("\\xCD");
				} else if (c == 'Ó') {
					sb.append("\\xD3");
				} else if (c == 'Ú') {
					sb.append("\\xDA");
				} else if (c == 'Ñ') {
					sb.append("\\xD1");
				} else {
					sb.append(c);
				}
			}
			return sb.toString().replace("  ", " &nbsp;");
		}

		public static String parseToCuteFormat(String cadenaConTildes) {

			if (cadenaConTildes == null) {
				return null;
			}
			StringBuilder sb = new StringBuilder((int) (1.4 * cadenaConTildes.length()));

			char c = 'c';
			int nro = cadenaConTildes.length();
			for (int i = 0; i < nro; i++) {
				c = cadenaConTildes.charAt(i);
				if (c == 'á') {
					sb.append("&aacute;");
				} else if (c == 'é') {
					sb.append("&eacute;");
				} else if (c == 'í') {
					sb.append("&iacute;");
				} else if (c == 'ó') {
					sb.append("&oacute;");
				} else if (c == 'ú') {
					sb.append("&uacute;");
				} else if (c == 'ñ') {
					sb.append("&ntilde;");
				} else if (c == 'Á') {
					sb.append("&Aacute;");
				} else if (c == 'É') {
					sb.append("&Eacute;");
				} else if (c == 'Í') {
					sb.append("&Iacute;");
				} else if (c == 'Ó') {
					sb.append("&Oacute;");
				} else if (c == 'Ú') {
					sb.append("&Uacute;");
				} else if (c == 'Ñ') {
					sb.append("&Ntilde;");
				} else {
					sb.append(c);
				}
			}
			return sb.toString().replace("  ", " &nbsp;");
		}	
		// FIN RIN15
	
		public static boolean isEmpty(String str) {
			return StringUtils.isEmpty(str);
		}

	 public static String toString(Object valor) {
	     String campo = null;
	     if( valor != null ){
	    	 campo = String.valueOf(valor);
	     }

	     return campo;
	 }

}
