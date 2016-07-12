package com.flatironschool.javacs;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import redis.clients.jedis.Jedis;


/**
 * Represents the results of a search query.
 *
 */
public class WikiSearch {
	
	// map from URLs that contain the term(s) to relevance score
	private Map<String, Integer> map;

	/**
	 * Constructor.
	 * 
	 * @param map
	 */
	public WikiSearch(Map<String, Integer> map) {
		this.map = map;
	}
	
	/**
	 * Looks up the relevance of a given URL.
	 * 
	 * @param url
	 * @return
	 */
	public Integer getRelevance(String url) {
		Integer relevance = map.get(url);
		return relevance==null ? 0: relevance;
	}
	
	/**
	 * Prints the contents in order of term frequency.
	 * 
	 * @param map
	 */
	private  void print() {
		List<Entry<String, Integer>> entries = sort();
		for (Entry<String, Integer> entry: entries) {
			System.out.println(entry);
		}
	}
	
	/**
	 * Computes the union of two search results.
	 * 
	 * @param that
	 * @return New WikiSearch object.
	 */
	public WikiSearch or(WikiSearch that) {
    Map<String, Integer> map = that.map;
    for (String curr : this.map.keySet()) {
    	if(that.map.containsKey(curr)) {
    		Integer newvalue = this.map.get(curr) + that.map.get(curr);
    		map.remove(curr);
    		map.put(curr, newvalue);
    	} else {
    		map.put(curr, this.map.get(curr));
    	}
    }
		return new WikiSearch(map);
	}
	
	/**
	 * Computes the intersection of two search results.
	 * 
	 * @param that
	 * @return New WikiSearch object.
	 */
	public WikiSearch and(WikiSearch that) {
    Map<String, Integer> map = new HashMap();
    for (String curr : this.map.keySet()) {
    	if(that.map.containsKey(curr)) {
    		map.put(curr, that.map.get(curr) + this.map.get(curr));
    	}
    }
		return new WikiSearch(map);
	}
	
	/**
	 * Computes the intersection of two search results.
	 * 
	 * @param that
	 * @return New WikiSearch object.
	 */
	public WikiSearch minus(WikiSearch that) {
    Map<String, Integer> map = new HashMap();
    for (String curr : this.map.keySet()) {
    	if(!that.map.containsKey(curr)) {
    		map.put(curr, this.map.get(curr));
    	}
    }
		return new WikiSearch(map);
	}
	
	/**
	 * Computes the relevance of a search with multiple terms.
	 * 
	 * @param rel1: relevance score for the first search
	 * @param rel2: relevance score for the second search
	 * @return
	 */
	protected int totalRelevance(Integer rel1, Integer rel2) {
		// simple starting place: relevance is the sum of the term frequencies.
		return rel1 + rel2;
	}

	/**
	 * Sort the results by relevance.
	 * 
	 * @return List of entries with URL and relevance.
	 */
	public List<Entry<String, Integer>> sort() {
		List<Entry<String, Integer>> list = new LinkedList<Entry<String, Integer>>(this.map.entrySet());
		Collections.sort(list, new Comparator<Entry<String, Integer>>()
		{
			public int compare(Entry<String, Integer> entry1, Entry<String, Integer> entry2) {
				return entry1.getValue().compareTo(entry2.getValue());
			}

		});

		List<Entry<String, Integer>> sortedList = new LinkedList<Entry<String, Integer>>();
		for(Entry<String, Integer> curr : list) {
			sortedList.add(curr);
		}

		return sortedList;
	}



/*

    List<Entry<String, Integer>> list = new Comparator<Entry<String, Integer>>();
		return Collections.sort(this.map, comparator);
	}

	Comparator<Entry<String, Integer>> comparator = new Comparator<Entry<String, Integer>>();
	public int compare(Entry<String, Integer> first, Entry<String, Integer> second) {
		if(first.getValue()>second.getValue()) {
			return 1;
		}
		if(first.getValue()<second.getValue()) {
			return -1;
		}
		return 0;
	}*/

	/**
	 * Performs a search and makes a WikiSearch object.
	 * 
	 * @param term
	 * @param index
	 * @return
	 */
	public static WikiSearch search(String term, JedisIndex index) {
		Map<String, Integer> map = index.getCounts(term);
		return new WikiSearch(map);
	}

	public static void main(String[] args) throws IOException {
		
		// make a JedisIndex
		Jedis jedis = JedisMaker.make();
		JedisIndex index = new JedisIndex(jedis); 
		
		// search for the first term
		String term1 = "java";
		System.out.println("Query: " + term1);
		WikiSearch search1 = search(term1, index);
		search1.print();
		
		// search for the second term
		String term2 = "programming";
		System.out.println("Query: " + term2);
		WikiSearch search2 = search(term2, index);
		search2.print();
		
		// compute the intersection of the searches
		System.out.println("Query: " + term1 + " AND " + term2);
		WikiSearch intersection = search1.and(search2);
		intersection.print();
	}
}
