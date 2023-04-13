package pe.gob.onpe.tramiteconv.dao.impl.sqlserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Repository;
import pe.gob.onpe.tramitedoc.bean.AvisoBandejaEntradaBean;
import pe.gob.onpe.tramitedoc.bean.EtiquetaBandejaEnBean;
import pe.gob.onpe.tramitedoc.dao.AvisoBandejaEntradaDao;
import pe.gob.onpe.tramitedoc.dao.SimpleJdbcDaoBase;

@Repository("avisoBandejaEntradaDao")
public class AvisoBandejaEntradaDaoImp extends SimpleJdbcDaoBase
        implements AvisoBandejaEntradaDao {

    private final Logger logger;

    public AvisoBandejaEntradaDaoImp() {
        this.logger = LoggerFactory.getLogger(getClass());
    }

    public List<AvisoBandejaEntradaBean> getBandejaEntradaAccesoTotal(String coDependencia, String coEmpleado) {
         StringBuilder sql = new StringBuilder();
        sql.append(" SELECT 1 ORDEN, '06' TI_PEN, 'PARA DESPACHO' DE_PEN,")
                .append(" 'Documentos Administrativos listos para la Firma' DE_RESUMEN, ")
                .append(" SUM(nu_can)NU_CAN, :pCoDep CO_DEP, '' CO_BANDEJA ")
                .append(" FROM (SELECT count(1) Nu_Can ")
                .append("         FROM IDOSGD.Tdtv_Remitos r WITH(NOLOCK) ")
                .append("        WHERE r.Es_Doc_Emi IN ('7') ")
                .append("          AND r.Ti_Emi     = '01' ")
                .append("          AND r.Co_Dep_Emi = :pCoDep ")
                .append("        GROUP BY r.Co_Dep_Emi ")
                .append("        UNION  ")
                .append("       SELECT 0 nu_can ")
                .append("      ) AS Q1 ")
                .append(" UNION ")
                .append(" SELECT 2 ORDEN, 'VB' TI_PEN, 'PARA VISTO BUENO' DE_PEN, ")
                .append("     'Documentos Administrativos listos para Visto Bueno' DE_RESUMEN, ")
                .append("     COUNT(1) NU_CAN, :pCoDep CO_DEP, '' CO_BANDEJA ")
                .append("   FROM IDOSGD.TDTV_REMITOS R WITH(NOLOCK) ")
                .append("  INNER JOIN IDOSGD.TDTV_PERSONAL_VB VB WITH(NOLOCK) ")
                .append("     ON (R.NU_ANN = VB.NU_ANN AND R.NU_EMI = VB.NU_EMI) ")
                .append("  WHERE(R.ES_DOC_EMI='7' OR (R.TI_EMI = '05' AND R.ES_DOC_EMI='5') ) ")
                .append("    AND VB.CO_DEP   =:pCoDep ")
                .append("    AND VB.CO_EMP_VB=:pCoEmp ")
                .append("    AND VB.IN_VB   <> '1' ")
                .append("  GROUP BY VB.CO_DEP ")
                .append(" UNION ")
                .append(" SELECT 3 ORDEN, '05' Ti_Pen, 'EN PROYECTO' De_Pen, ")
                .append("     'Documentos Administrativos en proceso de elaboración' de_resumen, ")
                .append("     sum(Nu_Can) Nu_Can, :pCoDep co_dep, '' co_bandeja ")
                .append("   FROM (SELECT COUNT(1) Nu_Can ")
                .append("           FROM IDOSGD.Tdtv_Remitos r WITH(NOLOCK) ")
                .append("          WHERE r.Es_Doc_Emi IN ('5') ")
                .append("             AND r.Ti_Emi     = '01' ")
                .append("             AND r.Co_Dep_Emi = :pCoDep ")
                .append("           GROUP BY r.Co_Dep_Emi ")
                .append("           UNION  ")
                .append("          SELECT 0 nu_can ")
                .append("        )AS Q2 ")
                .append(" UNION ")
                .append(" SELECT 5 orden, '07' ti_pen, 'MUY URGENTES' de_pen, ")
                .append("     'Documentos pendientes de recibir MUY URGENTES' de_resumen, ")
                .append("     COUNT (1) nu_can, :pCoDep co_dep, '' co_bandeja ")
                .append("   FROM IDOSGD.tdtv_remitos r WITH(NOLOCK) ")
                .append("  INNER JOIN IDOSGD.tdtv_destinos t WITH(NOLOCK) ")
                .append("     ON (r.nu_ann = t.nu_ann AND r.nu_emi = t.nu_emi) ")
                .append("  WHERE r.es_doc_emi NOT IN ('5', '9', '7') ")
                .append("    AND t.es_doc_rec = '0' ")
                .append("    AND t.es_eli     = '0' ")
                .append("    AND t.co_pri     = '3' ")
                .append("    AND r.in_oficio  = '0' ")
                .append("    AND t.co_dep_des = :pCoDep ")
                .append("  GROUP BY t.co_dep_des ")
                .append("  UNION ")
                .append(" SELECT 6 orden, '04' ti_pen, 'URGENTES' de_pen, ")
                .append("     'Documentos pendientes de recibir URGENTES' de_resumen, ")
                .append("     COUNT (1) nu_can, :pCoDep co_dep, '' co_bandeja ")
                .append("   FROM IDOSGD.tdtv_remitos r WITH(NOLOCK) ")
                .append("  INNER JOIN IDOSGD.tdtv_destinos t WITH(NOLOCK) ")
                .append("     ON (r.nu_ann = t.nu_ann AND r.nu_emi = t.nu_emi) ")
                .append("  WHERE r.es_doc_emi NOT IN ('5', '9', '7') ")
                .append("    AND t.es_doc_rec = '0' ")
                .append("    AND t.es_eli     = '0' ")
                .append("    AND t.co_pri     = '2' ")
                .append("    AND r.in_oficio  = '0' ")
                .append("    AND t.co_dep_des = :pCoDep ")
                .append("  GROUP BY t.co_dep_des ")
                .append(" UNION ")
                .append(" SELECT 7 orden, '02' Ti_Pen, 'RECIBIDOS (Oficina)'  De_Pen, ")
                .append("     'Documentos recibidos' de_resumen, ")
                .append("     SUM(nu_can) Nu_Can, :pCoDep co_dep, 'O' co_bandeja ")
                .append("   FROM (SELECT COUNT(1) Nu_Can ")
                .append("           FROM IDOSGD.Tdtv_Remitos r WITH(NOLOCK) ")
                .append("          INNER JOIN IDOSGD.Tdtv_Destinos t WITH(NOLOCK) ")
                .append("             ON (r.Nu_Ann = t.Nu_Ann AND r.Nu_Emi = t.Nu_Emi) ")
                .append("          WHERE r.Es_Doc_Emi NOT IN ('5', '7', '9') ")
                .append("            AND t.Es_Doc_Rec = '1' ")
                .append("            AND t.Es_Eli = '0'  ")
                .append("            AND t.Co_Dep_Des = :pCoDep ")
                .append("            AND t.CO_EMP_DES in ")
                .append("                (SELECT CO_EMPLEADO  ")
                .append("                   FROM IDOSGD.RHTM_DEPENDENCIA WITH(NOLOCK) ")
                .append("                  WHERE CO_DEPENDENCIA= :pCoDep ) ")
                .append("          GROUP BY t.Co_Dep_Des ")
                .append("          UNION ")
                .append("         SELECT 0 nu_can ")
                .append("         ) AS Q3 ")
                .append(" UNION ")
                .append(" SELECT 8 orden, '01' Ti_Pen, 'NO LEIDOS (Oficina)' De_Pen, ")
                .append("     'Documentos pendientes de recibir' de_resumen, ")
                .append("     SUM(nu_can) Nu_Can, :pCoDep co_dep, 'O' co_bandeja ")
                .append("   FROM (SELECT COUNT(1) Nu_Can ")
                .append("           FROM IDOSGD.Tdtv_Remitos r WITH(NOLOCK) ")
                .append("          INNER JOIN IDOSGD.Tdtv_Destinos t WITH(NOLOCK) ")
                .append("             ON (r.Nu_Ann = t.Nu_Ann AND r.Nu_Emi = t.Nu_Emi) ")
                .append("          WHERE r.Es_Doc_Emi NOT IN ('5', '7'  , '9') ")
                .append("            AND t.Es_Doc_Rec = '0'  ")
                .append("            AND t.Es_Eli = '0'  ")
                .append("            AND t.Co_Dep_Des = :pCoDep ")
                .append("            AND t.CO_EMP_DES in ")
                .append("                (SELECT CO_EMPLEADO ")
                .append("                   FROM IDOSGD.RHTM_DEPENDENCIA WITH(NOLOCK) ")
                .append("                  WHERE CO_DEPENDENCIA= :pCoDep ) ")
                .append("          GROUP BY t.Co_Dep_Des ")
                .append("          UNION  ")
                .append("         SELECT 0 nu_can ")
                .append("        ) AS Q3 ")
                .append(" UNION ")
                .append(" SELECT 14 orden, '02' Ti_Pen, 'RECIBIDOS (Profesional)' De_Pen, ")
                .append("     'Documentos recibidos' de_resumen, ")
                .append("     SUM(nu_can) Nu_Can, :pCoDep co_dep, 'P' co_bandeja ")
                .append("   FROM (SELECT COUNT(1) Nu_Can ")
                .append("           FROM IDOSGD.Tdtv_Remitos r WITH(NOLOCK) ")
                .append("          INNER JOIN IDOSGD.Tdtv_Destinos t WITH(NOLOCK) ")
                .append("             ON (r.Nu_Ann = t.Nu_Ann AND r.Nu_Emi = t.Nu_Emi) ")
                .append("          WHERE r.Es_Doc_Emi NOT IN ('5' , '7', '9') ")
                .append("            AND t.Es_Doc_Rec = '1' ")
                .append("            AND t.Es_Eli     = '0' ")
                .append("            AND t.Co_Dep_Des = :pCoDep ")
                .append("            AND NOT t.CO_EMP_DES in ")
                .append("                    (SELECT CO_EMPLEADO ")
                .append("                       FROM IDOSGD.RHTM_DEPENDENCIA WITH(NOLOCK) ")
                .append("                      WHERE CO_DEPENDENCIA= :pCoDep) ")
                .append("          GROUP BY t.Co_Dep_Des ")
                .append("          UNION  ")
                .append("         SELECT 0 nu_can ")
                .append("        ) AS Q3 ")
                .append(" UNION ")
                .append(" SELECT 17 orden, '01' Ti_Pen, 'NO LEIDOS (Profesional)' De_Pen, ")
                .append("     'Documentos pendientes de recibir' de_resumen, ")
                .append("     SUM(nu_can) Nu_Can, :pCoDep co_dep, 'P' co_bandeja ")
                .append("   FROM (SELECT COUNT(1) Nu_Can ")
                .append("           FROM IDOSGD.Tdtv_Remitos r WITH(NOLOCK) ")
                .append("          INNER JOIN IDOSGD.Tdtv_Destinos t WITH(NOLOCK) ")
                .append("             ON (r.Nu_Ann = t.Nu_Ann AND r.Nu_Emi = t.Nu_Emi) ")
                .append("          WHERE r.Es_Doc_Emi NOT IN ('5' , '7', '9') ")
                .append("            AND t.Es_Doc_Rec = '0' ")
                .append("            AND t.Es_Eli     = '0' ")
                .append("            AND t.Co_Dep_Des = :pCoDep ")
                .append("            AND NOT t.CO_EMP_DES in ")
                .append("                   (SELECT CO_EMPLEADO ")
                .append("                      FROM IDOSGD.RHTM_DEPENDENCIA WITH(NOLOCK) ")
                .append("                     WHERE CO_DEPENDENCIA= :pCoDep) ")
                .append("          GROUP BY t.Co_Dep_Des ")
                .append("          UNION  ")
                .append("          SELECT 0 nu_can ")
                .append("         ) AS Q3 ")
                .append(" UNION ")
                .append(" SELECT 18 orden, '08' Ti_Pen, '<p style=''color:#D50911;''>PENDIENTE DE ENVIO</p>' De_Pen, ")
                .append("     'Doc. pendientes de envio a mensajería' de_resumen, ")
                .append("     COUNT(1) Nu_Can, :pCoDep co_dep, '' co_bandeja ")
                .append("   FROM IDOSGD.Tdtv_Remitos r WITH(NOLOCK) ")
                .append("  WHERE r.Es_Doc_Emi ='0' ")
                .append("    AND r.DOC_ESTADO_MSJ is null ")
                .append("    AND r.TI_ENV_MSJ     is null ")
                .append("    AND r.CO_TIP_DOC_ADM in ('011','012','248') ")
                .append("    AND r.CO_GRU = '1' ")
                .append("    AND r.TI_EMI ='01' ")
                .append("    AND r.Es_Eli     = '0' ")
                .append("    AND r.Co_Dep_Emi = :pCoDep ")
                .append("    AND NOT EXISTS (SELECT D.NU_EMI ")
                .append("                      FROM IDOSGD.TDTV_DESTINOS D ")
                .append("                     WHERE TI_DES      ='01' ")
                .append("                       AND D.NU_EMI = r.NU_EMI ")
                .append("                       AND D.NU_ANN = r.NU_ANN ")
                .append("                       AND d.ES_ELI='0') ")
                .append("  GROUP BY r.Co_Dep_Emi ")
                .append("  ORDER BY 1   ");

        this.logger.info(sql.toString());
        List list = new ArrayList();
        Map objectParam = new HashMap();
        objectParam.put("pCoDep", coDependencia);
        objectParam.put("pCoEmp", coEmpleado);
        try {
            list = this.namedParameterJdbcTemplate.query(sql.toString(), objectParam, BeanPropertyRowMapper.newInstance(AvisoBandejaEntradaBean.class));
        } catch (EmptyResultDataAccessException e) {
            list = null;
        } catch (Exception e) {
            list = null;
            e.printStackTrace();
        }
        return list;
    }

    public List<AvisoBandejaEntradaBean> getBandejaEntradaAccesoPersonal(String coDependencia, String coEmpleado) {
         StringBuilder sql = new StringBuilder();
        sql.append(" SELECT 1 orden,'06' Ti_Pen, 'PARA DESPACHO' De_Pen, ")
                .append(" 'Documentos Administrativos listos para la Firma' de_resumen,")
                .append(" SUM(nu_can) Nu_Can, :pCoDep co_dep ")
                .append("  FROM (SELECT count(1) Nu_Can ")
                .append("          FROM IDOSGD.Tdtv_Remitos r WITH(NOLOCK) ")
                .append("         WHERE r.Es_Doc_Emi IN ('7') ")
                .append("           AND r.Ti_Emi = '01' ")
                .append("           AND r.Co_Dep_Emi = :pCoDep ")
                .append("           AND r.Co_Emp_Res = :pCoEmp ")
                .append("         GROUP BY r.Co_Dep_Emi, r.Co_Emp_Res ")
                .append("         UNION ")
                .append("         SELECT 0 nu_can ")
                .append("       ) AS Q1 ")
                .append(" UNION ")
                .append(" SELECT 2 ORDEN,'VB' TI_PEN, 'PARA VISTO BUENO' DE_PEN, ")
                .append("     'Documentos Administrativos listos para Visto Bueno' DE_RESUMEN, ")
                .append("     COUNT(1) NU_CAN, :pCoDep CO_DEP ")
                .append("   FROM IDOSGD.TDTV_REMITOS R  WITH(NOLOCK) ")
                .append("  INNER JOIN  IDOSGD.TDTV_PERSONAL_VB VB WITH(NOLOCK) ")
                .append("     ON (R.NU_ANN=VB.NU_ANN  AND R.NU_EMI=VB.NU_EMI ) ")
                .append("  WHERE (R.ES_DOC_EMI='7' OR (R.TI_EMI='05' AND R.ES_DOC_EMI='5')) ")
                .append("    AND VB.CO_DEP=:pCoDep     ")
                .append("    AND VB.CO_EMP_VB=:pCoEmp  ")
                .append("    AND VB.IN_VB <> '1'       ")
                .append("  GROUP BY VB.CO_DEP ")
                .append(" UNION ")
                .append(" SELECT 3 orden,'05' Ti_Pen, 'EN PROYECTO' De_Pen, ")
                .append("     'Documentos Administrativos en proceso de elaboracide_resumen', ")
                .append("     sum(Nu_Can) Nu_Can, :pCoDep co_dep ")
                .append("   FROM (SELECT COUNT(1) Nu_Can  ")
                .append("           FROM IDOSGD.Tdtv_Remitos r  WITH(NOLOCK) ")
                .append("          WHERE r.Es_Doc_Emi IN ('5')  ")
                .append("            AND r.Ti_Emi = '01'        ")
                .append("            AND r.Co_Dep_Emi = :pCoDep ")
                .append("            AND r.Co_Emp_Res = :pCoEmp ")
                .append("          GROUP BY r.Co_Dep_Emi, r.Co_Emp_Res ")
                .append("          UNION  ")
                .append("          SELECT 0 nu_can ")
                .append("         ) AS  Q2 ")
                .append(" UNION  ")
                .append(" SELECT 4 orden,'07' Ti_Pen, 'MUY URGENTES' De_Pen,  ")
                .append("     'Documentos pendientes de recibir MUY URGENTES' de_resumen,  ")
                .append("     COUNT(1) Nu_Can, :pCoDep co_dep  ")
                .append("   FROM IDOSGD.Tdtv_Remitos r  WITH(NOLOCK) ")
                .append("  INNER JOIN  IDOSGD.Tdtv_Destinos t  WITH(NOLOCK)  ")
                .append("     ON (r.Nu_Ann = t.Nu_Ann AND r.Nu_Emi = t.Nu_Emi) ")
                .append("  WHERE r.Es_Doc_Emi NOT IN ('5', '7', '9')  ")
                .append("    AND t.Co_Dep_Des = :pCoDep  ")
                .append("    AND t.Co_Emp_Des = :pCoEmp  ")
                .append("    AND t.Es_Doc_Rec = '0'      ")
                .append("    AND t.Es_Eli = '0'          ")
                .append("    AND t.Co_Pri = '3'          ")
                .append("    AND r.in_oficio = '0'       ")
                .append("  GROUP BY t.Co_Dep_Des, t.Co_Emp_Des ")
                .append(" UNION  ")
                .append(" SELECT 6 orden,'04' Ti_Pen, 'URGENTES' De_Pen,  ")
                .append("     'Documentos pendientes de recibir URGENTES' de_resumen, ")
                .append("     COUNT(1) Nu_Can, :pCoDep co_dep  ")
                .append("   FROM IDOSGD.Tdtv_Remitos r  WITH(NOLOCK)  ")
                .append("  INNER JOIN  IDOSGD.Tdtv_Destinos t  WITH(NOLOCK) ")
                .append("     ON (r.Nu_Ann = t.Nu_Ann AND r.Nu_Emi = t.Nu_Emi ) ")
                .append("  WHERE r.Es_Doc_Emi NOT IN ('5', '7', '9') ")
                .append("    AND t.Co_Dep_Des = :pCoDep   ")
                .append("    AND t.Co_Emp_Des = :pCoEmp   ")
                .append("    AND t.Es_Doc_Rec = '0'       ")
                .append("    AND t.Es_Eli = '0'           ")
                .append("    AND t.Co_Pri = '2'           ")
                .append("    AND r.in_oficio = '0'        ")
                .append("  GROUP BY t.Co_Dep_Des, t.Co_Emp_Des ")
                .append(" UNION  ")
                .append(" SELECT 4 orden,'02' Ti_Pen, 'RECIBIDOS' De_Pen,  ")
                .append("     'Documentos Recibidos' de_resumen,  ")
                .append("     SUM(nu_can) Nu_Can, :pCoDep co_dep  ")
                .append("   FROM (SELECT  COUNT(1) Nu_Can ")
                .append("           FROM IDOSGD.Tdtv_Remitos r WITH(NOLOCK)  ")
                .append("          INNER JOIN IDOSGD.Tdtv_Destinos t WITH(NOLOCK)  ")
                .append("             ON (r.Nu_Ann = t.Nu_Ann AND r.Nu_Emi = t.Nu_Emi) ")
                .append("          WHERE r.Es_Doc_Emi NOT IN ('5', '7', '9') ")
                .append("            AND t.Co_Dep_Des = :pCoDep              ")
                .append("            AND t.Co_Emp_Des = :pCoEmp              ")
                .append("            AND t.Es_Doc_Rec = '1'                  ")
                .append("            AND t.Es_Eli = '0'                      ")
                .append("          GROUP BY t.Co_Dep_Des, t.Co_Emp_Des ")
                .append("          UNION SELECT 0 nu_can  ")
                .append("        ) AS Q3  ")
                .append(" UNION ")
                .append(" SELECT 7 orden,'01' Ti_Pen, 'NO LEIDOS' De_Pen, ")
                .append("     'Documentos pendientes de recibir' de_resumen,  ")
                .append("     SUM(nu_can) Nu_Can, :pCoDep co_dep ")
                .append("   FROM (SELECT  COUNT(1) Nu_Can ")
                .append("           FROM IDOSGD.Tdtv_Remitos  r  WITH(NOLOCK) ")
                .append("          INNER JOIN  IDOSGD.Tdtv_Destinos t WITH(NOLOCK)  ")
                .append("             ON (r.Nu_Ann = t.Nu_Ann AND r.Nu_Emi = t.Nu_Emi) ")
                .append("          WHERE r.Es_Doc_Emi NOT IN ('5', '7', '9') ")
                .append("            AND t.Co_Dep_Des = :pCoDep ")
                .append("            AND t.Co_Emp_Des = :pCoEmp ")
                .append("            AND t.Es_Doc_Rec = '0'     ")
                .append("            AND t.Es_Eli = '0'         ")
                .append("          GROUP BY t.Co_Dep_Des, t.Co_Emp_Des ")
                .append("          UNION SELECT 0 nu_can   ")
                .append("        ) AS Q3 ");

        List list = new ArrayList();
        Map objectParam = new HashMap();
        objectParam.put("pCoDep", coDependencia);
        objectParam.put("pCoEmp", coEmpleado);
        try {
            list = this.namedParameterJdbcTemplate.query(sql.toString(), objectParam, BeanPropertyRowMapper.newInstance(AvisoBandejaEntradaBean.class));
        } catch (EmptyResultDataAccessException e) {
            list = null;
        } catch (Exception e) {
            list = null;
            e.printStackTrace();
        }
        return list;
    }

    public List<AvisoBandejaEntradaBean> getBandejaEntradaAccesoFuncionario(String coDependencia, String coEmpleado) {
         StringBuilder sql = new StringBuilder();
        sql.append("SELECT 1 orden,'06' Ti_Pen, 'PARA DESPACHO' De_Pen, ")
                .append("  'Documentos Administrativos listos para la Firma' de_resumen, ")
                .append("   COUNT(1) Nu_Can, :pCoDep co_dep  ")
                .append("   FROM IDOSGD.Tdtv_Remitos r WITH(NOLOCK)  ")
                .append("  WHERE r.Es_Doc_Emi IN ('7') --Listos para despacho ")
                .append("    AND r.Ti_Emi = '01'  ")
                .append("    AND r.Co_Dep_Emi = :pCoDep  ")
                .append("    AND (r.Co_Emp_Res = :pCoEmp OR r.co_emp_emi = :pCoEmp)  ")
                .append("  GROUP BY r.Co_Dep_Emi  ")
                .append(" UNION  ")
                .append(" SELECT 2 ORDEN,'VB' TI_PEN, 'PARA VISTO BUENO' DE_PEN, ")
                .append("     'Documentos Administrativos listos para Visto Bueno' DE_RESUMEN, ")
                .append("     COUNT(1) NU_CAN, :pCoDep CO_DEP ")
                .append("   FROM IDOSGD.TDTV_REMITOS R WITH(NOLOCK) ")
                .append("  INNER JOIN IDOSGD.TDTV_PERSONAL_VB VB WITH(NOLOCK)  ")
                .append("     ON (R.NU_ANN=VB.NU_ANN  AND R.NU_EMI=VB.NU_EMI)  ")
                .append("  WHERE R.ES_DOC_EMI='7'     ")
                .append("    AND VB.CO_DEP=:pCoDep    ")
                .append("    AND VB.CO_EMP_VB=:pCoEmp ")
                .append("    AND VB.IN_VB <> '1'      ")
                .append("  GROUP BY VB.CO_DEP  ")
                .append(" UNION  ")
                .append(" SELECT 3 orden,'05' Ti_Pen, 'EN PROYECTO' De_Pen, ")
                .append("     'Documentos Administrativos en proceso de elaboración' de_resumen, ")
                .append("     COUNT(1) Nu_Can, :pCoDep co_dep  ")
                .append("   FROM IDOSGD.Tdtv_Remitos r WITH(NOLOCK)  ")
                .append("  WHERE r.Es_Doc_Emi IN ('5') --En proyecto ")
                .append("    AND r.Ti_Emi = '01'  ")
                .append("    AND r.Co_Dep_Emi = :pCoDep ")
                .append("    AND (r.Co_Emp_Res = :pCoEmp OR r.co_emp_emi = :pCoEmp) ")
                .append("  GROUP BY r.Co_Dep_Emi    ")
                .append(" UNION  ")
                .append(" SELECT 4 orden,'07' Ti_Pen, 'MUY URGENTES' De_Pen, ")
                .append("     'Documentos pendientes de recibir MUY URGENTES' de_resumen, ")
                .append("     COUNT(1) Nu_Can, :pCoDep co_dep  ")
                .append("   FROM IDOSGD.Tdtv_Remitos  r  WITH(NOLOCK) ")
                .append("  INNER JOIN IDOSGD.Tdtv_Destinos t WITH(NOLOCK)  ")
                .append("     ON (r.Nu_Ann = t.Nu_Ann AND r.Nu_Emi = t.Nu_Emi)  ")
                .append("  WHERE r.Es_Doc_Emi NOT IN ('5', '7', '9') --en proyecto, anulados ")
                .append("    AND t.Es_Doc_Rec = '0'     ")
                .append("    AND t.Es_Eli = '0'         ")
                .append("    AND t.Co_Pri = '3'         ")
                .append("    AND t.Co_Dep_Des = :pCoDep ")
                .append("    AND t.Co_Emp_Des = :pCoEmp ")
                .append("  GROUP BY t.Co_Dep_Des, t.Co_Emp_Des ")
                .append(" UNION   ")
                .append(" SELECT 5 orden,'04' Ti_Pen, 'URGENTES' De_Pen, ")
                .append("     'Documentos pendientes de recibir URGENTES' de_resumen,  ")
                .append("     COUNT(1) Nu_Can, :pCoDep co_dep  ")
                .append("   FROM IDOSGD.Tdtv_Remitos  r  WITH(NOLOCK)   ")
                .append("  INNER JOIN IDOSGD.Tdtv_Destinos t WITH(NOLOCK) ")
                .append("     ON (r.Nu_Ann = t.Nu_Ann AND r.Nu_Emi = t.Nu_Emi) ")
                .append("  WHERE r.Es_Doc_Emi NOT IN ('5', '7', '9') --en proyecto, anulados ")
                .append("    AND t.Es_Doc_Rec = '0'     ")
                .append("    AND t.Es_Eli = '0'         ")
                .append("    AND t.Co_Pri = '2'         ")
                .append("    AND t.Co_Dep_Des = :pCoDep ")
                .append("    AND t.Co_Emp_Des = :pCoEmp ")
                .append("  GROUP BY t.Co_Dep_Des, t.Co_Emp_Des ")
                .append(" UNION   ")
                .append(" SELECT 4 orden,'01' Ti_Pen, 'BANDEJA' De_Pen, ")
                .append("     'Documentos pendientes de recibir' de_resumen,  ")
                .append("     COUNT(1) Nu_Can, :pCoDep co_dep    ")
                .append("   FROM IDOSGD.Tdtv_Remitos r WITH(NOLOCK)   ")
                .append("  INNER JOIN IDOSGD.Tdtv_Destinos t WITH(NOLOCK)   ")
                .append("     ON (r.Nu_Ann = t.Nu_Ann AND r.Nu_Emi = t.Nu_Emi)  ")
                .append("  WHERE r.Es_Doc_Emi NOT IN ('5', '7', '9') --en proyecto, anulados  ")
                .append("    AND t.Es_Doc_Rec = '1'      ")
                .append("    AND t.Es_Eli = '0'          ")
                .append("    AND t.Co_Dep_Des = :pCoDep  ")
                .append("    AND t.Co_Emp_Des = :pCoEmp  ")
                .append("  GROUP BY t.Co_Dep_Des, t.Co_Emp_Des ")
                .append(" UNION  ")
                .append(" SELECT 7 orden,'01' Ti_Pen, 'NO LEIDOS' De_Pen, ")
                .append("     'Documentos pendientes de recibir' de_resumen, ")
                .append("     COUNT(1) Nu_Can, :pCoDep co_dep   ")
                .append("   FROM IDOSGD.Tdtv_Remitos  r  WITH(NOLOCK)  ")
                .append("  INNER JOIN IDOSGD.Tdtv_Destinos t WITH(NOLOCK)   ")
                .append("     ON (r.Nu_Ann = t.Nu_Ann AND r.Nu_Emi = t.Nu_Emi)  ")
                .append("  WHERE  r.Es_Doc_Emi NOT IN ('5', '7', '9') --en proyecto, anulados  ")
                .append("    AND t.Es_Doc_Rec = '0'     ")
                .append("    AND t.Es_Eli = '0'         ")
                .append("    AND t.Co_Dep_Des = :pCoDep ")
                .append("    AND t.Co_Emp_Des = :pCoEmp ")
                .append("  GROUP BY t.Co_Dep_Des, t.Co_Emp_Des ");

        List list = new ArrayList();
        Map objectParam = new HashMap();
        objectParam.put("pCoDep", coDependencia);
        objectParam.put("pCoEmp", coEmpleado);
        try {
            list = this.namedParameterJdbcTemplate.query(sql.toString(), objectParam, BeanPropertyRowMapper.newInstance(AvisoBandejaEntradaBean.class));
        } catch (EmptyResultDataAccessException e) {
            list = null;
        } catch (Exception e) {
            list = null;
            e.printStackTrace();
        }
        return list;
    }

    public List<EtiquetaBandejaEnBean> getListEtiquetaBandejaEntrada(String coDep, String coEmp) {
         StringBuilder sql = new StringBuilder();
        sql.append("select e.co_est,e.de_est descripcion,count(*) numero_documentos,d.co_dep_des,d.co_emp_rec ");
        sql.append("from IDOSGD.tdtv_bandeja_rec d  WITH(NOLOCK) ");
        sql.append("INNER JOIN IDOSGD.tdtr_estados e  WITH(NOLOCK) ON e.co_est=d.co_etiqueta_rec  ");
        sql.append("where d.co_dep_des = :pCoDep ");
        sql.append("and d.co_emp_rec=:pCoEmp ");
        sql.append("and d.co_etiqueta_rec<>0 ");
        sql.append("and d.es_doc_emi not in ('5','7','9') ");
        sql.append("and e.de_tab='CO_ETIQUETA_REC' ");
        sql.append("group by e.co_est,e.de_est,d.co_dep_des,d.co_emp_rec ");
        sql.append("order by e.co_est ");
        List list = new ArrayList();
        Map objectParam = new HashMap();
        objectParam.put("pCoDep", coDep);
        objectParam.put("pCoEmp", coEmp);
        try {
            list = this.namedParameterJdbcTemplate.query(sql.toString(), objectParam, BeanPropertyRowMapper.newInstance(EtiquetaBandejaEnBean.class));
        } catch (EmptyResultDataAccessException e) {
            list = null;
        } catch (Exception e) {
            list = null;
            e.printStackTrace();
        }
        return list;
    }

    public List<AvisoBandejaEntradaBean> getBandejaEntradaAltaDireccionTotal(String coDependencia, String coEmpleado) {
        return null;
    }
}
