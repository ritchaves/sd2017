package org.komparator.security;

import java.io.*;
import java.security.*;
import javax.crypto.*;
import java.util.*;

public class CryptoUtil {

	
	/*
	 *  asymCipher() que recebe dados (byte[]) e uma chave e devolve esses dados cifrados. 
	*/
	public static byte[] asymCipher(byte[] plainBytes, PublicKey publicKey) throws NoSuchAlgorithmException, NoSuchPaddingException, 
		InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			byte[] cipherBytes = cipher.doFinal(plainBytes);
			return cipherBytes;
	}
	
	/*
	 * asymDecipher() na classe que recebe os dados cifrados e uma chave e devolve os dados decifrados. 
	 */
	public static byte[] asymDecipher(byte[] bytes, PrivateKey privateKey) throws NoSuchAlgorithmException, NoSuchPaddingException, 
	InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.ENCRYPT_MODE, privateKey);
		byte[] decipherBytes = cipher.doFinal(bytes); 
		return decipherBytes;
	}

	// digital signature --------
	
	public static byte[] makeDigitalSignature(final PrivateKey privateKey, final byte[] bytesToSign) throws NoSuchAlgorithmException, 
		InvalidKeyException, SignatureException {
		Signature signature = Signature.getInstance("SHA256withRSA");
		signature.initSign(privateKey);
		signature.update(bytesToSign);
		byte[] signatureResult = signature.sign();
		return signatureResult;
	}
	
	public static boolean verifyDigitalSignature(PublicKey publicKey, byte[] bytesToVerify, byte[] signature) throws NoSuchAlgorithmException,
		InvalidKeyException, SignatureException {
		Signature sig = Signature.getInstance("SHA256withRSA");
		sig.initVerify(publicKey);
		sig.update(bytesToVerify);
		return sig.verify(signature);
	}	
	
}
