package io.github.data4all;

import io.github.data4all.model.data.Tags;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author Maurice Boyke
 *
 */
public class Tagging {
	/**
	 * 
	 * @return the Keys of Tags
	 */
	public List<String> getKeys(){
		List<String> list = new ArrayList<String>();
		Tags tags = new Tags();
		Map<String, String> map = new HashMap <String, String>();
		map = tags.getClassifiedTags();
		Iterator<String> keySetIterator = map.keySet().iterator();
		while(keySetIterator.hasNext()){
			String key = keySetIterator.next();
			list.add(key);
		}
		return list;
	}
	/**
	 * 
	 * @param key the Key of the Hashmap
	 * @return the Values of the Key
	 */
	public List<String> getValues(String key){
		Tags tags = new Tags();
		Map<String, String> map = new HashMap <String, String>();
		map = tags.getClassifiedTags();
		String [] split;
		split = map.get(key).split(",");
		List<String> list= new ArrayList<String>(Arrays.asList(split));
		return list;
	}
	/**
	 * 
	 * @param key
	 * @param value
	 * @return a HashMap with the Keys and Values
	 */
	public Map<String, String> hashMapTag(String key, String value){
		Map<String, String> map = new HashMap <String, String>();
		map.put(key, value);
		return map;
	}
}
