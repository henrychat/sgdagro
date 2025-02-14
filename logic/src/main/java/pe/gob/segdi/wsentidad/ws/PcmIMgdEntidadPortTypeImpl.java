package pe.gob.segdi.wsentidad.ws;



import java.util.List;
import javax.jws.WebService;
import javax.xml.ws.Service;
import javax.xml.namespace.QName;
import java.net.URL;

@WebService
public class PcmIMgdEntidadPortTypeImpl implements pe.gob.segdi.wsentidad.ws.PcmIMgdEntidadPortType {

    private String serviceUrl;

    // Constructor sin argumentos requerido por JAX-WS
    public PcmIMgdEntidadPortTypeImpl() {
        // Constructor vacío
    }

    // Constructor para recibir la URL del servicio en tiempo de ejecución
    public PcmIMgdEntidadPortTypeImpl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    @Override
    public List<EntidadBean> getListaEntidad(int sidcatent) {
        try {
            // Si no se ha establecido la URL, se puede lanzar una excepción o usar un valor por defecto
            if (serviceUrl == null) {
                throw new RuntimeException("La URL del servicio no está definida");
            }

            // Se construye la URL del servicio
            URL url = new URL(serviceUrl);
            QName qname = new QName("http://ws.wsentidad.segdi.gob.pe/", "PcmIMgdEntidad");

            // Crear el servicio y obtener el puerto
            Service service = Service.create(url, qname);
            PcmIMgdEntidadPortType port = service.getPort(PcmIMgdEntidadPortType.class);

            // Invocar el método para obtener la lista de entidades
            return port.getListaEntidad(sidcatent);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al obtener la lista de entidades", e);
        }
    }
}
