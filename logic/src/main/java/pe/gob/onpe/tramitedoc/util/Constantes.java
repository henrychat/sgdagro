package pe.gob.onpe.tramitedoc.util;

/**
 * Created by IntelliJ IDEA.
 * User: crosales
 * Date: 09/02/12
 * Time: 08:08 AM
 * To change this template use File | Settings | File Templates.
 */
public interface Constantes {
    public static String USER_COOKIE_ID = "Usuario";
    public static String USER_ACCESS_COOKIE_ID = "Acceso";
    public static String SESSION_SATTUS = "State";
    public static int TI_IDENTIFICACION = 0;

    public static String PAGE_NUMBER = "pg";
    public static int ROWS_X_PAGE = 4;

    public static String ETAPA_CREACION = "0";
    public static String ETAPA_PDF_CARGADO = "2";
    public static String ETAPA_PARA_CONTROL_CALIDAD="3";
    public static String ETAPA_ANULADO="9";
    public static String ETAPA_PUBLICADO="1";
    public static String ETAPA_RECHAZADO="4";


    public static String PDF_RESOLUCION = "R";
    public static String PDF_ANEXO = "A";
    public static String PDF_ANTECEDENTE = "T";
    
    //para el motor de base de datos
    int DB_MYSQL = 1;
    int DB_ORACLE = 2;
    int DB_POSTGRES = 3;
    int DB_MSSQL = 4;    
    
    int nroRegistrosXPagina = 10;    
    
    //Para la paginacion
    String paginacion_atras = "P_ATRAS";
    String paginacion_siguiente = "P_SIGUIENTE";
    String paginacion_ultimo = "P_ULTIMO";
    String paginacion_primero = "P_PRIMERO";
    String paginacion_actualizar = "P_ACTUALIZAR";
    String paginacion_buscar = "P_BUSCAR";   
    
    public static final String CO_TRAMITE_ORIGINAL    = "0";
    public static final String DE_TRAMITE_ORIGINAL    = "ORIGINAL";
    public static final String CO_TRAMITE_COPIA    = "1";
    public static final String DE_TRAMITE_COPIA    = "COPIA";        
    public static final String CO_TRAMITE_ATENDER    = "4";
    public static final String DE_TRAMITE_ATENDER    = "ATENDER";
    public static final String CO_TRAMITE_FINES    = "F";
    public static final String DE_TRAMITE_FINES    = "PARA CONOCIMINETO Y FINES";    
    
    public static final String TIPO_EMISION_DEPENDENCIAS = "01";
    
    public static final String COD_PERMISO_DOC_EMITIDOS = "M0501";
    public static final String COD_PERMISO_DOC_MP_OFICINA = "M0502";
    public static final String COD_PERMISO_REVERTIR_DOCUMENTO = "M0504";
    public static final String COD_PERMISO_ENVIAR_EXTERN_DOCUMENTOS = "M0505";
    public static final String COD_PERMISO_DER_MENSAJ_EXT_SGD = "M0506";
    public static final String COD_PERMISO_ADM_UUOO = "M0119";
    public static final String COD_PERMISO_TRAZ_AVZ = "M030113";
    
    
    public static final String  COD_LOCAL_CENTRAL="001";
    public static final String COD_MENSAJERIA_CENTRAL="10602";
    
    public static final String CO_TIPO_ENVIO_MENSAJERIA    = "0";
    public static final String CO_TIPO_ENVIO_ENTREGA_DIRECTA    = "1";
    
    
    
    
    
    public static final String COD_SISTEMA_SGD = "9";
    public static final String COD_NOTIFICACION_ELECTRONICA = "3";
    //3
    public static final String TIPO_ACCESO_TOTAL = "0";
}
