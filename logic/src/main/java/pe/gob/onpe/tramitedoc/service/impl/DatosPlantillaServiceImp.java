package pe.gob.onpe.tramitedoc.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pe.gob.onpe.tramitedoc.bean.PlantillaDocx;
import pe.gob.onpe.tramitedoc.dao.DatosPlantillaDao;
import pe.gob.onpe.tramitedoc.service.DatosPlantillaService;









@Service("datosPlantillaService")
public class DatosPlantillaServiceImp
  implements DatosPlantillaService
{
  @Autowired
  private DatosPlantillaDao datosPlantillaDao;
  
  public List<PlantillaDocx> getLstPlantillasDocx(String pcoDep, String idPlantilla) {
    List<PlantillaDocx> list = null;
    try {
      list = this.datosPlantillaDao.getLstPlantillasDocx(pcoDep, idPlantilla);
    }
    catch (Exception ex) {
      list = null;
      ex.printStackTrace();
    } 
    
    return list;
  }
}
