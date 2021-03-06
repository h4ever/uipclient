package com.z2daddy.uip.pojo;
// Generated 2017-3-6 16:40:52 by Hibernate Tools 3.6.0.Final

import java.util.Date;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * TSceIntrointfbuffset generated by hbm2java
 */
@Entity
@Table(name = "T_SCE_INTROINTFBUFFSET", schema = "UCR_ASRV")
public class TSceIntrointfbuffset implements java.io.Serializable {

	private TSceIntrointfbuffsetId id;
	private String callerno;
	private String intfparams;
	private Date taskdate;

	public TSceIntrointfbuffset() {
	}

	public TSceIntrointfbuffset(TSceIntrointfbuffsetId id, String callerno) {
		this.id = id;
		this.callerno = callerno;
	}

	public TSceIntrointfbuffset(TSceIntrointfbuffsetId id, String callerno, String intfparams, Date taskdate) {
		this.id = id;
		this.callerno = callerno;
		this.intfparams = intfparams;
		this.taskdate = taskdate;
	}

	@EmbeddedId

	@AttributeOverrides({
			@AttributeOverride(name = "tasktag", column = @Column(name = "TASKTAG", nullable = false, length = 3)),
			@AttributeOverride(name = "touchid", column = @Column(name = "TOUCHID", nullable = false, length = 30)) })
	public TSceIntrointfbuffsetId getId() {
		return this.id;
	}

	public void setId(TSceIntrointfbuffsetId id) {
		this.id = id;
	}

	@Column(name = "CALLERNO", nullable = false, length = 20)
	public String getCallerno() {
		return this.callerno;
	}

	public void setCallerno(String callerno) {
		this.callerno = callerno;
	}

	@Column(name = "INTFPARAMS", length = 200)
	public String getIntfparams() {
		return this.intfparams;
	}

	public void setIntfparams(String intfparams) {
		this.intfparams = intfparams;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "TASKDATE", length = 7)
	public Date getTaskdate() {
		return this.taskdate;
	}

	public void setTaskdate(Date taskdate) {
		this.taskdate = taskdate;
	}

}
