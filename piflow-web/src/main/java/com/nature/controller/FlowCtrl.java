package com.nature.controller;

import com.nature.base.util.HttpUtils;
import com.nature.base.util.JsonUtils;
import com.nature.base.util.LoggerUtil;
import com.nature.base.util.Utils;
import com.nature.base.vo.StatefulRtnBase;
import com.nature.component.mxGraph.model.MxCell;
import com.nature.component.mxGraph.service.IMxCellService;
import com.nature.component.mxGraph.service.IMxGraphModelService;
import com.nature.component.mxGraph.service.IMxGraphService;
import com.nature.component.workFlow.model.Flow;
import com.nature.component.workFlow.model.FlowInfoDb;
import com.nature.component.workFlow.model.Property;
import com.nature.component.workFlow.model.Stops;
import com.nature.component.workFlow.service.*;
import com.nature.third.inf.IGetFlowInfo;
import com.nature.third.inf.IGetFlowLog;
import com.nature.third.inf.IStartFlow;
import com.nature.third.inf.IStopFlow;
import com.nature.third.vo.flowLog.AppVo;
import com.nature.third.vo.flowLog.FlowLog;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/flow/*")
public class FlowCtrl {

    /**
     * @Title ������־��ע�ⶼ��"org.slf4j"����
     */
    Logger logger = LoggerUtil.getLogger();

    @Autowired
    private IFlowService flowServiceImpl;

    @Autowired
    private IStartFlow startFlowImpl;

    @Autowired
    private IStopFlow stopFlowImpl;

    @Autowired
    private IGetFlowInfo getFlowInfoImpl;

    @Autowired
    private IGetFlowLog getFlowLogImpl;

    @Autowired
    private IPropertyService propertyServiceImpl;

    @Autowired
    private IStopsService stopsServiceImpl;

    @Autowired
    private IPathsService pathsServiceImpl;

    @Autowired
    private IMxGraphModelService mxGraphModelServiceImpl;

    @Autowired
    private IFlowInfoDbService flowInfoDbServiceImpl;

    @Autowired
    private IMxCellService mxCellServiceImpl;

    @Autowired
    private IMxGraphService mxGraphServiceImpl;

    /**
     * ����flow
     *
     * @param request
     * @param model
     * @return
     */
    @RequestMapping("/runFlow")
    @ResponseBody
    public String runFlow(HttpServletRequest request, Model model) {
        Map<String, String> rtnMap = new HashMap<String, String>();
        rtnMap.put("code", "0");
        String errMsg = "";
        String flowId = request.getParameter("flowId");
        if (StringUtils.isNotBlank(flowId)) {
            // ����flowId��ѯflow
            Flow flowById = flowServiceImpl.getFlowById(flowId);
            // addFlow��Ϊ����ReqRtnStatus��ֵΪtrue,�򱣴�ɹ�
            if (null != flowById) {
                String startFlow = startFlowImpl.startFlow(flowById);
                if (StringUtils.isNotBlank(startFlow)) {
                    FlowInfoDb flowInfoDb = getFlowInfoImpl.AddFlowInfo(startFlow);
                    // flowInfo�ӿڷ���Ϊ�յ����
                    if (null == flowInfoDb) {
                        rtnMap.put("flowInfoDbMsg", "flowInfoDb����ʧ��");
                    }
                    StatefulRtnBase saveAppId = flowServiceImpl.saveAppId(flowId, flowInfoDb);
                    String saveAppIdMsg = "";
                    if (null != saveAppId && saveAppId.isReqRtnStatus()) {
                        saveAppIdMsg = "flowIdΪ" + flowId + "��flow������appID�ɹ�" + startFlow;
                        rtnMap.put("saveAppIdMsg", saveAppIdMsg);
                        logger.info(saveAppIdMsg);
                    } else {
                        saveAppIdMsg = "flowIdΪ" + flowId + "��flow������appID����";
                        rtnMap.put("saveAppIdMsg", saveAppIdMsg);
                        logger.warn(saveAppIdMsg);
                    }
                    errMsg = "�����ɹ������ص�appidΪ��" + startFlow;
                    rtnMap.put("code", "1");
                    rtnMap.put("appid", startFlow);
                    rtnMap.put("errMsg", errMsg);
                } else {
                    errMsg = "����ʧ��";
                    rtnMap.put("errMsg", errMsg);
                    logger.warn(errMsg);
                }
            } else {
                errMsg = "δ��ѯ��flowIdΪ" + flowId + "��flow";
                rtnMap.put("errMsg", errMsg);
                logger.warn(errMsg);
            }
        } else {
            errMsg = "flowIdΪ��";
            rtnMap.put("errMsg", errMsg);
            logger.warn(errMsg);
        }
        return JsonUtils.toJsonNoException(rtnMap);
    }

    /**
     * ֹͣflow
     *
     * @param request
     * @param model
     * @return
     */
    @RequestMapping("/stopFlow")
    @ResponseBody
    public String stopFlow(HttpServletRequest request, Model model) {
        Map<String, String> rtnMap = new HashMap<String, String>();
        rtnMap.put("code", "0");
        String flowId = request.getParameter("flowId");
        if (StringUtils.isNotBlank(flowId)) {
            // ����flowId��ѯflow
            Flow flowById = flowServiceImpl.getFlowById(flowId);
            // addFlow��Ϊ����ReqRtnStatus��ֵΪtrue,�򱣴�ɹ�
            if (null != flowById) {
                String appId = flowById.getAppId().getId();
                if (StringUtils.isNotBlank(appId)) {
                    String flowStop = stopFlowImpl.stopFlow(appId);
                    if (StringUtils.isNotBlank(flowStop)) {
                        rtnMap.put("code", "1");
                        rtnMap.put("rtnMsg","ֹͣ�ɹ�������״̬Ϊ��" + flowStop);
                    }
                }
            } else {
                logger.warn("δ��ѯ��flowIdΪ" + flowId + "��flow");
                rtnMap.put("rtnMsg","δ��ѯ��flowIdΪ" + flowId + "��flow");
            }
        } else {
            logger.warn("flowIdΪ��");
        }
        return JsonUtils.toJsonNoException(rtnMap);
    }

    /**
     * ��ȡflow��Log�ĵ�ַ
     *
     * @param request
     * @param model
     * @return
     */
    @RequestMapping("/getLogUrl")
    @ResponseBody
    public Map<String, String> getLogUrl(HttpServletRequest request, Model model) {
        Map<String, String> rtnMap = new HashMap<>();
        rtnMap.put("code", "0");
        String appId = request.getParameter("appId");
        if (StringUtils.isNotBlank(appId)) {
            FlowLog flowlog = getFlowLogImpl.getFlowLog(appId);
            if (null != flowlog) {
                AppVo app = flowlog.getApp();
                if (null != app) {
                    rtnMap.put("code", "1");
                    rtnMap.put("stdoutLog", app.getAmContainerLogs() + "/stdout/?start=0");
                    rtnMap.put("stderrLog", app.getAmContainerLogs() + "/stderr/?start=0");
                }
            }
        } else {
            logger.warn("appIdΪ��");
        }

        return rtnMap;
    }

    /**
     * ͨ��flow�ĵ�ַ����log
     *
     * @param request
     * @param model
     * @return
     */
    @RequestMapping("/getLog")
    @ResponseBody
    public String getLog(HttpServletRequest request, Model model) {
        String rtnMsg = "";
        String urlStr = request.getParameter("url");
        if (StringUtils.isNotBlank(urlStr)) {
            rtnMsg = HttpUtils.getHtml(urlStr);
        } else {
            logger.warn("urlStrΪ��");
        }

        return rtnMsg;
    }

    @RequestMapping("/queryFlowData")
    @ResponseBody
    public Flow saveData(String load) {
        Flow flow = flowServiceImpl.getFlowById(load);
        return flow;
    }

    /**
     * �������flow
     *
     * @param flow
     * @return
     */
    @RequestMapping("/saveFlowInfo")
    @ResponseBody
    public Flow saveFlowInfo(Flow flow) {
        String id = Utils.getUUID32();
        flow.setId(id);
        flow.setName(flow.getName());
        flow.setDescription(flow.getDescription());
        flow.setCrtDttm(new Date());
        flow.setCrtUser("wdd");
        flow.setLastUpdateDttm(new Date());
        flow.setLastUpdateUser("ddw");
        flow.setEnableFlag(true);
        flow.setVersion(0L);
        flow.setUuid(id);
        flowServiceImpl.addFlow(flow);
        return flow;
    }

    /**
     * �޸�Flow
     *
     * @param flow
     * @return
     */
    @RequestMapping("/updateFlowInfo")
    @ResponseBody
    public int updateFlowInfo(Flow flow) {
        String id = flow.getId();
        flow.setId(id);
        flow.setName(flow.getName());
        flow.setDescription(flow.getDescription());
        flow.setLastUpdateDttm(new Date());
        flow.setLastUpdateUser("ddw");
        flow.setVersion(0L + 1);
        int result = flowServiceImpl.updateFlow(flow);
        return result;
    }

    /**
     * ����flowIdɾ��flow������Ϣ
     * @param id
     * @return
     */
    @RequestMapping("/deleteFlow")
    @ResponseBody
    @Transactional
    public int deleteFlow(String id) {
        int deleteFLowInfo = 0;
        Flow flowById = flowServiceImpl.getFlowById(id);
        if (null != flowById) {
            //��ѭ��ɾ��stop����
            for (Stops stopId : flowById.getStopsList()) {
                for (Property property : stopId.getProperties()) {
                    propertyServiceImpl.deleteStopsPropertyByStopId(property.getId());
                }
            }
            //ɾ��stop
            stopsServiceImpl.deleteStopsByFlowId(flowById.getId());
            //ɾ��paths
            pathsServiceImpl.deletePathsByFlowId(flowById.getId());
            List<MxCell> root = flowById.getMxGraphModel().getRoot();
            for (MxCell mxcell : root) {
                mxCellServiceImpl.deleteMxCellById(mxcell.getId());
                if (mxcell.getMxGeometry() != null) {
                    mxGraphServiceImpl.deleteMxGraphById(mxcell.getMxGeometry().getId());
                }
                if (mxcell.getMxGraphModel() != null) {
                    mxGraphModelServiceImpl.deleteMxGraphModelById(mxcell.getMxGraphModel().getId());
                }
            }
            deleteFLowInfo = flowServiceImpl.deleteFLowInfo(id);
            mxGraphModelServiceImpl.deleteMxGraphModelById(flowById.getMxGraphModel().getId());
            if (flowById.getAppId()!=null) {
                flowInfoDbServiceImpl.deleteFlowInfoById(flowById.getAppId().getId());
            }

        }
        return deleteFLowInfo;
    }
}
