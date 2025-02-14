/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pe.gob.segdi.wsentidad.ws;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import java.util.List;

/**
 *
 * @author mvaldera
 */
@WebService(name = "PcmIMgdEntidadPortType", targetNamespace = "http://ws.wsentidad.segdi.gob.pe/")
@XmlSeeAlso({
    ObjectFactory.class
})
public interface PcmIMgdEntidadPortType {


    /**
     *
     * @param sidcatent
     * @return
     *     returns java.util.List<pe.gob.segdi.wsentidad.ws.EntidadBean>
     */
    @WebMethod(action = "getListaEntidad")
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "getListaEntidad", targetNamespace = "http://ws.wsentidad.segdi.gob.pe/", className = "pe.gob.segdi.wsentidad.ws.GetListaEntidad")
    @ResponseWrapper(localName = "getListaEntidadResponse", targetNamespace = "http://ws.wsentidad.segdi.gob.pe/", className = "pe.gob.segdi.wsentidad.ws.GetListaEntidadResponse")
    public List<EntidadBean> getListaEntidad(
            @WebParam(name = "sidcatent", targetNamespace = "")
            int sidcatent);
}
