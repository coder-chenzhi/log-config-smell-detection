package edu.zju.entity;

public class Location{
	String describe;
	Integer line;
	public Location(){
		describe="";
		line=0;
	}
	public Location(String des,Integer line){
		this.describe=des;
		this.line=line;
	}
	public String getDescribe() {
		return describe;
	}
	public void setDescribe(String describe) {
		this.describe = describe;
	}
	public Integer getLine() {
		return line;
	}
	public void setLine(Integer line) {
		this.line = line;
	}
	
}