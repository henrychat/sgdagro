package pe.gob.onpe.tramitedoc.dao;

import java.util.List;

import pe.gob.onpe.tramitedoc.bean.BuscarDocTrazabilidadBean;
import pe.gob.onpe.tramitedoc.bean.BuscarDocumentoSeguiEstEmiBean;
import pe.gob.onpe.tramitedoc.bean.DependenciaBean;
import pe.gob.onpe.tramitedoc.bean.DocumentoEmiSeguiBean;
import pe.gob.onpe.tramitedoc.bean.DocumentoSeguiEstEmitidoBean;
import pe.gob.onpe.tramitedoc.bean.DocumentoSeguiTrazaBean;
import pe.gob.onpe.tramitedoc.bean.DocumentosTrazaBean;
import pe.gob.onpe.tramitedoc.bean.ReferenciaConsulBean;


public interface SeguiTrazabilidadDao {
	  public  List<DocumentoSeguiTrazaBean> getListDocSeguiTrazab(BuscarDocTrazabilidadBean buscarDocumentoSeguiEstEmiBean);
	  public DocumentoSeguiEstEmitidoBean existeDocumentoReferenciado(BuscarDocTrazabilidadBean buscarDocumentoSeguiEstEmiBean);
	  public List<DocumentoSeguiEstEmitidoBean> getListaReporteBusqueda(BuscarDocumentoSeguiEstEmiBean buscarDocumentoSeguiEstEmiBean);

  //  public List<DocumentoSeguiEstRecibidoBean> getListDocSeguiEstRec(BuscarDocTrazabilidadBean buscarDocumentoSeguiEstRecBean);
	  public DocumentoEmiSeguiBean getDocumentoEmiSeguiBean(String pnuAnn,String pnuEmi,String pnuDes);
    

    //public List<ReferenciaConsulBean> getDocumentosRefRecepAdmSegui(String snuAnn, String snuEmi);

    public List<DependenciaBean> getListDestinatarioEmi(String pcoDepen, String pdeDepEmite);
    
    public List<DocumentoSeguiEstEmitidoBean> getDocumentosReferenciadoBusq(DocumentoSeguiEstEmitidoBean documentoSeguiEstEmitidoBean, String tiAcceso);
    
    public String getRutaReporte(BuscarDocTrazabilidadBean buscarDocumentoSeguiEstRecBean);
    
    public List<ReferenciaConsulBean> getDocumentosRefRecepAdm(String snuAnn, String snuEmi);
   // public String existeDocumentoTraza(BuscarDocTrazabilidadBean bean) throws Exception;
    public List<DocumentosTrazaBean>  listaExpedienteOrigen(BuscarDocTrazabilidadBean bean) throws Exception;
    public List<DocumentosTrazaBean> existeDocumentoTraza(BuscarDocTrazabilidadBean bean) throws Exception;
    

    
    
  
    
}
