package pe.gob.onpe.tramiteconv.dao.impl.sqlserver;

import java.util.ArrayList;
import java.util.List;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import pe.gob.onpe.tramitedoc.bean.DestinoTSABean;
import pe.gob.onpe.tramitedoc.dao.FirmaTSADao;
import pe.gob.onpe.tramitedoc.dao.SimpleJdbcDaoBase;


@Repository("firmaTSADao")
public class FirmaTSADaoImp
  extends SimpleJdbcDaoBase
  implements FirmaTSADao
{
  public List<DestinoTSABean> getLsDestinoDoc(String nuAnn, String nuEmi) {
    StringBuilder sql = new StringBuilder();
    List<DestinoTSABean> list = new ArrayList<DestinoTSABean>();
    sql.append("SELECT NU_ANN, NU_EMI, NU_DES, TI_DES, NU_RUC_DES\nFROM IDOSGD.TDTV_DESTINOS \nWHERE NU_ANN=? AND NU_EMI=?\nAND TI_DES='02' AND ES_ELI='0'");



    
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(DestinoTSABean.class), new Object[] { nuAnn, nuEmi });
    }
    catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }

  
  public String getCanEntidadTSA(String nuRuc, String tiDoc) {
    String vReturn = "0";
    try {
      vReturn = (String)this.jdbcTemplate.queryForObject("SELECT  COUNT(1)\nFROM IDOSGD.TDTX_TIMESTAMP_ENTIDAD\nWHERE CENT_RUC=? AND CO_TIP_DOC=?\nAND IN_ESTADO='1'", String.class, new Object[] { nuRuc, tiDoc });


    
    }
    catch (Exception e) {
      e.printStackTrace();
    } 
    return vReturn;
  }
}
