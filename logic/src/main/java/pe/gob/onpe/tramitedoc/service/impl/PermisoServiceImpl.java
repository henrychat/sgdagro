package pe.gob.onpe.tramitedoc.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import pe.gob.onpe.tramitedoc.bean.PermisoUsuarioBean;
import pe.gob.onpe.tramitedoc.dao.PermisoDao;
import pe.gob.onpe.tramitedoc.service.PermisoService;

@Service("permisoService")
public class PermisoServiceImpl  implements PermisoService {
	
	@Autowired
	private PermisoDao  permisoDao;

	@Override
	public boolean obtenerPermiso(PermisoUsuarioBean permiso) throws Exception {
		
		return  permisoDao.obtenerPermiso(permiso);
	}

}
