/**
 * 
 */
package com.example.myegonetwork;

/**
 * @author Valerio Arnaboldi (valerio.arnaboldi@gmail.com)
 *
 */
public class FBErrorException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public FBErrorException(String error){
		super(error);
	}

}
