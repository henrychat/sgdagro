package pe.gob.onpe.tramitedoc.service.impl;

import java.io.FileOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pe.gob.onpe.tramitedoc.bean.DocumentoFileBean;
import pe.gob.onpe.tramitedoc.dao.ImagenPortadaDao;
import pe.gob.onpe.tramitedoc.service.ImagenPortadaService;











@Service("imagenPortadaService")
public class ImagenPortadaServiceImp
  implements ImagenPortadaService
{
  @Autowired
  private ImagenPortadaDao imagenPortadaDao;
  
  public String saveImgPortada(String coUser, DocumentoFileBean fileMeta) {
    String vReturn = "NO_OK";
    FileOutputStream outputStream = null;
    try {
      String nomFile = fileMeta.getNombreArchivo();

      
      String nameFile = "../docroot/recursos/" + nomFile;
      outputStream = new FileOutputStream(nameFile);
      outputStream.write(fileMeta.getArchivoBytes());
      this.imagenPortadaDao.changeImgPortada(nomFile);
      vReturn = "OK";
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        if (outputStream != null) {
          outputStream.close();
        }
      } catch (Exception exception) {}
    } 
    
    return vReturn;
  }
}
