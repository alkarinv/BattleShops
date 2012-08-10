package com.alk.battleShops.util;

/**
 * 
 * @author alkarin
 *
 */
public class Pair<T,T2> {
	public Pair() {fst = null; snd = null;}
	public Pair(T fst, T2 snd) {
		this.fst = fst;
		this.snd = snd;
	}
	public T fst;
	public T2 snd;
}
