package pe.gob.onpe.tramitedoc.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pe.gob.onpe.tramitedoc.bean.DestinoResBen;
import pe.gob.onpe.tramitedoc.bean.DocumentoAnexoBean;
import pe.gob.onpe.tramitedoc.bean.RemitosResBean;
import pe.gob.onpe.tramitedoc.dao.DocumentoBasicoDao;
import pe.gob.onpe.tramitedoc.service.DocumentoBasicoService;











@Service("documentoBasicoService")
public class DocumentoBasicoServiceImp
  implements DocumentoBasicoService
{
  @Autowired
  private DocumentoBasicoDao documentoBasicoDao;
  
  public RemitosResBean getRemitoResumen(String pnuAnn, String pnuEmi) {
    RemitosResBean docRemitoBean = null;
    docRemitoBean = this.documentoBasicoDao.getRemitoResumen(pnuAnn, pnuEmi);
    return docRemitoBean;
  }

  
  public DestinoResBen getDestinoDocumento(String pnuAnn, String pnuEmi, String pnuDes) {
    DestinoResBen docDestinoBean = null;
    docDestinoBean = this.documentoBasicoDao.getDestinoResumen(pnuAnn, pnuEmi, pnuDes);
    return docDestinoBean;
  }

  
  public DestinoResBen getDestinoResumen(String pnuAnn, String pnuEmi, String pnuDes) {
    DestinoResBen docDestinoBean = null;
    
    if (pnuDes != null && !pnuDes.equals("N")) {
      docDestinoBean = this.documentoBasicoDao.getDestinoResumen(pnuAnn, pnuEmi, pnuDes);
    } else {
      List<DestinoResBen> list = null;
      list = this.documentoBasicoDao.getDestinoResumenList(pnuAnn, pnuEmi);
      docDestinoBean = new DestinoResBen();
      int i = 0; if (i < list.size()) {
        docDestinoBean = list.get(i);
      }
    } 
    
    return docDestinoBean;
  }


  
  public List<DestinoResBen> getDestinoResumenList(String pnuAnn, String pnuEmi) {
    List<DestinoResBen> list = null;
    list = this.documentoBasicoDao.getDestinoResumenList(pnuAnn, pnuEmi);
    return list;
  }


  
  public List<DocumentoAnexoBean> getAnexosList(String pnuAnn, String pnuEmi) {
    List<DocumentoAnexoBean> list = null;
    list = this.documentoBasicoDao.getAnexosList(pnuAnn, pnuEmi);
    return list;
  }

  
  public List<DocumentoAnexoBean> getAnexosMsjList(String pnuAnn, String pnuEmi) {
    List<DocumentoAnexoBean> list = null;
    list = this.documentoBasicoDao.getAnexosMsjList(pnuAnn, pnuEmi);
    return list;
  }





  
  public String verificaCargaDoc(String pnuAnn, String pnuEmi) { return this.documentoBasicoDao.verificaCargaDoc(pnuAnn, pnuEmi); }
}
