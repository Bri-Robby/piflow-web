package com.nature.component.workFlow.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Where;

import com.nature.base.BaseHibernateModelUUIDNoCorpAgentId;

/**
 * 组名称表
 * 
 * @author Nature
 *
 */
@Entity
@Table(name = "FLOW_SOTPS_GROUPS")
public class StopGroup extends BaseHibernateModelUUIDNoCorpAgentId {

	private static final long serialVersionUID = 1L;

	private String groupName; // 组名称

	// 组包含的stop
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "association_groups_stops_template", joinColumns = @JoinColumn(name = "groups_id"), inverseJoinColumns = @JoinColumn(name = "stops_template_id"))
	@Where(clause = "enable_flag=1")
	private List<StopsTemplate> stopsTemplateList = new ArrayList<StopsTemplate>();

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public List<StopsTemplate> getStopsTemplateList() {
		return stopsTemplateList;
	}

	public void setStopsTemplateList(List<StopsTemplate> stopsTemplateList) {
		this.stopsTemplateList = stopsTemplateList;
	}

}