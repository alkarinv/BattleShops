package mc.alk.shops.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class CompositeMap<T> {
	HashMap<Integer,HashMap<Integer,T>> map = new HashMap<Integer,HashMap<Integer,T>>();

	public T put(int l, int h, T value){
		HashMap<Integer,T> map2 = map.get(l);
		if (map2 == null){
			map2 = new HashMap<Integer,T>();
			map.put(l, map2);
		}
		return map2.put(h, value);
	}

	public T put(long key, T value){
		int l = (int) (key >> 32);
		int h = (int) key;
		HashMap<Integer,T> map2 = map.get(l);
		if (map2 == null){
			map2 = new HashMap<Integer,T>();
			map.put(l, map2);
		}
		return map2.put(h, value);
	}

	public void clear(){
		map.clear();
	}

	public static long toKey(int hob, int lob){
		return (((long)hob) << 32) | (lob & 0xffffffffL);
	}

	public boolean containsKey(long key) {
		int l = (int) (key >> 32);
		int h = (int) key;
		HashMap<Integer,T> map2 = map.get(l);
		return map2 != null && map2.containsKey(h);
	}

	public T get(long key) {
		int hob = (int) (key >> 32);
		int lob = (int) key;
		HashMap<Integer,T> map2 = map.get(hob);
		return map2==null ? null : map2.get(lob);
	}

	public T remove(long key) {
		int l = (int) (key >> 32);
		int h = (int) key;
		HashMap<Integer,T> map2 = map.get(l);
		return map2==null ? null : map2.remove(h);
	}

	public Collection<Long> keySet() {
		List<Long> keys = new ArrayList<Long>();
		Long base = 0L;
		for (Integer k : map.keySet()){
			base = (long) k << 32;
			for (Integer k2 : map.get(k).keySet()){
				keys.add(base | (k2 & 0xffffffffL));
			}
		}
		return keys;
	}

	public Collection<T> values() {
		List<T> values = new ArrayList<T>();
		for (HashMap<Integer, T> v : map.values()){
			for (T t : v.values()){
				values.add(t);
			}
		}
		return values;
	}

	public static int getHOB(long itemid) {
		return (int) (itemid >> 32);
	}
	public static int getLOB(long itemid) {
		return (int) itemid;
	}
}
