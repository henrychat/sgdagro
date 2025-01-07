package pe.gob.onpe.tramitedoc.web.controller;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import pe.gob.onpe.autentica.model.Usuario;
import pe.gob.onpe.autentica.service.UsuarioService;
import pe.gob.onpe.libreria.util.Mensaje;
import pe.gob.onpe.libreria.util.ServletUtility;
import pe.gob.onpe.tramitedoc.bean.SiElementoBean;
import pe.gob.onpe.tramitedoc.bean.UsuarioConfigBean;
import pe.gob.onpe.tramitedoc.service.ReferencedData;
import pe.gob.onpe.tramitedoc.service.UsuarioConfigService;
import pe.gob.onpe.tramitedoc.util.Constantes;
import pe.gob.onpe.tramitedoc.web.util.ApplicationProperties;
import pe.gob.onpe.tramitedoc.web.validator.UsuarioValidator;

@Controller(value = "loginController")
@RequestMapping("/login.do")
@SessionAttributes(value = {"usuario", "usuarioConfig"})

public class LoginController extends GeneralController {

    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioConfigService usuarioConfigService;

    public LoginController() {
    }

    @Autowired
    private ReferencedData referencedData;

    @RequestMapping(value = "")
    //  @RequestMapping(method = RequestMethod.GET)
    public String doShowForm(HttpServletRequest request, Model model) {
        Usuario usuario = new Usuario();
        loadUsuarioRemoteAttribs(usuario, request);

        List<SiElementoBean> grpEnlacesRedInterna = referencedData.grpElementoList("SEG_RED_ENTIDAD");
        int iAccesoInterno = 0;
        String iPInterna = "";
        for (int i = 0; i < grpEnlacesRedInterna.size(); i++) {
            iPInterna = grpEnlacesRedInterna.get(i).getCeleDescor();
            if (usuario.getIpPC().contains(iPInterna)) {
                iAccesoInterno = 1;
            }

        }

        if (iAccesoInterno == 1) {
            usuario.setInAccesoLocal("1");
        } else {
            usuario.setInAccesoLocal("0");
        }

        model.addAttribute("usuario", usuario);
        model.addAttribute("intentoSinCaptcha", applicationProperties.getNroIntentoLoginMaximo());
        model.addAttribute("contIntentos", usuario.getContIntentos());
        model.addAttribute("urlManual", applicationProperties.getUrlManualAyuda());
        model.addAttribute("urlVideoTutorial", applicationProperties.getUrlVideoTutorial());

        return "login";
    }

    @RequestMapping(method = RequestMethod.POST)
    public String doProcessForm(@ModelAttribute("usuario") Usuario usuario, Model model, BindingResult result, HttpServletRequest request) {
        logger.info(usuario);
        UsuarioConfigBean usuarioConfig = new UsuarioConfigBean();
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpSession sesion = httpServletRequest.getSession(false);
        String captcha = (String) sesion.getAttribute("CAPTCHA");
        String destino = "login";
        boolean isAccessOk = true;
        Mensaje msg = new Mensaje();

        if (usuario == null) {
            destino = "login";
            isAccessOk = false;
        }

        usuario.setCaptcha((usuario.getCaptcha() + "".toUpperCase()));
        if ((usuario.getInAccesoLocal() == "0" || usuario.getContIntentos() > Integer.parseInt(applicationProperties.getNroIntentoLoginMaximo()))
                && (captcha == null || (captcha != null && !captcha.toUpperCase().equals(usuario.getCaptcha().toUpperCase())))) {
            usuario.setCaptcha("");
            msg.setCoRespuesta("999");
            msg.setDeRespuesta("El código de imagen no coinciden");
            isOkAndLoadAccess(msg, usuario, result, request);
            destino = "login";
            isAccessOk = false;
        }

        usuario.setTiIdentificacion(Constantes.TI_IDENTIFICACION);

        UsuarioValidator.getInstancia(usuarioService).validate(usuario, result);
        if (!result.hasErrors() && isAccessOk) {

            isAccessOk = false;
            loadUsuarioRemoteAttribs(usuario, request);
            usuarioService.autenticarUsuario(msg, usuario, applicationProperties.getCoAplicativo(), false, applicationProperties.getADSecureLDAP(), applicationProperties.getADDomain(), applicationProperties.getADHost(), applicationProperties.getADPort(), applicationProperties.getADAmbitoBusquedaUsuario(), applicationProperties.getADMinutosRestableceIntento());
            if (isOkAndLoadAccess(msg, usuario, result, request)) {
                if (usuario.getEsUsuario().equals("A")) {
                    usuarioConfig = usuarioConfigService.getConfig(msg, usuario.getCempCodemp(), usuario.getCoDep());
                    if (isOkAndLoadAccess(msg, result, request)) {
                        usuarioConfig.setCoUsuario(usuario.getCoUsuario());
                        String expiro = "";
                        if (usuario.getInAD().equals("1")) {
                            usuario.setDiasAntesExpiraClave(20);//NO EXPIRA LA CLAVE
                            expiro = "NE";
                        } else {
                            usuario.setDiasAntesExpiraClave(usuarioService.getNroDiasAntesExpiraClave(usuarioConfig.getDiasExpiracionClave(), usuario.getFeModClave(), usuario.getFeActual()));

                            expiro = usuarioService.verificarClaveExpiro(usuarioConfig.getDiasExpiracionClave(), usuario.getFeModClave(), usuario.getFeActual(), applicationProperties.getDiasAntesExpira());
                            if (!expiro.equals("EXP") && usuario.getInClave() != null && !usuario.getInClave().equals("EXP")) {
                                expiro = "NE";
                            }
                        }
                        usuario.setInClave(expiro);

                        if (expiro.equals("NE")) {
                            usuarioConfig.setInEsAdmin(usuario.getInAdmin());
                            usuario.setCoDep(usuarioConfig.getCoDep());
                            model.addAttribute("usuarioConfig", usuarioConfig);
                            isAccessOk = true;
                        }
                    }
                    /*
                    for(UsuarioAcceso usuarioAcceso: usuario.getUsuarioAccesos()){
                        if(StringUtils.hasLength(usuarioAcceso.getCoModulo()) && StringUtils.hasLength(usuarioAcceso.getCoOpcion())){
                            // tiene accesos asignados
                            isAccessOk = true;
                            break;
                        }
                    }
                     */
                } else {
                    usuario.setInClave("NF");
                }
            }

            int cont = usuario.getContIntentos() + 1;
            usuario.setContIntentos(cont);
            if (isAccessOk) {
                usuarioService.registraLogAcceso(usuario, "1");
                usuario.setSessionId(ServletUtility.getInstancia().loadSessionId(request));
                destino = "forward:/{version}/mainpanel.do";
                //destino = "forward:mainpanel";
            } else {
                if (usuario.getContIntentos() >= Integer.parseInt(applicationProperties.getNroIntentoLoginCaptchaMaximo())) {
                    destino = "forward:/{version}/LoginValidate.do";
                }
                usuarioService.registraLogAcceso(usuario, "0");
            }
        }
        model.addAttribute("contIntentos", usuario.getContIntentos());
        model.addAttribute("intentoSinCaptcha", applicationProperties.getNroIntentoLoginMaximo());
        return destino;
    }

    private boolean isOkAndLoadAccess(Mensaje msg, Usuario usuario, BindingResult result, HttpServletRequest request) {
        if (msg.getCoRespuesta().equals("00")) {
            // acceso valido a ok
            usuarioService.obtienePermisos(msg, usuario, applicationProperties.getCoAplicativo());
            return true;
        } else {
            // no accede y imprime lel mensaje
            printErrorMessaje(result, msg);
            return false;
        }
    }

    private boolean isOkAndLoadAccess(Mensaje msg, BindingResult result, HttpServletRequest request) {
        if (msg.getCoRespuesta().equals("00")) {
            // acceso valido a ok
            return true;
        } else {
            // no accede y imprime lel mensaje
            printErrorMessaje(result, msg);
            return false;
        }
    }

}
