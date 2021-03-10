package pe.gob.onpe.tramitedoc.dao;

import pe.gob.onpe.tramitedoc.bean.PermisoUsuarioBean;

public interface PermisoDao {
	boolean obtenerPermiso(PermisoUsuarioBean permiso) throws Exception;
}
