package pe.gob.onpe.tramiteconv.dao.impl.sqlserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import pe.gob.onpe.tramitedoc.bean.*;
import pe.gob.onpe.tramitedoc.dao.CommonQueryDao;
import pe.gob.onpe.tramitedoc.dao.DatosPlantillaDao;
import pe.gob.onpe.tramitedoc.dao.SimpleJdbcDaoBase;
import pe.gob.onpe.tramitedoc.web.util.Utilidades;

@Repository("commonQueryDao")
public class CommonQueryDaoImp
        extends SimpleJdbcDaoBase
        implements CommonQueryDao {

    @Autowired
    private DatosPlantillaDao datosPlantillaDao;

    public DependenciaBean getDependenciaxCoDependencia(String coDepen) {
         StringBuilder sql = new StringBuilder();
        DependenciaBean dependenciaBean = null;
        sql.append("SELECT DE_DEPENDENCIA, CO_DEPENDENCIA FROM IDOSGD.RHTM_DEPENDENCIA WITH(NOLOCK) WHERE\nCO_DEPENDENCIA=? AND IN_BAJA='0'");

        try {
            dependenciaBean = (DependenciaBean) this.jdbcTemplate.queryForObject(sql.toString(), (RowMapper) BeanPropertyRowMapper.newInstance(DependenciaBean.class), new Object[]{coDepen});
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dependenciaBean;
    }

    public DependenciaBean getDependenciaBasico(String coDepen) {
         StringBuilder sql = new StringBuilder();
        DependenciaBean dependenciaBean = null;
        sql.append("SELECT \nX.DE_DEPENDENCIA, \nX.CO_DEPENDENCIA, \nX.de_sigla,\nX.in_mesa_partes,\n(SELECT DEP.CO_DEPENDENCIA FROM IDOSGD.RHTM_DEPENDENCIA DEP WITH(NOLOCK) WHERE DEP.IN_MESA_PARTES='1' AND DEP.IN_BAJA='0'\nAND 0 < (SELECT COUNT(1) FROM IDOSGD.TDTR_DEPENDENCIA_MP DMP WITH(NOLOCK) WHERE DMP.CO_DEP=X.CO_DEPENDENCIA AND DMP.ES_ELI='0' AND DMP.IN_MP='1')) CO_DEP_MP \nFROM IDOSGD.RHTM_DEPENDENCIA X \nWHERE X.CO_DEPENDENCIA=?");

        try {
            dependenciaBean = (DependenciaBean) this.jdbcTemplate.queryForObject(sql.toString(), (RowMapper) BeanPropertyRowMapper.newInstance(DependenciaBean.class), new Object[]{coDepen});
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dependenciaBean;
    }

    public List<RemitenteBean> getListRemitente(String coDepen) {
        List<RemitenteBean> list = new ArrayList<RemitenteBean>();
         StringBuilder sql = new StringBuilder();
        sql.append("select D.DE_DEPENDENCIA descrip, D.CO_DEPENDENCIA cod_dep,D.DE_CORTA_DEPEN de_corta_depen from IDOSGD.RHTM_DEPENDENCIA D WITH(NOLOCK) \nWHERE ? IN (CO_DEPENDENCIA,CO_DEPEN_PADRE) OR \nCO_DEPENDENCIA IN (SELECT co_dep_ref FROM IDOSGD.tdtx_referencia \nWHERE co_dep_emi = ?  AND ti_emi = 'D'   AND es_ref = '1') ORDER BY 1");

        try {
            list = this.jdbcTemplate.query(sql.toString(), (RowMapper) BeanPropertyRowMapper.newInstance(RemitenteBean.class), new Object[]{coDepen, coDepen});
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public String verificarExpedienteDuplicado(String pnuAnnExp, String pcoDepExp, String pcoGru, String pnuCorrExp) {
        String vReturn = "1";
        try {
            vReturn = (String) this.jdbcTemplate.queryForObject("select count(1) from IDOSGD.TDTC_EXPEDIENTE WITH(NOLOCK) \nWHERE NU_ANN_EXP=? and CO_DEP_EXP=? and CO_GRU=? and NU_CORR_EXP=?", String.class, new Object[]{pnuAnnExp, pcoDepExp, pcoGru, pnuCorrExp});

        } catch (Exception e) {
            e.printStackTrace();
        }
        return vReturn;
    }

    public String verficarNumeroDuplicadoEmiDocExtRecep(String pnuAnn, String pcoDepEmi, String pnuCorEmi) {
        String vReturn = "1";
        try {
            vReturn = (String) this.jdbcTemplate.queryForObject("SELECT count(1)\nFROM IDOSGD.tdtv_remitos WITH(NOLOCK) \nWHERE NU_ANN = ?\nAND CO_DEP_EMI = ?\nAND CO_GRU = '3' \nAND NU_COR_EMI = ?", String.class, new Object[]{pnuAnn, pcoDepEmi, pnuCorEmi});

        } catch (Exception e) {
            e.printStackTrace();
        }
        return vReturn;
    }

    ///HENRY
    public List<ProveedorBean> getLstProveedoresUrl(String prazonSocial) {

        String url = datosPlantillaDao.getParametros("URL_SUNAT_REST");

        RestTemplate restTemplate = new RestTemplate();

        String uri = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("nroRuc", prazonSocial).build()
                .toUriString();
        List<ProveedorBean> list = new ArrayList<ProveedorBean>();


        try {
            if (prazonSocial.matches("^[a-zA-Z].*")) {
                throw new EmptyResultDataAccessException("Input does not start with a letter", 1);
            }

            ResponseEntity<Map> response = restTemplate.getForEntity(uri, Map.class);


            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {// Extraer los datos del JSON
                Map<String, Object> body = response.getBody();
                Map<String, Object> data = (Map<String, Object>) body.get("Data");

                if (data != null) {
                    ProveedorBean proveedor = new ProveedorBean();
                    proveedor.setNuRuc((String) data.get("ddp_numruc"));
                    proveedor.setDescripcion((String) data.get("ddp_nombre"));
                    proveedor.setNoDep((String) data.get("desc_de"));
                    proveedor.setNoPrv((String) data.get("desc_prov"));
                    proveedor.setNoDis((String) data.get("desc_dist"));
                    proveedor.setCproDomicil(data.get("desc_tipvia") + " " + data.get("ddp_nomvia") + " " +
                            data.get("ddp_numer1") + " " + data.get("desc_tipzon") + " " + data.get("ddp_nomzon"));
                    proveedor.setDeDireccion(data.get("desc_tipvia") + " " + data.get("ddp_nomvia") + " " +
                            data.get("ddp_numer1") + " " + data.get("desc_tipzon") + " " + data.get("ddp_nomzon"));

                    UbigeoBean ubigeo = new UbigeoBean();
                    ubigeo.setNoDep(((String) data.get("desc_de")).trim());
                    ubigeo.setNoPrv(((String) data.get("desc_prov")).trim());
                    ubigeo.setNoDis(((String) data.get("desc_dist")).trim());


                    List<UbigeoBean> ubigeoList = getUbigeoBusqList(ubigeo);

                    if (ubigeoList != null && !ubigeoList.isEmpty()) {
                        System.out.println("inhreso a ubigeoList != null && !ubigeoList.isEmpty()");
                        UbigeoBean foundUbigeo = ubigeoList.get(0);
                        proveedor.setCubiCoddep(foundUbigeo.getUbDep());
                        proveedor.setCubiCodpro(foundUbigeo.getUbPrv());
                        proveedor.setCubiCoddis(foundUbigeo.getUbDis());
                        proveedor.setIdDepartamento(foundUbigeo.getUbDep());
                        proveedor.setIdProvincia(foundUbigeo.getUbPrv());
                        proveedor.setIdDistrito(foundUbigeo.getUbDis());

                    } else {

                        proveedor.setCubiCoddep("");
                        proveedor.setCubiCodpro("");
                        proveedor.setCubiCoddis("");
                        proveedor.setIdDepartamento("");
                        proveedor.setIdProvincia("");
                        proveedor.setIdDistrito("");
                    }

                    list.add(proveedor);
                    insProveedor(proveedor);
                    return list;
                }
            }

        } catch (EmptyResultDataAccessException e) {

            list = getLstProveedores(prazonSocial);
        } catch (Exception e) {
            list = null;
            e.printStackTrace();
        }
        return list;
    }

    public List<ProveedorBean> getLstProveedores(String prazonSocial) {

        StringBuilder sql = new StringBuilder();
        prazonSocial = Utilidades.fn_getCleanTextLenGreathree(Utilidades.fn_getCleanTextExpReg(prazonSocial));

        String sqlContains = "";
        Map<String, Object> objectParam = new HashMap<String, Object>();
        List<ProveedorBean> list = new ArrayList<ProveedorBean>();

        sql.append("SELECT A.* \nFROM (\nSELECT CPRO_RUC NU_RUC, CPRO_RAZSOC DESCRIPCION,CPRO_DOMICIL,CPRO_TELEFO,CPRO_EMAIL, \nLTRIM(NODEP) NODEP,LTRIM(NOPRV) NOPRV,LTRIM(NODIS) NODIS, LTRIM(CUBI_CODDEP) CUBI_CODDEP,LTRIM(CUBI_CODPRO) CUBI_CODPRO,LTRIM(CUBI_CODDIS) CUBI_CODDIS,  \nISNULL(CUBI_CODDEP,'') ID_DEPARTAMENTO, ISNULL(CUBI_CODPRO,'') ID_PROVINCIA, ISNULL(CUBI_CODDIS,'') ID_DISTRITO, ISNULL(CPRO_DOMICIL,'') DE_DIRECCION, ISNULL(CPRO_EMAIL,'') DE_CORREO,ISNULL(CPRO_TELEFO,'') TELEFONO \nFROM IDOSGD.LG_PRO_PROVEEDOR LEFT JOIN IDOSGD.IDTUBIAS ON UBDEP+UBPRV+UBDIS=CUBI_CODDEP+CUBI_CODPRO+CUBI_CODDIS\nwhere cpro_ruc='" + prazonSocial + "' or cpro_razsoc like '%" + prazonSocial + "%' \n  ) A order by 1 desc");

        try {
            if (prazonSocial != null && prazonSocial.trim().length() > 1) {
                list = this.namedParameterJdbcTemplate.query(sql.toString(), objectParam, (RowMapper) BeanPropertyRowMapper.newInstance(ProveedorBean.class));
                System.out.println(list.get(0).toString());
            }
        } catch (EmptyResultDataAccessException e) {
            list = null;
        } catch (Exception e) {
            list = null;
            e.printStackTrace();
        }
        return list;
    }


    public String insProveedor(ProveedorBean proveedor) {
        String vReturn = "NO_OK";
        StringBuilder sql = new StringBuilder();
        sql.append("insert into IDOSGD.LG_PRO_PROVEEDOR ");
        sql.append("(cpro_ruc, dpro_fecins ,cpro_razsoc, cpro_domicil, cubi_coddep, cubi_codpro, cubi_coddis , cpro_telefo,cpro_email,dni_repre,nom_repre) ");
        sql.append("values (?,CURRENT_TIMESTAMP,?,?,?,?,?,?,?,?,?) ");
        try {
            if (proveedor.getNuRuc().isEmpty()) {
                String seq = "SELECT RIGHT(REPLICATE('0', 11) + CAST((ISNULL(MAX(CAST(CPRO_RUC AS BIGINT)), 0) + 1) AS VARCHAR), 11) FROM IDOSGD.LG_PRO_PROVEEDOR";
                String keyCPRO_RUC = (String)this.jdbcTemplate.queryForObject(seq, String.class);
                proveedor.setNuRuc(keyCPRO_RUC);
            }

            this.jdbcTemplate.update(sql.toString(), new Object[] { proveedor
                    .getNuRuc(), proveedor.getDescripcion(), proveedor.getCproDomicil(), proveedor
                    .getCubiCoddep(), proveedor.getCubiCodpro(), proveedor.getCubiCoddis(), proveedor.getCproTelefo(), proveedor.getCproEmail(),
                    proveedor.getDniRepre(), proveedor.getNomRepre() });

            vReturn = "OK";
        } catch (DuplicateKeyException con) {
            vReturn = "Nde RUC duplicado.";
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return vReturn;
    }


    public List<DestinatarioOtroOrigenBean> getLstOtrosOrigenes(String pdescripcion) {
        Map<String, Object> objectParam = new HashMap<String, Object>();
        String sqlContains = "";
        StringBuilder sql = new StringBuilder();
        pdescripcion = Utilidades.fn_getCleanTextLenGreathree(Utilidades.fn_getCleanTextExpReg(pdescripcion));

        List<DestinatarioOtroOrigenBean> list = new ArrayList<DestinatarioOtroOrigenBean>();

        sql.append("SELECT ISNULL(DE_APE_PAT_OTR,'')+' '+ ISNULL(DE_APE_MAT_OTR,'')+', '+ ISNULL(DE_NOM_OTR,'') + ' - ' + DE_RAZ_SOC_OTR DESCRIPCION,\n (SELECT RETORNO FROM IDOSGD.PK_SGD_DESCRIPCION_DE_DOMINIOS('TIP_DOC_IDENT', CO_TIP_OTR_ORI )) TIPO_DOC_IDENTIDAD,\n NU_DOC_OTR_ORI NRO_DOC_IDENTIDAD, CO_OTR_ORI CO_OTRO_ORIGEN,\nISNULL(UB_DEP,'') ID_DEPARTAMENTO, ISNULL(UB_PRO,'') ID_PROVINCIA, ISNULL(UB_DIS,'') ID_DISTRITO, ISNULL(DE_DIR_OTRO_ORI,'') DE_DIRECCION, ISNULL(DE_EMAIL,'') DE_CORREO,ISNULL(DE_TELEFO,'') TELEFONO \n  ,UB_DEP,UB_PRO,UB_DIS\n  ,ltrim(UB.NODEP)+' '+ltrim(UB.NOPRV)+' '+ltrim(UB.NODIS) ubigeo,DE_EMAIL,a.DE_TELEFO,a.DE_DIR_OTRO_ORI\n ,ltrim(UB.NODEP) NODEP, ltrim(UB.NOPRV) NOPRV, ltrim(UB.NODIS) NODIS  \n  FROM IDOSGD.TDTR_OTRO_ORIGEN a\n LEFT JOIN IDOSGD.IDTUBIAS UB ON UB.UBDEP=a.UB_DEP AND UB.UBPRV=a.UB_PRO AND UB.UBDIS=a.UB_DIS\nwhere (DE_RAZ_SOC_OTR like '%" + pdescripcion + "%' \nOR (DE_APE_PAT_OTR+' '+ DE_APE_MAT_OTR+' '+DE_NOM_OTR) like '%" + pdescripcion + "%' \nOR (DE_NOM_OTR +' '+DE_APE_PAT_OTR+' '+ DE_APE_MAT_OTR) like '%" + pdescripcion + "%' \nOR NU_DOC_OTR_ORI like REPLACE('%" + pdescripcion + "%',' ','%') \n) and ISNULL(CO_TIP_OTR_ORI,'--') not in('09')\n order by 1 desc");

        try {
            if (pdescripcion != null && pdescripcion.trim().length() > 1) {
                list = this.namedParameterJdbcTemplate.query(sql.toString(), objectParam, (RowMapper) BeanPropertyRowMapper.newInstance(DestinatarioOtroOrigenBean.class));
            }
        } catch (EmptyResultDataAccessException e) {
            list = null;
        } catch (Exception e) {
            list = null;
            e.printStackTrace();
        }
        return list;
    }


    /////HENRY
    public CiudadanoBean getCiudadano(String pnuDoc) {

        CiudadanoBean ciudadanoExistente = getCiudadanoSql(pnuDoc);
        if (ciudadanoExistente != null) {
            return ciudadanoExistente;
        }

        String url = datosPlantillaDao.getParametros("URL_RENIEC_REST");
        RestTemplate restTemplate = new RestTemplate();

        // Construir la URL con el parámetro
        String uri = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("documento", pnuDoc).build()
                .toUriString();

        try {
            // Realizar la solicitud al servicio
            ResponseEntity<Map> response = restTemplate.getForEntity(uri, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                // Extraer los datos del JSON
                Map<String, Object> body = response.getBody();
                Map<String, Object> data = (Map<String, Object>) body.get("Data");

                if (data != null) {

                    CiudadanoBean ciudadanoBean = new CiudadanoBean();
                    ciudadanoBean.setNuDocumento(pnuDoc);
                    ciudadanoBean.setNombre(data.get("ApellidoPaterno") + " " + data.get("ApellidoMaterno") + " " + data.get("Nombres"));
                    ciudadanoBean.setDeDireccion((String) data.get("Direccion"));
                    ciudadanoBean.setUbigeo((String) data.get("Ubigeo"));

                    // Procesar el Ubigeo
                    String[] ubigeoParts = ((String) data.get("Ubigeo")).split("/");
                    if (ubigeoParts.length == 3) {
                        String departamento = ubigeoParts[0];
                        String provincia = ubigeoParts[1];
                        String distrito = ubigeoParts[2];

                        // Crear el objeto UbigeoBean para la búsqueda
                        UbigeoBean ubigeo = new UbigeoBean();
                        ubigeo.setNoDep(departamento);
                        ubigeo.setNoPrv(provincia);
                        ubigeo.setNoDis(distrito);

                        // Llamar al método getUbigeoBusqList
                        List<UbigeoBean> ubigeoList = getUbigeoBusqList(ubigeo);
                        System.out.println("ubigeoList ubigeoList ubigeoList");
                        System.out.println(ubigeoList);
                        if (ubigeoList != null && !ubigeoList.isEmpty()) {
                            System.out.println("ingresa condicion");
                            UbigeoBean foundUbigeo = ubigeoList.get(0);
                            ciudadanoBean.setIdDepartamento(foundUbigeo.getUbDep());
                            ciudadanoBean.setIdProvincia(foundUbigeo.getUbPrv());
                            ciudadanoBean.setIdDistrito(foundUbigeo.getUbDis());
                        } else {
                            System.out.println(" no ingresa condicion");
                            ciudadanoBean.setIdDepartamento("");
                            ciudadanoBean.setIdProvincia("");
                            ciudadanoBean.setIdDistrito("");
                        }
                    }

                    ciudadanoBean.setDeCorreo("");
                    ciudadanoBean.setTelefono("");


                    CitizenBean citizenBean = new CitizenBean();
                    citizenBean.setNuLem(pnuDoc);
                    citizenBean.setUbDep(ciudadanoBean.getIdDepartamento());
                    citizenBean.setUbPrv(ciudadanoBean.getIdProvincia());
                    citizenBean.setUbDis(ciudadanoBean.getIdDistrito());
                    citizenBean.setDeApp((String) data.get("ApellidoPaterno"));
                    citizenBean.setDeApm((String) data.get("ApellidoMaterno"));
                    citizenBean.setDeNom((String) data.get("Nombres"));
                    citizenBean.setDeDomicil((String) data.get("Direccion"));


                    insCiudadano(citizenBean);



                    return ciudadanoBean;
                }
            }
        } catch (Exception e) {
            System.err.println("Error al obtener datos desde el servicio: " + e.getMessage());
            e.printStackTrace();

        }

        return null;
    }

    public String insCiudadano(CitizenBean ciudadano) {
        String vReturn = "NO_OK";
        StringBuilder sql = new StringBuilder();
        String genCoProceso = "";

        sql.append("insert into IDOSGD.TDTX_ANI_SIMIL ");
        sql.append("(NULEM, UBDEP, UBPRV, UBDIS, DEAPP, DEAPM, DENOM, DEDOMICIL ) ");
        sql.append("values (?,?,?,?,?, ?,? ,?) ");


        try {
            this.jdbcTemplate.update(sql.toString(), new Object[] { ciudadano
                    .getNuLem(), ciudadano.getUbDep(), ciudadano.getUbPrv(), ciudadano.getUbDis(), ciudadano
                    .getDeApp(), ciudadano.getDeApm(), ciudadano.getDeNom(), ciudadano.getDeDomicil() });


            vReturn = "OK";
        }
        catch (DuplicateKeyException con) {
            vReturn = "Nde documento duplicado.";
            con.printStackTrace();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return vReturn;
    }

    public List<UbigeoBean> getUbigeoBusqList(UbigeoBean ubigeo) {
        System.out.println("ubigeo.getNoDep");
        System.out.println(ubigeo.getNoDep());
        System.out.println(ubigeo.getNoPrv());
        System.out.println(ubigeo.getNoDis());
        StringBuilder sql = new StringBuilder();
        sql.append("select  ubdep, ");
        sql.append("        ubprv, ");
        sql.append("        ubdis, ");
        sql.append("        ubloc, ");
        sql.append("        coreg, ");
        sql.append("        LTRIM(RTRIM(nodep)) nodep, ");
        sql.append("        LTRIM(RTRIM(noprv)) noprv,  ");
        sql.append("        LTRIM(RTRIM(nodis)) nodis, ");
        sql.append("        cpdis, ");
        sql.append("        stubi, ");
        sql.append("        stsob, ");
        sql.append("        feres, ");
        sql.append("        inubi, ");
        sql.append("        ub_inei ");
        sql.append("from IDOSGD.idtubias ");
        sql.append("where ubdep like '%' + ? + '%' and ubprv like '%' + ? + '%' and ");
        sql.append("ubdis like '%' + ? + '%' and nodep like '%' + ? + '%' and ");
        sql.append("noprv like '%' + ? + '%' and nodis like '%' + ? + '%' and ");
        sql.append("UBLOC = '00' and ");
        sql.append("UBPRV <> '00' and ");
        sql.append("UBDIS <> '00' and ");
        sql.append("UBDEP <> ' ' ");

        List<UbigeoBean> list = new ArrayList<UbigeoBean>();

        try {
            list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(UbigeoBean.class), new Object[] {
                    ubigeo.getUbDep() != null ? ubigeo.getUbDep() : "",
                    ubigeo.getUbPrv() != null ? ubigeo.getUbPrv() : "",
                    ubigeo.getUbDis() != null ? ubigeo.getUbDis() : "",
                    ubigeo.getNoDep() != null ? ubigeo.getNoDep() : "",
                    ubigeo.getNoPrv() != null ? ubigeo.getNoPrv() : "",
                    ubigeo.getNoDis() != null ? ubigeo.getNoDis() : ""
            });
            System.out.println("lista");
            System.out.println(list);
            System.out.println(list.get(0));
            System.out.println("lista final");
        }
        catch (EmptyResultDataAccessException e) {
            list = null;
        } catch (Exception e) {
            list = null;
            e.printStackTrace();
        }
        return list;
    }


    public CiudadanoBean getCiudadanoSql(String pnuDoc) {
         StringBuilder sql = new StringBuilder();
        sql.append("select I.NULEM NU_DOCUMENTO,substring(RTRIM(I.DEAPP)+' '+RTRIM(I.DEAPM)+' '+RTRIM(I.DENOM),1,200) NOMBRE, \nISNULL(I.UBDEP,'') ID_DEPARTAMENTO, ISNULL(I.UBPRV,'') ID_PROVINCIA, ISNULL(I.UBDIS,'') ID_DISTRITO, ISNULL(I.DEDOMICIL,'') DE_DIRECCION, ISNULL(I.DEEMAIL,'') DE_CORREO,ISNULL(I.DETELEFO,'') TELEFONO \n,I.UBDEP, I.UBPRV,I.UBDIS,I.DEDOMICIL,I.DEEMAIL, I.DETELEFO ,  LTRIM(RTRIM(UB.NODEP)) + '/' + LTRIM(RTRIM(UB.NOPRV)) + '/' + LTRIM(RTRIM(UB.NODIS)) ubigeo \n from IDOSGD.TDTX_ANI_SIMIL I LEFT JOIN IDOSGD.IDTUBIAS UB ON UB.UBDEP=I.UBDEP AND UB.UBPRV=I.UBPRV AND UB.UBDIS=I.UBDIS  \nwhere\nNULEM like ?+'%'\norder by 2");

        CiudadanoBean ciudadanoBean = new CiudadanoBean();
        try {
            ciudadanoBean = (CiudadanoBean) this.jdbcTemplate.queryForObject(sql.toString(), (RowMapper) BeanPropertyRowMapper.newInstance(CiudadanoBean.class), new Object[]{pnuDoc});
        } catch (EmptyResultDataAccessException e) {
            ciudadanoBean = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ciudadanoBean;
    }

    public ProveedorBean getProveedorSql(String pnuRuc){
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT CPRO_RUC NU_RUC, CPRO_RAZSOC DESCRIPCION,CPRO_DOMICIL, CUBI_CODDEP, CUBI_CODPRO, CUBI_CODDIS, CUBI_CODDEP ID_DEPARTAMENTO,  CUBI_CODPRO ID_PROVINCIA, CUBI_CODDIS ID_DISTRITO, CPRO_DOMICIL DE_DIRECCION FROM IDOSGD.LG_PRO_PROVEEDOR WITH(NOLOCK)\nwhere CPRO_RUC=?");
        ProveedorBean proveedorBean = new ProveedorBean();
        try {
            proveedorBean = (ProveedorBean) this.jdbcTemplate.queryForObject(sql.toString(), (RowMapper) BeanPropertyRowMapper.newInstance(ProveedorBean.class), new Object[]{pnuRuc});

        } catch (EmptyResultDataAccessException e) {
            proveedorBean = null;
        }catch (Exception e) {
            e.printStackTrace();
        }
        return proveedorBean;
    }

    public ProveedorBean getProveedor(String pnuRuc) {

        ProveedorBean proveedorSql = getProveedorSql(pnuRuc);
        if (proveedorSql != null) {
            return proveedorSql;
        }


        ProveedorBean proveedorBean = null;
        try {

                String url = datosPlantillaDao.getParametros("URL_SUNAT_REST");
                RestTemplate restTemplate = new RestTemplate();
                String uri = UriComponentsBuilder.fromHttpUrl(url)
                        .queryParam("nroRuc", pnuRuc).build()
                        .toUriString();

                ResponseEntity<Map> response = restTemplate.getForEntity(uri, Map.class);


                if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                    Map<String, Object> body = response.getBody();
                    Map<String, Object> data = (Map<String, Object>) body.get("Data");

                    if (data != null) {
                        ProveedorBean proveedor = new ProveedorBean();
                        proveedor.setNuRuc((String) data.get("ddp_numruc"));
                        proveedor.setDescripcion((String) data.get("ddp_nombre"));
                        proveedor.setNoDep((String) data.get("desc_de"));
                        proveedor.setNoPrv((String) data.get("desc_prov"));
                        proveedor.setNoDis((String) data.get("desc_dist"));
                        proveedor.setCproDomicil(data.get("desc_tipvia") + " " + data.get("ddp_nomvia") + " " +
                                data.get("ddp_numer1") + " " + data.get("desc_tipzon") + " " + data.get("ddp_nomzon"));
                        proveedor.setDeDireccion(data.get("desc_tipvia") + " " + data.get("ddp_nomvia") + " " +
                                data.get("ddp_numer1") + " " + data.get("desc_tipzon") + " " + data.get("ddp_nomzon"));

                        UbigeoBean ubigeo = new UbigeoBean();
                        ubigeo.setNoDep(((String) data.get("desc_de")).trim());
                        ubigeo.setNoPrv(((String) data.get("desc_prov")).trim());
                        ubigeo.setNoDis(((String) data.get("desc_dist")).trim());


                        List<UbigeoBean> ubigeoList = getUbigeoBusqList(ubigeo);

                        if (ubigeoList != null && !ubigeoList.isEmpty()) {
                            System.out.println("inhreso a ubigeoList != null && !ubigeoList.isEmpty()");
                            UbigeoBean foundUbigeo = ubigeoList.get(0);
                            proveedor.setCubiCoddep(foundUbigeo.getUbDep());
                            proveedor.setCubiCodpro(foundUbigeo.getUbPrv());
                            proveedor.setCubiCoddis(foundUbigeo.getUbDis());
                            proveedor.setIdDepartamento(foundUbigeo.getUbDep());
                            proveedor.setIdProvincia(foundUbigeo.getUbPrv());
                            proveedor.setIdDistrito(foundUbigeo.getUbDis());

                        } else {

                            proveedor.setCubiCoddep("");
                            proveedor.setCubiCodpro("");
                            proveedor.setCubiCoddis("");
                            proveedor.setIdDepartamento("");
                            proveedor.setIdProvincia("");
                            proveedor.setIdDistrito("");
                        }

                        String urlRepre = datosPlantillaDao.getParametros("URL_SUNAT_REST_REPRESE");
                        String uriRepre = UriComponentsBuilder.fromHttpUrl(urlRepre)
                                .queryParam("nroRuc", pnuRuc).build()
                                .toUriString();
                        RestTemplate restTemplateRepre = new RestTemplate();
                        ResponseEntity<Map> responseRepre = restTemplateRepre.getForEntity(uriRepre, Map.class);
                        if(responseRepre.getStatusCode() == HttpStatus.OK && responseRepre.getBody() != null){
                            Map<String, Object> bodyRepre = responseRepre.getBody();
                            Map<String, Object> dataRepre = (Map<String, Object>) bodyRepre.get("Data");
                            String rsoNroDoc = (String) dataRepre.get("rso_nrodoc");
                            if (rsoNroDoc != null) {
                                proveedor.setDniRepre(rsoNroDoc);
                                proveedor.setNomRepre((String) dataRepre.get("rso_nombre"));
                            }
                        }

                        insProveedor(proveedor);
                        proveedorBean = proveedor;
                        return proveedorBean;
                    }
                }



       /* } catch (EmptyResultDataAccessException e) {
            proveedorBean = null;
      */} catch (Exception e) {
            e.printStackTrace();
        }
        return proveedorBean;
    }

    public List<EmpleadoBean> getLsEmpleado(String coDep) {
        List<EmpleadoBean> list;
         StringBuilder sql = new StringBuilder();

        sql.append("SELECT e.cemp_apepat+' '+e.cemp_apemat+' '+e.cemp_denom nombre, e.CEMP_CODEMP \nFROM IDOSGD.RHTM_PER_EMPLEADOS e WITH(NOLOCK)\nwhere e.CEMP_EST_EMP = '1'\nORDER BY 1");

        try {
            list = this.jdbcTemplate.query(sql.toString(), (RowMapper) BeanPropertyRowMapper.newInstance(EmpleadoBean.class), new Object[0]);
        } catch (EmptyResultDataAccessException e) {
            list = null;
        } catch (Exception e) {
            list = null;
            e.printStackTrace();
        }
        return list;
    }

    public List<CargoFuncionBean> getLsEmpleo() {
        List<CargoFuncionBean> list;
         StringBuilder sql = new StringBuilder();

        sql.append("SELECT CCAR_DESCAR DESCRIPCION, CCAR_CO_CARGO CODIGO\nFROM IDOSGD.RHTM_CARGOS WITH(NOLOCK)\nWHERE CCAR_CO_CARGO <> '000'\nORDER BY 1");

        try {
            list = this.jdbcTemplate.query(sql.toString(), (RowMapper) BeanPropertyRowMapper.newInstance(CargoFuncionBean.class), new Object[0]);
        } catch (EmptyResultDataAccessException e) {
            list = null;
        } catch (Exception e) {
            list = null;
            e.printStackTrace();
        }
        return list;
    }

    public List<DependenciaBean> getLsDepencia() {
        List<DependenciaBean> list;
         StringBuilder sql = new StringBuilder();

        sql.append("SELECT DE_DEPENDENCIA,\n       CO_DEPENDENCIA,\n       DE_CORTA_DEPEN\n  FROM IDOSGD.RHTM_DEPENDENCIA WITH(NOLOCK)\n WHERE co_nivel <> '6'\n   AND IN_BAJA = '0'\n   order by 1");

        try {
            list = this.jdbcTemplate.query(sql.toString(), (RowMapper) BeanPropertyRowMapper.newInstance(DependenciaBean.class), new Object[0]);
        } catch (EmptyResultDataAccessException e) {
            list = null;
        } catch (Exception e) {
            list = null;
            e.printStackTrace();
        }
        return list;
    }

    public String[] getNroCorrLocalDependencia() {
        String vReturn = "ERROR|";
         StringBuilder sql = new StringBuilder();
        sql.append("SELECT ISNULL( MAX( a.NU_CORRELATIVO) , 0)+1\nFROM IDOSGD.SITM_LOCAL_DEPENDENCIA a WITH(NOLOCK)");

        try {
            String nroCorr = (String) this.jdbcTemplate.queryForObject(sql.toString(), String.class);
            vReturn = "OK|" + nroCorr;
        } catch (EmptyResultDataAccessException e) {
            vReturn = "OK|1";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return vReturn.split("\\|");
    }

    public List<EmpleadoBean> getLsEmpleadoIntitu(String deEmp) {
        List<EmpleadoBean> list;
         StringBuilder sql = new StringBuilder();

        sql.append("SELECT e.cemp_apepat+' '+e.cemp_apemat+' '+e.cemp_denom nombre, e.CEMP_CODEMP \nFROM IDOSGD.RHTM_PER_EMPLEADOS e WITH(NOLOCK)\nwhere e.CEMP_EST_EMP = '1' \nand  e.cemp_apepat+' '+e.cemp_apemat+' '+e.cemp_denom LIKE '%'+?+'%'\nORDER BY 1");

        try {
            list = this.jdbcTemplate.query(sql.toString(), (RowMapper) BeanPropertyRowMapper.newInstance(EmpleadoBean.class), new Object[]{deEmp});
        } catch (EmptyResultDataAccessException e) {
            list = null;
        } catch (Exception e) {
            list = null;
            e.printStackTrace();
        }
        return list;
    }

    public String[] getCodigoMotivoDocRec(String nuAnn, String nuEmi, String nuDes) {
        String vReturn = "ERROR|";
         StringBuilder sql = new StringBuilder();
        sql.append("SELECT CO_MOT FROM IDOSGD.TDTV_DESTINOS WITH(NOLOCK)\nWHERE NU_ANN=?\nAND NU_EMI=?\nAND NU_DES=?\nAND ES_ELI='0'");

        try {
            String coMot = (String) this.jdbcTemplate.queryForObject(sql.toString(), String.class, new Object[]{nuAnn, nuEmi, nuDes});
            vReturn = "OK|" + coMot;
        } catch (EmptyResultDataAccessException e) {
            vReturn = "ERROR|";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return vReturn.split("\\|");
    }

    public EmpleadoBean getEmpJefeDep(String coDep) {
         StringBuilder sql = new StringBuilder();
        sql.append("select CO_EMPLEADO CEMP_CODEMP,IDOSGD.PK_SGD_DESCRIPCION_DE_NOM_EMP(CO_EMPLEADO) NOMBRE \nfrom IDOSGD.rhtm_dependencia WITH(NOLOCK)\nwhere CO_DEPENDENCIA = ?\nAND IN_BAJA='0'");

        EmpleadoBean empleadoBean = new EmpleadoBean();
        try {
            empleadoBean = (EmpleadoBean) this.jdbcTemplate.queryForObject(sql.toString(), (RowMapper) BeanPropertyRowMapper.newInstance(EmpleadoBean.class), new Object[]{coDep});
        } catch (EmptyResultDataAccessException e) {
            empleadoBean = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return empleadoBean;
    }

    public List<EmpleadoBean> getLsEmpDepDesEmp(String deEmp, String coDep) {
        List<EmpleadoBean> list;
         StringBuilder sql = new StringBuilder();

        sql.append("SELECT e.cemp_apepat+' '+e.cemp_apemat+' '+e.cemp_denom nombre, e.CEMP_CODEMP \nFROM IDOSGD.RHTM_PER_EMPLEADOS e WITH(NOLOCK), \n( \nSELECT CEMP_CODEMP \nFROM IDOSGD.RHTM_PER_EMPLEADOS\nwhere CEMP_EST_EMP = '1' \n  and (CO_DEPENDENCIA=? or CEMP_CO_DEPEND=?) \nunion \nselect co_emp from IDOSGD.tdtx_dependencia_empleado  where co_dep=? and es_emp='0' \nunion \nselect co_empleado from IDOSGD.rhtm_dependencia where co_dependencia=? \n) a \nwhere e.cemp_codemp = a.cemp_codemp\nand  e.cemp_apepat+' '+e.cemp_apemat+' '+e.cemp_denom LIKE '%'+?+'%'\norder by 1");

        try {
            list = this.jdbcTemplate.query(sql.toString(), (RowMapper) BeanPropertyRowMapper.newInstance(EmpleadoBean.class), new Object[]{coDep, coDep, coDep, coDep, deEmp});
        } catch (EmptyResultDataAccessException e) {
            list = null;
        } catch (Exception e) {
            list = null;
            e.printStackTrace();
        }
        return list;
    }

    public List<EmpleadoVoBoBean> getLsPersonalVoBo(String nuAnn, String nuEmi) {
        List<EmpleadoVoBoBean> list;
         StringBuilder sql = new StringBuilder();

        sql.append("SELECT VB.CO_EMP_VB CO_EMPLEADO,(SELECT CEMP_APEPAT + ' ' + CEMP_APEMAT + ' ' + CEMP_DENOM FROM IDOSGD.RHTM_PER_EMPLEADOS WITH(NOLOCK) WHERE CEMP_CODEMP = VB.CO_EMP_VB) NOMBRE,\nVB.CO_DEP CO_DEPENDENCIA, \n(SELECT RETORNO FROM IDOSGD.PK_SGD_DESCRIPCION_DE_SIGLA_CORTA(VB.CO_DEP)) DE_DEPENDENCIA,\n(CASE \nWHEN VB.IN_VB ='B' THEN '0' \nELSE VB.IN_VB \nEND \n) IN_VOBO \nFROM IDOSGD.TDTV_PERSONAL_VB VB \nWHERE VB.NU_ANN=?\nAND VB.NU_EMI=?\nORDER BY NOMBRE");

        try {
            list = this.jdbcTemplate.query(sql.toString(), (RowMapper) BeanPropertyRowMapper.newInstance(EmpleadoVoBoBean.class), new Object[]{nuAnn, nuEmi});
        } catch (EmptyResultDataAccessException e) {
            list = null;
        } catch (Exception e) {
            list = null;
            e.printStackTrace();
        }
        return list;
    }

    public String getInFirmaDoc(String pcoDep, String pcoTipoDoc) {
         StringBuilder sql = new StringBuilder();
        String result = null;
        sql.append(" SELECT \n\t(\n\t\tCASE WHEN es_obl_firma='0' THEN 'N'\n\t\tELSE 'F'\n\t\tEND\n\t)\nFROM IDOSGD.sitm_doc_dependencia WITH(NOLOCK)\nWHERE co_dep     = ?\nAND co_tip_doc = ?");

        try {
            result = (String) this.jdbcTemplate.queryForObject(sql.toString(), String.class, new Object[]{pcoDep, pcoTipoDoc});
        } catch (EmptyResultDataAccessException e) {
            result = "F";
        } catch (Exception e) {
            result = "F";
            e.printStackTrace();
        }

        return result;
    }

    public List<CiudadanoBean> getLstCiudadanos(String sDescCiudadano) {
        List<CiudadanoBean> list = new ArrayList<CiudadanoBean>();
         StringBuilder sql = new StringBuilder();
//    sql.append(" select I.NULEM NU_DOCUMENTO,substr(RTRIM(I.DEAPP)||' '||RTRIM(I.DEAPM)||' '||RTRIM(I.DENOM),1,200) NOMBRE,\nNVL(UBDEP,'') ID_DEPARTAMENTO, NVL(UBPRV,'') ID_PROVINCIA, NVL(UBDIS,'') ID_DISTRITO, NVL(DEDOMICIL,'') DE_DIRECCION, NVL(DEEMAIL,'') DE_CORREO,NVL(DETELEFO,'') TELEFONO \nfrom IDTANIRS I \nwhere\n(substr(RTRIM(I.DEAPP)||' '||RTRIM(I.DEAPM)||' '||RTRIM(I.DENOM),1,200)) like '%'||?||'%'\norder by 2");

        sql.append(" select I.NULEM NU_DOCUMENTO,SUBSTRING(RTRIM(I.DEAPP)+' '+RTRIM(I.DEAPM)+' '+RTRIM(I.DENOM),1,200)    NOMBRE, ");
        sql.append(" ISNULL(UBDEP,'') ID_DEPARTAMENTO, ISNULL(UBPRV,'') ID_PROVINCIA, ISNULL(UBDIS,'') ");
        sql.append(" ID_DISTRITO, ISNULL(DEDOMICIL,'') DE_DIRECCION, ISNULL(DEEMAIL,'') DE_CORREO, ");
        sql.append(" ISNULL(DETELEFO,'') TELEFONO  ");
        sql.append(" from IDOSGD.IDTANIRS I WITH(NOLOCK)    where ");
        sql.append(" (SUBSTRING(RTRIM(I.DEAPP)+' '+RTRIM(I.DEAPM)+' '+RTRIM(I.DENOM),1,200)) like '%'+?+'%' ");
        sql.append(" order by 2 ");

        CiudadanoBean ciudadanoBean = new CiudadanoBean();
        try {
            list = this.jdbcTemplate.query(sql.toString(), (RowMapper) BeanPropertyRowMapper.newInstance(CiudadanoBean.class), new Object[]{sDescCiudadano
                .toUpperCase()});
        } catch (EmptyResultDataAccessException e) {
            ciudadanoBean = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<SiElementoBean> getLsDepenciaMensjeria() {
        List<SiElementoBean> list;
         StringBuilder sql = new StringBuilder();

        sql.append(" SELECT CELE_DESELE,\n CELE_DESCOR \n FROM IDOSGD.SI_ELEMENTO WITH(NOLOCK)\n WHERE SI_ELEMENTO.CTAB_CODTAB='MSJ_DEPENDENCIA'\n order by SI_ELEMENTO.NELE_NUMSEC");

        try {
            list = this.jdbcTemplate.query(sql.toString(), (RowMapper) BeanPropertyRowMapper.newInstance(SiElementoBean.class));
        } catch (EmptyResultDataAccessException e) {
            list = null;
        } catch (Exception e) {
            list = null;
            e.printStackTrace();
        }
        return list;
    }

    public String obtenerValorParametro(String nombreParametro) {
         StringBuilder sql = new StringBuilder();
        String result = null;
        sql.append("SELECT UPPER(DE_PAR) DE_PAR FROM IDOSGD.TDTR_PARAMETROS WITH(NOLOCK) WHERE CO_PAR=?");
        try {
            result = (String) this.jdbcTemplate.queryForObject(sql.toString(), String.class, new Object[]{nombreParametro});
        } catch (EmptyResultDataAccessException e) {
            result = "";
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public String getCargoEmpleado(String coDep, String coEmp) {
        StringBuilder sql = new StringBuilder();
        String result = null;
        sql.append("SELECT RETORNO  FROM  IDOSGD.PK_SGD_DESCRIPCION_DE_CARGO_DOCUMENTO(?, ?)  ");
        try {
            result = (String) this.jdbcTemplate.queryForObject(sql.toString(), String.class, new Object[]{coDep, coEmp});
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public DatosInterBean DatosInter(String nuAnn, String nuEmi) {
        DatosInterBean datosInter = new DatosInterBean();

         StringBuilder sql = new StringBuilder();

        sql.append("SELECT (CASE WHEN LEN (A.NU_RUC_DES)>0 THEN A.NU_RUC_DES ELSE '0' END) AS NU_RUC_DES,(CASE WHEN LEN(A.DE_NOM_DES)>0 THEN a.DE_NOM_DES ELSE \n CASE WHEN LEN(A.REMI_NU_DNI_EMI)>0 THEN IDOSGD.PK_SGD_DESCRIPCION_ANI_SIMIL(a.REMI_NU_DNI_EMI) ELSE \n CASE WHEN LEN(A.REMI_CO_OTR_ORI_EMI)>0 THEN IDOSGD.PK_SGD_DESCRIPCION_OTRO_ORIGEN(a.remi_co_otr_ori_emi) ELSE '' END END END) DE_NOM_DES,\n(CASE WHEN LEN(A.DE_DEP_DES)>0 THEN DE_DEP_DES ELSE '' END) DE_DEP_DES,\n(CASE WHEN LEN(A.DE_CAR_DES)>0 THEN DE_CAR_DES ELSE A.DE_CARGO END) DE_CAR_DES FROM IDOSGD.TDTV_DESTINOS A INNER JOIN IDOSGD.TDTV_REMITOS B ON A.NU_ANN=B.NU_ANN AND A.NU_EMI=B.NU_EMI AND B.CO_TIP_DOC_ADM IN (SELECT CDOC_TIPDOC FROM IDOSGD.IOTDTX_TIPO_DOCUMENTO) \nWHERE A.NU_ANN=?\nAND A.NU_EMI=?\nAND A.NU_DES=1");

        try {
            datosInter = (DatosInterBean) this.jdbcTemplate.queryForObject(sql.toString(), (RowMapper) BeanPropertyRowMapper.newInstance(DatosInterBean.class), new Object[]{nuAnn, nuEmi});
        } catch (EmptyResultDataAccessException e) {

            datosInter = null;
        } catch (Exception e) {
            e.printStackTrace();
            datosInter = null;
        }

        return datosInter;
    }

    @Override
    public List<TablaMaestraBean> getLsContenidoTabla(String vTabla) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<CargoFuncionBean> getCargoBusqList(CargoFuncionBean cargo) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String insCargo(CargoFuncionBean cargo, String codusuario) {
        // TODO Auto-generated method stub
        return null;
    }
}
