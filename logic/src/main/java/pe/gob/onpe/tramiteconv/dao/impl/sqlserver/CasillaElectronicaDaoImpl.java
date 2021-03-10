package pe.gob.onpe.tramiteconv.dao.impl.sqlserver;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import pe.gob.onpe.tramitedoc.bean.CasillaElectronicaBean;

import pe.gob.onpe.tramitedoc.dao.CasillaElectronicaDao;
import pe.gob.onpe.tramitedoc.dao.SimpleJdbcDaoBase;
@Repository("casillaElectronicaDao")
public class CasillaElectronicaDaoImpl extends SimpleJdbcDaoBase implements CasillaElectronicaDao{

	@Override
	public CasillaElectronicaBean obtenerCasilla( String tipdoc, String numdoc) throws Exception {
		 
			    
			    StringBuffer sqlQry = new StringBuffer();
			    sqlQry.append(" SELECT TP.TX_DESCRIPCION tipoUsuario,null ruc ,MP.[TX_NRO_DOCUMENTO_IDENTIDAD] nroDocIdentidad");
			    sqlQry.append(" ,MP.[TX_APELLIDO_PATERNO] apePaterno ,MP.[TX_APELLIDO_MATERNO] apeMaterno, MP.[TX_NOMBRES] nombres ");
			    sqlQry.append(" FROM [MPV].[T_MVC_SOLICITANTE] MS ");
			    sqlQry.append(" LEFT JOIN [MPV].[T_MAP_PERSONA] MP ON MP.NU_ID_PERSONA = MS.NU_ID_PERSONA");
			    sqlQry.append(" LEFT JOIN [MPV].[T_MAP_TIPOPERSONA] TP ON TP.NU_ID_TIPOPERSONA = MS.NU_ID_TIPOPERSONA");
			    sqlQry.append(" WHERE   TP.TX_CODIGO=?  and MP.TX_NRO_DOCUMENTO_IDENTIDAD=?    ");
			    //--TX_NRO_DOCUMENTO_IDENTIDAD dni, carne, pasaporte
			    //--TX_CODIGO 03 CIUDADANO, 02 PERSONA JURIDICA, 04 OTROS
//			    private String ruc;
//				private String apePaterno;
//				private String apeMaterno;
//				private String nombres;
//				private String nroDocIdentidad;
//				private String estado;	      
			    	  
			    CasillaElectronicaBean  casilla=null;;
			    
			    try {
			    	casilla = (CasillaElectronicaBean)this.jdbcTemplate.queryForObject(sqlQry.toString(),  (RowMapper)BeanPropertyRowMapper.newInstance(CasillaElectronicaBean.class),
			    			new Object[] { tipdoc,numdoc });
			   
			    } catch(EmptyResultDataAccessException ex) {
			    	// vacio
			    	return null;
			    
			    	
			    } catch (Exception e) {
			      e.printStackTrace();
			      throw e;
			    } 
			    return casilla;
			  
	}

	@Override
	public CasillaElectronicaBean obtenerCasillaPersonaJuridica( String tipdoc, String numdoc,String numRuc) throws Exception{

	    String vReturn = "2";
	    StringBuffer sqlQry = new StringBuffer();
	    sqlQry.append(" SELECT TP.TX_DESCRIPCION tipoUsuario,null  ruc,MP.[TX_NRO_DOCUMENTO_IDENTIDAD] nroDocIdentidad ");
	    sqlQry.append(" ,MP.[TX_APELLIDO_PATERNO] apePaterno ,MP.[TX_APELLIDO_MATERNO] apeMaterno, MP.[TX_NOMBRES] nombres ");
	    sqlQry.append(" FROM [MPV].[T_MVC_SOLICITANTE] MS ");
	    sqlQry.append(" LEFT JOIN [MPV].[T_MAP_PERSONA] MP ON MP.NU_ID_PERSONA = MS.NU_ID_PERSONA");
	    sqlQry.append(" LEFT JOIN [MPV].[T_MAP_TIPOPERSONA] TP ON TP.NU_ID_TIPOPERSONA = MS.NU_ID_TIPOPERSONA");
	    sqlQry.append(" LEFT JOIN [MPV].[T_MAP_PERSONA_JURIDICA] MJ ON MJ.NU_ID_JURIDICA = MS.NU_ID_JURIDICA ");
	    sqlQry.append(" WHERE  TP.TX_CODIGO=?  and MP.TX_NRO_DOCUMENTO_IDENTIDAD=?   and   MJ.TX_NRO_RUC =? ");
	    //--TX_NRO_DOCUMENTO_IDENTIDAD dni, carne, pasaporte
	    //--TX_CODIGO 03 CIUDADANO, 02 PERSONA JURIDICA, 04 OTROS
	    

	    	      
	    CasillaElectronicaBean  casilla=null;;
	    
	    try {
	    	casilla = (CasillaElectronicaBean)this.jdbcTemplate.queryForObject(sqlQry.toString(),  (RowMapper)BeanPropertyRowMapper.newInstance(CasillaElectronicaBean.class), 
	    			new Object[] {   tipdoc,  numdoc,numRuc });
	    
	    } catch(EmptyResultDataAccessException ex) {
	    	// vacio
	    	return null;
	    
	    } catch (Exception e) {
	      e.printStackTrace();
	      throw e;
	    } 
	    return casilla;
	}

}
