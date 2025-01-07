package pe.gob.onpe.tramiteconv.dao.impl.sqlserver;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import pe.gob.onpe.tramitedoc.bean.PermisoUsuarioBean;
import pe.gob.onpe.tramitedoc.dao.PermisoDao;
import pe.gob.onpe.tramitedoc.dao.SimpleJdbcDaoBase;

@Repository("permisoDao")
public class PermisoDaoImpl extends SimpleJdbcDaoBase implements PermisoDao {

    @Override
    public boolean obtenerPermiso(PermisoUsuarioBean permiso) throws Exception {

        StringBuilder sql = new StringBuilder();
        sql.append("Select P.COD_USER,P.COD_APLICA,P.COD_OPC from  IDOSGD.SEG_USER_APLICA_OPC  P ");
        sql.append(" inner join IDOSGD.SEG_USUARIOS1 U ON (P.COD_USER=U.COD_USER AND U.ES_USUARIO='A') ");
        sql.append(" WHERE  U.CEMP_CODEMP=?  AND  P.ES_HABILITADO='1' AND COD_APLICA=?  AND P.COD_OPC=?  ");

        PermisoUsuarioBean permisoBean = null;

        try {
            permisoBean = (PermisoUsuarioBean) this.jdbcTemplate.queryForObject(sql.toString(), (RowMapper) BeanPropertyRowMapper.newInstance(PermisoUsuarioBean.class),
                    new Object[]{permiso.getCempCodEmp(), permiso.getCodAplica(), permiso.getCodOpc()});
        } catch (EmptyResultDataAccessException e) {
            if (permisoBean == null) {
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return true;

    }
}
