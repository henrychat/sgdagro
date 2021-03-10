package pe.gob.onpe.tramitedoc.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pe.gob.onpe.tramitedoc.bean.DocumentoDatoBean;
import pe.gob.onpe.tramitedoc.dao.NotificacionDao;
import pe.gob.onpe.tramitedoc.service.NotificacionService;
import pe.gob.onpe.tramitedoc.web.util.ApplicationProperties;





















@Service("notificacionService")
public class NotificacionServiceImp   implements NotificacionService
{
  @Autowired
  ApplicationProperties applicationProperties;
  @Autowired
  private NotificacionDao notificacionDao;
  
  private DocumentoDatoBean getDatosDoc(String pnuAnn, String pnuEmi) {
    DocumentoDatoBean docDatoBean = null;
    try {
      docDatoBean = this.notificacionDao.getDatosDoc(pnuAnn, pnuEmi);
    } catch (Exception e) {
      docDatoBean = null;
    } 
    return docDatoBean;
  }



  
  public String procesarNotificacion(String pnuAnn, String pnuEmi, String coUser) {
    String vResult = "NO_OK";


























































    
    return vResult;
  }













































































  
  public String notificarVistoBueno(String nuEmi, String nuAnn) {
    String result;
    try {
      result = this.notificacionDao.notificarVistoBueno(nuEmi, nuAnn);
    } catch (Exception e) {
      result = null;
    } 
    return result;
  }


  
  public String getLsEmpleadoNotificar(String nuEmi, String nuAnn, String detalleDoc) {
    String result;
    try {
      result = this.notificacionDao.getLsEmpleadoNotificar(nuEmi, nuAnn, detalleDoc);
    } catch (Exception e) {
      result = null;
    } 
    return result;
  }




@Override
public String getLsMesaVirtualNotificar(String nuEmi, String nuAnn, String detalleDoc) {
	// TODO Auto-generated method stub
	return null;
}




@Override
public String getLsTupaNotificar(String nuEmi, String nuAnn, String detalleDoc) {
	// TODO Auto-generated method stub
	return null;
}
}
