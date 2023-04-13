package pe.gob.onpe.tramiteconv.dao.impl.sqlserver;

import java.util.ArrayList;
import java.util.List;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.ParameterizedBeanPropertyRowMapper;
import org.springframework.stereotype.Repository;
import pe.gob.onpe.tramitedoc.bean.TemaBean;
import pe.gob.onpe.tramitedoc.dao.SimpleJdbcDaoBase;
import pe.gob.onpe.tramitedoc.dao.TemaDao;

@Repository("temaDao")
public class TemaDaoImp
        extends SimpleJdbcDaoBase
        implements TemaDao {

    public List<TemaBean> getListTema(TemaBean buscarTemaBean) {
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT   co_tema,  de_tema FROM IDOSGD.tdtr_tema WHERE ES_ELI='0' AND CO_DEPENDENCIA=? ");
        List<TemaBean> list = new ArrayList<TemaBean>();
        try {
            list = this.jdbcTemplate.query(sql.toString(), (RowMapper) ParameterizedBeanPropertyRowMapper.newInstance(TemaBean.class), new Object[]{buscarTemaBean
                .getCoDependencia()});
        } catch (EmptyResultDataAccessException e) {
            list = null;
        } catch (Exception e) {
            list = null;
            e.printStackTrace();
        }

        return list;
    }

    public String updTemaDestinos(String codigoEmision, String codTema) {
        String vReturn = "NO_OK";
        StringBuilder sqlIns = new StringBuilder();
        sqlIns.append("UPDATE IDOSGD.TDTV_DESTINOS SET CO_TEMA =" + codTema + " WHERE NU_ANN+NU_EMI+NU_DES = '" + codigoEmision + "' ");
        try {
            this.jdbcTemplate.update(sqlIns.toString());
            vReturn = "OK";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return vReturn;
    }

    public String updTemaRemitos(String codigoEmision, String codTema) {
        String vReturn = "NO_OK";
        StringBuilder sqlIns = new StringBuilder();
        sqlIns.append("UPDATE IDOSGD.TDTV_REMITOS SET CO_TEMA =" + codTema + " WHERE NU_ANN+NU_EMI = '" + codigoEmision + "'  ");
        try {
            this.jdbcTemplate.update(sqlIns.toString());
            vReturn = "OK";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return vReturn;
    }

    public String getEditTemaRemitos(String codigoEmision) {
        String vReturn = "NO_OK";
        StringBuilder sqlIns = new StringBuilder();
        sqlIns.append("SELECT CO_TEMA FROM  IDOSGD.TDTV_REMITOS     WHERE NU_ANN+NU_EMI = '" + codigoEmision + "'  ");
        try {
            this.jdbcTemplate.update(sqlIns.toString());
            String codTema = (String) this.jdbcTemplate.queryForObject(sqlIns.toString(), String.class);
            vReturn = codTema;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return vReturn;
    }

    public String getEditTemaDestinos(String codigoEmision) {
        String vReturn = "NO_OK";
        StringBuilder sqlIns = new StringBuilder();
        sqlIns.append("SELECT CO_TEMA FROM  IDOSGD.TDTV_DESTINOS     WHERE NU_ANN+NU_EMI+NU_DES = '" + codigoEmision + "'  ");
        try {
            this.jdbcTemplate.update(sqlIns.toString());
            String codTema = (String) this.jdbcTemplate.queryForObject(sqlIns.toString(), String.class);
            vReturn = codTema;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return vReturn;
    }
}
