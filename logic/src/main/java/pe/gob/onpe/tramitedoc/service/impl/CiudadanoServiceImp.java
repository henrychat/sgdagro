package pe.gob.onpe.tramitedoc.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pe.gob.onpe.tramitedoc.bean.CitizenBean;
import pe.gob.onpe.tramitedoc.dao.CiudadanoDao;
import pe.gob.onpe.tramitedoc.service.CiudadanoService;


@Service("ciudadanoService")
public class CiudadanoServiceImp
  implements CiudadanoService
{
  @Autowired
  private CiudadanoDao ciudadanoDao;
  
  public List<CitizenBean> getCiudadanoList() {
    List<CitizenBean> list = null;
    try {
      list = this.ciudadanoDao.getCiudadanoList();
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    return list;
  }
  
  public List<CitizenBean> getCiudadanoBusqList(String busCiudDocumento, String busCiudApPaterno, String busCiudApMaterno, String busCiudNombres) {
    List<CitizenBean> list = null;
    try {
      list = this.ciudadanoDao.getCiudadanoBusqList(busCiudDocumento, busCiudApPaterno, busCiudApMaterno, busCiudNombres);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    return list;
  }
  
  public String insCiudadano(CitizenBean ciudadano, String codUsuario) {
    String vReturn = "NO_OK";
    
    try {
      ciudadano.setDeApp(ciudadano.getDeApp().toUpperCase().trim());
      ciudadano.setDeApm(ciudadano.getDeApm().toUpperCase().trim());
      ciudadano.setDeNom(ciudadano.getDeNom().toUpperCase().trim());
      vReturn = this.ciudadanoDao.insCiudadano(ciudadano, codUsuario);
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return vReturn;
  }
  
  public CitizenBean getCiudadano(String codCiudadano) {
    CitizenBean ciudadano = null;
    
    try {
      ciudadano = this.ciudadanoDao.getCiudadano(codCiudadano);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    return ciudadano;
  }
  
  public String updCiudadano(CitizenBean ciudadano, String codUsuario) {
    String vReturn = "NO_OK";
    
    try {
      ciudadano.setDeApp(ciudadano.getDeApp().toUpperCase().trim());
      ciudadano.setDeApm(ciudadano.getDeApm().toUpperCase().trim());
      ciudadano.setDeNom(ciudadano.getDeNom().toUpperCase().trim());
      
      vReturn = this.ciudadanoDao.updCiudadano(ciudadano, codUsuario);
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return vReturn;
  }
}
