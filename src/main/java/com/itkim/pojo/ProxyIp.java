package com.itkim.pojo;

public class ProxyIp {

	String ip;
	
	Integer port;
	 
	String protocol; //http 或 https
	
	String address;

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	@Override
	public String toString() {
		return "ProxyIp [ip=" + ip + ", port=" + port + ", protocol="
				+ protocol + ", address=" + address + "]";
	}
	
	
}
