/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pe.gob.onpe.tramitedoc.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pe.gob.onpe.tramitedoc.bean.BuscarDocTrazabilidadBean;
import pe.gob.onpe.tramitedoc.bean.BuscarDocumentoSeguiEstEmiBean;
import pe.gob.onpe.tramitedoc.bean.DocumentoEmiSeguiBean;
import pe.gob.onpe.tramitedoc.bean.DocumentoSeguiEstEmitidoBean;
import pe.gob.onpe.tramitedoc.bean.DocumentoSeguiTrazaBean;
import pe.gob.onpe.tramitedoc.bean.DocumentosTrazaBean;
import pe.gob.onpe.tramitedoc.bean.ReferenciaConsulBean;
import pe.gob.onpe.tramitedoc.bean.ReporteBean;

/**
 *
 * @author NGilt
 */
public interface SeguiTrazabilidadService {

    public DocumentoEmiSeguiBean getDocumentoEmiSeguiBean(String snuAnn, String snuEmi, String snuDes);

    public List<ReferenciaConsulBean> getDocumentosRefEmiAdmSegui(String snuAnn, String snuEmi);

    public String getRutaReporte(BuscarDocTrazabilidadBean buscarDocumentoSeguiEstEmiBean);

    public List<DocumentoSeguiTrazaBean> getListDocSegui(BuscarDocTrazabilidadBean buscarDocumentoSeguiEstEmiBean);

    public HashMap getBuscaDependenciaEmite(String coDep, String pdeDepEmite);

    public HashMap getDocumentosEnReferencia(BuscarDocTrazabilidadBean buscarDocumentoSeguiEstEmiBean);
   
 
    public ReporteBean getGenerarReporte(BuscarDocTrazabilidadBean buscarDocumentoSeguiEstEmiBean, Map parametros)  throws Exception;
    

    List<DocumentosTrazaBean> existeDocumentoTraza(BuscarDocTrazabilidadBean bean) throws Exception;
    
    List<DocumentosTrazaBean> listaExpedienteOrigen(BuscarDocTrazabilidadBean bean) throws Exception;
    
  
    List<DocumentosTrazaBean> existeDocumentoAccesoTotal(BuscarDocTrazabilidadBean bean,String coDep) throws Exception;
     
     
    
      
}
