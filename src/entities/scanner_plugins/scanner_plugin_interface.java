package entities.scanner_plugins;


import data_structures.posting_unit;

public interface scanner_plugin_interface{
	public void set_parameters(Object param);
	public long conduct(posting_unit pUnit); 
}