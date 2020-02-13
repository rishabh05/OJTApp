/*@ID: CN20140001
 *@Description: srcRowItem is used to stored BA's name,id,record id 
 * @Developer: Arunachalam
 * @Version 1.0
 * @Date: 21/03/2014
 */
package com.ojt.readyreckoner;
public class RowItem
{
    private String strName=null;
    private String strID=null;
    private String strDetailID=null;
 
    public RowItem(String strName, String strID,String strDetailID) 
    {
        this.strID=strID;
        this.strName=strName;
        this.strDetailID=strDetailID;
    }
    public String getDetailID() 
    {
        return strDetailID;
    }
    public String getID()
    {
        return strID;
    }
    public String getName()
    {
        return strName;
    }
 }