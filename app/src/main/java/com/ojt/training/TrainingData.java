/*@ID: CN20140001
 * @Description: srcTrainingData is used to store the training data 
 * @Developer: Arunachalam
 * @Version 1.0
 * @Date: 20/05/2014
 */
package com.ojt.training;

public class TrainingData
{
	private String strImageName=null;
    private String strID=null;
    private String strContentName=null;
    private String strTitle=null;
    private String strFileType=null;
    private String strStatus=null;
    private String strImageURL=null;
    private String strReference=null;
 
    public void setID(String strID) 
    {
    	 this.strID=strID;
    }
    public void setImageName(String strImageName)
    {
    	this.strImageName=strImageName;
    }
    public void setContentName(String strContentName)
    {
    	this.strContentName=strContentName;
    }
    public void setTitle(String strTitle)
    {
    	this.strTitle=strTitle;
    }
    public void setFileType(String strFileType)
    {
    	this.strFileType=strFileType;
    }
    public void setStatus(String strStatus)
    {
    	 this.strStatus=strStatus;
    }
    public void setImageURL(String strImageURL)
    {
    	 this.strImageURL=strImageURL;
    }
    public void setReference(String strReference)
    {
    	 this.strReference=strReference;
	}
    public String getID() 
    {
        return strID;
    }
    public String getImageName()
    {
        return strImageName;
    }
    public String getContentName()
    {
        return strContentName;
    }
    public String getTitle()
    {
        return strTitle;
    }
    public String getFileType()
    {
        return strFileType;
    }
    public String getStatus()
    {
        return strStatus;
    }
    public String getImageURL()
    {
        return strImageURL;
    }
    public String getReference()
    {
        return strReference;
    }
}
