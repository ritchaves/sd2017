package org.komparator.security;

import java.io.*;
import java.security.*;
import javax.crypto.*;
import java.util.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import static javax.xml.bind.DatatypeConverter.printBase64Binary;

public class CryptoUtil {
	
	
	//Cipher ------------------------------------------------------------------------
	
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

	// Digital signature ------------------------------------------------------------------------
	
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
	
	
	//Random Number Generator-------------------
	
	public static String randomTokenGenerator() {

		SecureRandom random;
		try {
			random = SecureRandom.getInstance("SHA1PRNG");
			final byte array[] = new byte[32];
			random.nextBytes(array);
			return printBase64Binary(array);
			
		} catch (NoSuchAlgorithmException e) {
			System.err.print("Random Token Gen: This algo does not exist!!");
			return null;
		}
		
	}


	//Certificate ------------------------------------------------------------------------
	
	public static Certificate getX509CertificateFromResource(String certificateResourcePath)
			throws IOException, CertificateException {
		InputStream is = getResourceAsStream(certificateResourcePath);
		return getX509CertificateFromStream(is);
	}
	
	public static Certificate getX509CertificateFromStream(InputStream in) throws CertificateException {
		try {
			CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
			Certificate cert = certFactory.generateCertificate(in);
			return cert;
		} finally {
			closeStream(in);
		}
	}
	/*Para obter a chave a partir do certificado*/
	public static PublicKey getPublicKeyFromCertificate(Certificate certificate) {
		return certificate.getPublicKey();
	}
	
	/*Para verificar os certificados recebidos*/
	public static boolean verifySignedCertificate(Certificate certificate, Certificate caCertificate) {
		return verifySignedCertificate(certificate, caCertificate.getPublicKey());
	}
	
	public static boolean verifySignedCertificate(Certificate certificate, PublicKey caPublicKey) {
		try {
			certificate.verify(caPublicKey);
		} catch (InvalidKeyException | CertificateException | NoSuchAlgorithmException | NoSuchProviderException
				| SignatureException e) {
			System.err.println("Caught exception while verifying certificate with CA public key : " + e);
			System.err.println("Returning false.");
			return false;
		}
		return true;
	}
	
	//Private Key ------------------------------------------------------------------
	
		public static PrivateKey getPrivateKeyFromKeyStoreResource(String keyStoreResourcePath, char[] keyStorePassword,
				String keyAlias, char[] keyPassword)
				throws FileNotFoundException, KeyStoreException, UnrecoverableKeyException {
			KeyStore keystore = readKeystoreFromResource(keyStoreResourcePath, keyStorePassword);
			return getPrivateKeyFromKeyStore(keyAlias, keyPassword, keystore);
		}
		
		public static KeyStore readKeystoreFromResource(String keyStoreResourcePath, char[] keyStorePassword)
				throws KeyStoreException {
			InputStream is = getResourceAsStream(keyStoreResourcePath);
			return readKeystoreFromStream(is, keyStorePassword);
		}
		
		public static PrivateKey getPrivateKeyFromKeyStore(String keyAlias, char[] keyPassword, KeyStore keystore)
				throws KeyStoreException, UnrecoverableKeyException {
			PrivateKey key;
			try {
				key = (PrivateKey) keystore.getKey(keyAlias, keyPassword);
			} catch (NoSuchAlgorithmException e) {
				throw new KeyStoreException(e);
			}
			return key;
		}
		
		private static KeyStore readKeystoreFromStream(InputStream keyStoreInputStream, char[] keyStorePassword)
				throws KeyStoreException {
			KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
			try {
				keystore.load(keyStoreInputStream, keyStorePassword);
			} catch (NoSuchAlgorithmException | CertificateException | IOException e) {
				throw new KeyStoreException("Could not load key store", e);
			} finally {
				closeStream(keyStoreInputStream);
			}
			return keystore;
		}

	// resource stream helpers ------------------------------------------------------------------------

	/** Method used to access resource. */
	private static InputStream getResourceAsStream(String resourcePath) {
		// uses current thread's class loader to also work correctly inside
		// application servers
		// reference: http://stackoverflow.com/a/676273/129497
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);
		return is;
	}

	/** Do the best effort to close the stream, but ignore exceptions. */
	private static void closeStream(InputStream in) {
		try {
			if (in != null)
				in.close();
		} catch (IOException e) {
			// ignore
		}
	}
}
