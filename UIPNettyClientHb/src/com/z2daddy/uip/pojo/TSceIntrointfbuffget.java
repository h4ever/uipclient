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
 * TSceIntrointfbuffget generated by hbm2java
 */
@Entity
@Table(name = "T_SCE_INTROINTFBUFFGET", schema = "UCR_ASRV")
public class TSceIntrointfbuffget implements java.io.Serializable {

	private TSceIntrointfbuffgetId id;
	private String callerno;
	private String bizcode;
	private Date taskdate;
	private String intfresp;

	public TSceIntrointfbuffget() {
	}

	public TSceIntrointfbuffget(TSceIntrointfbuffgetId id, String callerno, String bizcode) {
		this.id = id;
		this.callerno = callerno;
		this.bizcode = bizcode;
	}

	public TSceIntrointfbuffget(TSceIntrointfbuffgetId id, String callerno, String bizcode, Date taskdate,
			String intfresp) {
		this.id = id;
		this.callerno = callerno;
		this.bizcode = bizcode;
		this.taskdate = taskdate;
		this.intfresp = intfresp;
	}

	@EmbeddedId

	@AttributeOverrides({
			@AttributeOverride(name = "touchid", column = @Column(name = "TOUCHID", nullable = false, length = 30)),
			@AttributeOverride(name = "seqtag", column = @Column(name = "SEQTAG", nullable = false, length = 3)) })
	public TSceIntrointfbuffgetId getId() {
		return this.id;
	}

	public void setId(TSceIntrointfbuffgetId id) {
		this.id = id;
	}

	@Column(name = "CALLERNO", nullable = false, length = 20)
	public String getCallerno() {
		return this.callerno;
	}

	public void setCallerno(String callerno) {
		this.callerno = callerno;
	}

	@Column(name = "BIZCODE", nullable = false, length = 10)
	public String getBizcode() {
		return this.bizcode;
	}

	public void setBizcode(String bizcode) {
		this.bizcode = bizcode;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "TASKDATE", length = 7)
	public Date getTaskdate() {
		return this.taskdate;
	}

	public void setTaskdate(Date taskdate) {
		this.taskdate = taskdate;
	}

	@Column(name = "INTFRESP", length = 2000)
	public String getIntfresp() {
		return this.intfresp;
	}

	public void setIntfresp(String intfresp) {
		this.intfresp = intfresp;
	}

}
