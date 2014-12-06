package bcby.rutgers.parkinghelper;

import java.io.Serializable;

public class Lot implements Serializable{
	public int id;
	public String name;
    public String location;
    public double longitude;
    public double latitude;
    public String type;
    public String startTime;
    public String endTime;
    
    Lot(int _id,String _name,String _location,double _longitude,double _latitude,String _type
    		,String _sT,String _eT){
    	this.id = _id;
    	this.name = _name;
    	this.location = _location;
    	this.longitude = _longitude;
    	this.latitude = _latitude;
    	this.type = _type;
    	this.startTime= _sT;
    	this.endTime= _eT;
    }
}