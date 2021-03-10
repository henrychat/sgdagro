package pe.gob.onpe.tramitedoc.dao;

import pe.gob.onpe.tramitedoc.bean.CasillaElectronicaBean;

public interface CasillaElectronicaDao {
	
	 CasillaElectronicaBean  obtenerCasilla( String tipdoc, String numdoc) throws Exception;
	 CasillaElectronicaBean  obtenerCasillaPersonaJuridica( String tipdoc, String numdoc,String numRuc) throws Exception;

}
