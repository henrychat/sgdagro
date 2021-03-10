package pe.gob.onpe.tramitedoc.service.impl;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pe.gob.onpe.tramitedoc.bean.DocumentoEmiBean;
import pe.gob.onpe.tramitedoc.bean.DocumentoFileBean;
import pe.gob.onpe.tramitedoc.bean.DocumentoObjBean;
import pe.gob.onpe.tramitedoc.dao.EmiDocumentoAdmDao;
import pe.gob.onpe.tramitedoc.service.EmiDocumentoAltaService;















@Service("emiDocumentoAltaService")
public class EmiDocumentoAltaServiceImp
  implements EmiDocumentoAltaService
{
  private static Logger logger = Logger.getLogger("SGDEmiDocumentoAltaService");
  
  @Autowired
  private EmiDocumentoAdmDao emiDocumentoAdmDao;
  
  public String cargaDocumentoEmi(String coUsu, String pnuAnn, String pnuEmi, DocumentoFileBean pfileAnexo) {
    String vReturn = "NO_OK";
    DocumentoObjBean docObjBean = new DocumentoObjBean();

    
    try {
      docObjBean.setNuAnn(pnuAnn);
      docObjBean.setNuEmi(pnuEmi);
      docObjBean.setTiCap("01");
      docObjBean.setNombreArchivo(pfileAnexo.getNombreArchivo());
      docObjBean.setCoUseMod(coUsu);
      docObjBean.setTipoDoc(docObjBean.getNombreArchivo().substring(docObjBean.getNombreArchivo().lastIndexOf('.') + 1));

      
      docObjBean.setNombreArchivo(docObjBean.getNombreArchivo().substring(docObjBean.getNombreArchivo().lastIndexOf('\\') + 1));
      
      byte[] archivoByte = pfileAnexo.getArchivoBytes();
      docObjBean.setDocumento(archivoByte);
      docObjBean.setNuTamano((int)Math.round(archivoByte.length / 1024.0D));
      
      vReturn = this.emiDocumentoAdmDao.updDocumentoObj(docObjBean);
    } catch (Exception ex) {
      StringBuffer mensaje = new StringBuffer();
      vReturn = "Error en Leer Documento de Repositorio";
      mensaje.append(docObjBean.getNuAnn() + "." + docObjBean.getNuEmi());
      logger.error(mensaje, ex);
    } 
    
    return vReturn;
  }


  
  public String changeToProyecto(DocumentoEmiBean documentoEmiBean) {
    String vReturn = "NO_OK";
    
    String pcoUseMod = documentoEmiBean.getCoUseMod();
    String pcoEmpRes = documentoEmiBean.getCoEmpRes();
    try {
      documentoEmiBean = this.emiDocumentoAdmDao.getEstadoDocumento(documentoEmiBean.getNuAnn(), documentoEmiBean.getNuEmi());
      if (documentoEmiBean != null && (documentoEmiBean.getEsDocEmi().equals("7") || documentoEmiBean.getEsDocEmi().equals("0"))) {
        String pesDocEmi = documentoEmiBean.getEsDocEmi();
        vReturn = this.emiDocumentoAdmDao.verificarDocumentoLeido(documentoEmiBean.getNuAnn(), documentoEmiBean.getNuEmi());
        if (vReturn != null && vReturn.equals("0")) {
          documentoEmiBean.setEsDocEmi("5");
          documentoEmiBean.setCoUseMod(pcoUseMod);
          vReturn = this.emiDocumentoAdmDao.updEstadoDocumentoEmiAdm(documentoEmiBean);
          if (!vReturn.equals("OK")) {
            vReturn = "Error al Cambiar a Proyecto el Documento.";
          }
        } else {
          vReturn = "Documento ya fue leido por los Destinatarios.";
        } 
      } else {
        vReturn = "Documento no puede cambiar de Estado.";
      } 
    } catch (Exception e) {
      vReturn = e.getMessage();
    } 
    return vReturn;
  }
}
