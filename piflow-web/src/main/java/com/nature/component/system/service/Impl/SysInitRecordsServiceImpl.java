package com.nature.component.system.service.Impl;

import com.nature.base.util.*;
import com.nature.base.vo.UserVo;
import com.nature.common.constant.SysParamsCache;
import com.nature.component.group.model.PropertyTemplate;
import com.nature.component.group.model.StopGroup;
import com.nature.component.group.model.StopsTemplate;
import com.nature.component.system.model.SysInitRecords;
import com.nature.component.system.service.ISysInitRecordsService;
import com.nature.domain.system.SysInitRecordsDomain;
import com.nature.mapper.flow.PropertyTemplateMapper;
import com.nature.mapper.flow.StopGroupMapper;
import com.nature.mapper.flow.StopsTemplateMapper;
import com.nature.third.service.IStop;
import org.apache.tomcat.util.threads.ThreadPoolExecutor;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@Service
public class SysInitRecordsServiceImpl implements ISysInitRecordsService {

    Logger logger = LoggerUtil.getLogger();

    @Resource
    private SysInitRecordsDomain sysInitRecordsDomain;

    @Resource
    private IStop stopImpl;

    @Resource
    StopGroupMapper stopGroupMapper;

    @Resource
    private StopsTemplateMapper stopsTemplateMapper;

    @Resource
    private PropertyTemplateMapper propertyTemplateMapper;

    @Override
    public String initComponents() {
        Map<String, Object> rtnMap = new HashMap<>();
        ExecutorService es = new ThreadPoolExecutor(1, 5, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(100000));
        UserVo currentUser = SessionUserUtil.getCurrentUser();
        Boolean aBoolean = loadStopGroup(currentUser.getUsername());
        if (aBoolean) {
            String[] stopNameList = stopImpl.getAllStops();
            // The call is successful, empty the "Stop" message and insert
            stopGroupMapper.deleteStopsPropertyInfo();
            int deleteStopsInfo = stopGroupMapper.deleteStopsInfo();
            logger.info("Successful deletion StopsInfo" + deleteStopsInfo + "piece of data!!!");
            if (null != stopNameList && stopNameList.length > 0) {
                for (String stopListInfos : stopNameList) {
                    es.execute(() -> {
                        Boolean aBoolean1 = loadStop(stopListInfos);
                        if (!aBoolean1) {
                            logger.warn("stop load failed, bundel : " + stopListInfos);
                        }
                    });
                }
            }
        }
        SysParamsCache.THREAD_POOL_EXECUTOR = ((ThreadPoolExecutor) es);
        rtnMap.put("code", 200);
        return JsonUtils.toJsonNoException(rtnMap);
    }

    @Override
    public String threadMonitoring() {
        Map<String, Object> rtnMap = new HashMap<>();
        rtnMap.put("code", 500);
        if (null == SysParamsCache.THREAD_POOL_EXECUTOR) {
            return JsonUtils.toJsonNoException(rtnMap);
        }
        //Total number of threads
        double taskCount = SysParamsCache.THREAD_POOL_EXECUTOR.getTaskCount();
        //Number of execution completion threads
        double completedTaskCount = SysParamsCache.THREAD_POOL_EXECUTOR.getCompletedTaskCount();
        double progressNum = ((completedTaskCount / taskCount) * 40);
        progressNum = 40;
        if (39 < progressNum && progressNum < 40) {
            progressNum = 39;
        }
        long progressNumLong = (long) Math.ceil(progressNum) + 60;

        if (100 == progressNumLong) {
            addSysInitRecordsAndSave();
        }
        rtnMap.put("progress", progressNumLong);
        rtnMap.put("code", 200);
        return JsonUtils.toJsonNoException(rtnMap);
    }

    private Boolean loadStopGroup(String currentUser) {
        String[] group = stopImpl.getAllGroup();
        if (null != group && group.length > 0) {
            // The call is successful, the group table information is cleared and then inserted.
            stopGroupMapper.deleteGroupCorrelation();
            int deleteGroup = stopGroupMapper.deleteGroup();
            logger.debug("Successful deletion Group" + deleteGroup + "piece of data!!!");
            int a = 0;
            for (String string : group) {
                if (string.length() > 0) {
                    StopGroup stopGroup = new StopGroup();
                    stopGroup.setId(SqlUtils.getUUID32());
                    stopGroup.setCrtDttm(new Date());
                    stopGroup.setCrtUser(currentUser);
                    stopGroup.setLastUpdateUser(currentUser);
                    stopGroup.setEnableFlag(true);
                    stopGroup.setLastUpdateDttm(new Date());
                    stopGroup.setGroupName(string);
                    int insertStopGroup = stopGroupMapper.insertStopGroup(stopGroup);
                    a += insertStopGroup;
                }
            }
            logger.debug("Successful insert Group" + a + "piece of data!!!");
        }
        return true;
    }

    private Boolean loadStop(String stopListInfos) {
        logger.info("Now the call is：" + stopListInfos);
        StopsTemplate stopsTemplate = stopImpl.getStopInfo(stopListInfos);
        if (null != stopsTemplate) {
            List<StopsTemplate> listStopsTemplate = new ArrayList<>();
            listStopsTemplate.add(stopsTemplate);
            int insertStopsTemplate = stopsTemplateMapper.insertStopsTemplate(listStopsTemplate);
            logger.info("flow_stops_template affects the number of rows : " + insertStopsTemplate);
            logger.info("=============================association_groups_stops_template=====start==================");
            List<StopGroup> stopGroupList = stopsTemplate.getStopGroupList();
            for (StopGroup stopGroup : stopGroupList) {
                String stopGroupId = stopGroup.getId();
                String stopsTemplateId = stopsTemplate.getId();
                int insertAssociationGroupsStopsTemplate = stopGroupMapper.insertAssociationGroupsStopsTemplate(stopGroupId, stopsTemplateId);
                logger.info("association_groups_stops_template Association table insertion affects the number of rows : " + insertAssociationGroupsStopsTemplate);
            }
            List<PropertyTemplate> properties = stopsTemplate.getProperties();
            int insertPropertyTemplate = propertyTemplateMapper.insertPropertyTemplate(properties);
            logger.info("flow_stops_property_template affects the number of rows : " + insertPropertyTemplate);
        }
        return true;
    }

    private Boolean addSysInitRecordsAndSave() {
        SysInitRecords sysInitRecords = new SysInitRecords();
        sysInitRecords.setId(SqlUtils.getUUID32());
        sysInitRecords.setInitDate(new Date());
        sysInitRecords.setIsSucceed(true);
        sysInitRecordsDomain.saveOrUpdate(sysInitRecords);
        SysParamsCache.setIsBootComplete(true);
        return true;
    }


}
