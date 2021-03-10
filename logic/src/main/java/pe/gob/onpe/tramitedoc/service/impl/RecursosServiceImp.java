package pe.gob.onpe.tramitedoc.service.impl;

import org.springframework.stereotype.Service;
import pe.gob.onpe.tramitedoc.service.RecursosService;





























@Service("recursosService")
public class RecursosServiceImp
  implements RecursosService
{
  public String getAllObjects() {
    StringBuffer retval = new StringBuffer();
    
    boolean sucessfull = false;
    StringBuffer prepare = new StringBuffer();










    
    try {
      sucessfull = true;
    }
    catch (Exception e) {
      
      sucessfull = false;
      e.printStackTrace();
    } 
    retval.append("{'retval':");
    retval.append(sucessfull);

    
    retval.append("}");
    
    return retval.toString();
  }
}
