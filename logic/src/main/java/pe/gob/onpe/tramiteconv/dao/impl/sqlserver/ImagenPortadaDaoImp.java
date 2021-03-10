package pe.gob.onpe.tramiteconv.dao.impl.sqlserver;

import org.springframework.stereotype.Repository;
import pe.gob.onpe.tramitedoc.dao.ImagenPortadaDao;
import pe.gob.onpe.tramitedoc.dao.SimpleJdbcDaoBase;









@Repository("imagenPortadaDao")
public class ImagenPortadaDaoImp
  extends SimpleJdbcDaoBase
  implements ImagenPortadaDao
{
  public String changeImgPortada(String nomFile) {
    String vReturn = "NO_OK";
    StringBuilder sqlUpd = new StringBuilder();
    sqlUpd.append("UPDATE IDOSGD.TDTR_PARAMETROS \nSET DE_PAR = ?\nWHERE CO_PAR = 'ID_IMG_PORTADA_SGD'");

    
    try {
      this.jdbcTemplate.update(sqlUpd.toString(), new Object[] { nomFile });
      vReturn = "OK";
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return vReturn;
  }
}
