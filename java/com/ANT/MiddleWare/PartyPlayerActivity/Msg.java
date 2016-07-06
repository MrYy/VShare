package com.ANT.MiddleWare.PartyPlayerActivity;


public class Msg {
	public static final int TYP_RECIEVED=0;
	public static final int TYP_SEND=1;
	public String content;
	public String name;
	public Long timesamp;
	private int type;
	public Msg(String content,int type,String name,Long timesamp)
	{this.content=content;
	this.type=type;
	this.name=name;
	this.timesamp=timesamp;
	}
	public String getContent()
	{return content;}
    public int getType()
    {return type;}
    public String getName()
    {return name;}
    public Long getTimesamp()
    {return timesamp;}
    
}
