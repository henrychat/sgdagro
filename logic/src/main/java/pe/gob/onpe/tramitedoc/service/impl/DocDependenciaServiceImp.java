package pe.gob.onpe.tramitedoc.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pe.gob.onpe.tramitedoc.bean.DocumentoDependenciaBean;
import pe.gob.onpe.tramitedoc.bean.MotivoBean;
import pe.gob.onpe.tramitedoc.bean.MotivoDocumentoDependenciaBean;
import pe.gob.onpe.tramitedoc.dao.DocDependenciaDao;
import pe.gob.onpe.tramitedoc.service.DocDependenciaService;



@Service("docDependenciaService")
public class DocDependenciaServiceImp
  implements DocDependenciaService
{
  @Autowired
  private DocDependenciaDao docDependenciaDao;
  
  public DocumentoDependenciaBean getDocDependencia(String codDependencia, String codTipDoc) {
    DocumentoDependenciaBean documentoDependenciaBean = null;
    try {
      documentoDependenciaBean = this.docDependenciaDao.getDocDependencia(codDependencia, codTipDoc);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    return documentoDependenciaBean;
  }























  
  public List<DocumentoDependenciaBean> getAllDocXDependencia(String codDependencia) {
    List<DocumentoDependenciaBean> list = null;
    
    try {
      list = this.docDependenciaDao.getDocDependenciaList(codDependencia);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    return list;
  }
  
  public List<DocumentoDependenciaBean> getDocDependenciaList(String codDependencia) {
    List<DocumentoDependenciaBean> list = null;
    try {
      list = this.docDependenciaDao.getDocDependenciaList(codDependencia);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    return list;
  }
  
  public List<DocumentoDependenciaBean> getDocDependenciaFaltantesList(String codDependencia) {
    List<DocumentoDependenciaBean> list = null;
    try {
      list = this.docDependenciaDao.getDocDependenciaFaltantesList(codDependencia);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    return list;
  }
  
  public String insDocDependencia(DocumentoDependenciaBean documentoDependenciaBean, String coUsuario) {
    String vReturn = "NO_OK";
    try {
      documentoDependenciaBean.setEsEli("0");
      documentoDependenciaBean.setCoUseCre(coUsuario);
      documentoDependenciaBean.setCoUseMod(coUsuario);
      
      documentoDependenciaBean.setEsOblFirma("1");

      
      vReturn = this.docDependenciaDao.insDocDependencia(documentoDependenciaBean);
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return vReturn;
  }
  
  public String eliDocDependencia(DocumentoDependenciaBean documentoDependenciaBean) {
    String vReturn = "NO_OK";
    try {
      vReturn = this.docDependenciaDao.eliDocDependencia(documentoDependenciaBean);
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return vReturn;
  }
  
  public String updDocDependencia(DocumentoDependenciaBean documentoDependenciaBean, String codDocReempl, String coUsuario) {
    String vReturn = "NO_OK";
    try {
      documentoDependenciaBean.setCoUseMod(coUsuario);
      vReturn = this.docDependenciaDao.updDocDependencia(documentoDependenciaBean, codDocReempl);
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return vReturn;
  }
  public List<MotivoBean> getMotDependenciaList(String codDependencia, String codDoc) {
    List<MotivoBean> list = null;
    try {
      list = this.docDependenciaDao.getMotDependenciaList(codDependencia, codDoc);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    return list;
  }
  
  public List<MotivoBean> getMotFaltantesList(String codDependencia, String codDoc) {
    List<MotivoBean> list = null;
    try {
      list = this.docDependenciaDao.getMotFaltantesList(codDependencia, codDoc);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    return list;
  }
  
  public String insMotDocDependencia(MotivoDocumentoDependenciaBean motDocDep) {
    String vReturn = "NO_OK";
    try {
      vReturn = this.docDependenciaDao.insMotDocDependencia(motDocDep);
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return vReturn;
  }
  public String updMotDocDependencia(MotivoDocumentoDependenciaBean motDocDep, String codMotReempl) {
    String vReturn = "NO_OK";
    try {
      vReturn = this.docDependenciaDao.updMotDocDependencia(motDocDep, codMotReempl);
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return vReturn;
  }
  public String eliMotDocDependencia(MotivoDocumentoDependenciaBean motDocDep) {
    String vReturn = "NO_OK";
    try {
      vReturn = this.docDependenciaDao.eliMotDocDependencia(motDocDep);
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return vReturn;
  }
}
