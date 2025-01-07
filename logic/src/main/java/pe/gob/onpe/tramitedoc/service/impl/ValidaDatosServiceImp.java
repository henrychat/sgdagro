package pe.gob.onpe.tramitedoc.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pe.gob.onpe.tramitedoc.dao.ValidaDatosDao;
import pe.gob.onpe.tramitedoc.service.ValidaDatosService;

@Service("validaDatosService")
public class ValidaDatosServiceImp
        implements ValidaDatosService {

    @Autowired
    private ValidaDatosDao validaDatosDao;

    public String getValidaDep(String pcoEmp, String pcoUsu, String pcoDep) {
        String vReturn = null;
        try {
            vReturn = this.validaDatosDao.getValidaDep(pcoEmp, pcoUsu, pcoDep);
        } catch (Exception e) {
            vReturn = null;
        }
        return vReturn;
    }
}
