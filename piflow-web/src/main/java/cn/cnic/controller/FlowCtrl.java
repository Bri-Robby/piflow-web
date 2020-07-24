package cn.cnic.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.cnic.base.util.SessionUserUtil;
import cn.cnic.component.flow.model.Flow;
import cn.cnic.component.flow.service.IFlowGroupService;
import cn.cnic.component.flow.service.IFlowService;
import cn.cnic.component.flow.vo.FlowVo;

@Controller
@RequestMapping("/flow")
public class FlowCtrl {

    @Resource
    private IFlowService flowServiceImpl;

    /**
     * flowList page query
     *
     * @param page
     * @param limit
     * @param param
     * @return
     */
    @RequestMapping("/getFlowListPage")
    @ResponseBody
    public String getFlowListPage(Integer page, Integer limit, String param) {
        boolean isAdmin = SessionUserUtil.isAdmin();
        String username = SessionUserUtil.getCurrentUsername();
        return flowServiceImpl.getFlowListPage(username, isAdmin, page, limit, param);
    }

    /**
     * Enter the front page of the drawing board
     *
     * @param request
     * @return
     */
    @RequestMapping("/drawingBoardData")
    @ResponseBody
    public String drawingBoardData(HttpServletRequest request) {
        String load = request.getParameter("load");
        //set parentAccessPath
        String parentAccessPath = request.getParameter("parentAccessPath");
        String username = SessionUserUtil.getCurrentUsername();
        boolean isAdmin = SessionUserUtil.isAdmin();
        return flowServiceImpl.drawingBoardData(username, isAdmin, load, parentAccessPath);
    }

    /**
     * run Flow
     *
     * @param request
     * @return
     */
    @RequestMapping("/runFlow")
    @ResponseBody
    public String runFlow(HttpServletRequest request) {
        String flowId = request.getParameter("flowId");
        String runMode = request.getParameter("runMode");
        String username = SessionUserUtil.getCurrentUsername();
        boolean isAdmin = SessionUserUtil.isAdmin();
        return flowServiceImpl.runFlow(username, isAdmin, flowId, runMode);
    }

    @RequestMapping("/queryFlowData")
    @ResponseBody
    public String queryFlowData(String load) {
        return flowServiceImpl.getFlowVoById(load);
    }

    /**
     * save flow
     *
     * @param flowVo
     * @return
     */
    @RequestMapping("/saveFlowInfo")
    @ResponseBody
    public String saveFlowInfo(FlowVo flowVo) {
        String username = SessionUserUtil.getCurrentUsername();
        return flowServiceImpl.addFlow(username, flowVo);
    }

    /**
     * update Flow
     *
     * @param flow
     * @return
     */
    @RequestMapping("/updateFlowInfo")
    @ResponseBody
    public String updateFlowInfo(Flow flow) {
        String username = SessionUserUtil.getCurrentUsername();
        return flowServiceImpl.updateFlow(username, flow);
    }

    /**
     * Delete flow association information according to flowId
     *
     * @param id
     * @return
     */
    @RequestMapping("/deleteFlow")
    @ResponseBody
    @Transactional
    public String deleteFlow(String id) {
        String username = SessionUserUtil.getCurrentUsername();
        boolean isAdmin = SessionUserUtil.isAdmin();
        return flowServiceImpl.deleteFLowInfo(username, isAdmin, id);
    }

    @RequestMapping("/updateFlowBaseInfo")
    @ResponseBody
    public String updateFlowBaseInfo(String fId, FlowVo flowVo) {
        String username = SessionUserUtil.getCurrentUsername();
        return flowServiceImpl.updateFlowBaseInfo(username, fId, flowVo);
    }

}
