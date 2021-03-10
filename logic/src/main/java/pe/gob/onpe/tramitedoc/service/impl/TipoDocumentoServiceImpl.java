package pe.gob.onpe.tramitedoc.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import pe.gob.onpe.tramitedoc.dao.MaestrosDao;
import pe.gob.onpe.tramitedoc.service.TipoDocumentoService;

@Service("tipoDocumentoService")
public class TipoDocumentoServiceImpl implements TipoDocumentoService {
	
	@Autowired
	private MaestrosDao maestrodao;

	@Override
	public String obtenerIndicarSalida(String coTipoDoc) throws Exception {
		 return maestrodao.obtenerIndicarSalida(coTipoDoc);
		
	}

}
