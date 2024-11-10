package edu.cs;

public class FileRecord {
	  private Integer pid;
	  private String fileName;
	  
	  FileRecord(Integer pid, String fileName){
		  this.pid = pid;
		  this.fileName = fileName;
	  }
	  
	  public Integer getPid() {
		  return this.pid;
	  }
	  
	  public String getFileName() {
		  return this.fileName;
	  }
}