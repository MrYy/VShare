package com.ANT.MiddleWare.PartyPlayerActivity;

public class ConfigureData {
	private static final String TAG = ConfigureData.class.getSimpleName();

	private String url;
	private boolean serviceAlive;
	private WorkMode workingMode;
	public static enum WorkMode {
		LOCAL_MODE,G_MDOE,COOPERATIVE_MODE,JUNIT_TEST_MODE,
		FAKE_MODE
	}

	public ConfigureData(String url) {
		this.url = url;
		serviceAlive = false;
		workingMode = WorkMode.LOCAL_MODE;
	}

	public WorkMode getWorkingMode() {
		return workingMode;
	}

	public void setWorkingMode(WorkMode workingMode) {
		this.workingMode = workingMode;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public boolean isServiceAlive() {
		return serviceAlive;
	}

	public void setServiceAlive(boolean serviceAlive) {
		this.serviceAlive = serviceAlive;
	}

}
