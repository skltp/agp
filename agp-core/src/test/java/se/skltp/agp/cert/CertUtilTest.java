package se.skltp.agp.cert;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import javax.xml.bind.DatatypeConverter;

import org.junit.Test;

import sun.security.provider.X509Factory;

public class CertUtilTest {

	@Test
	public void testCreatePemFormat () throws Exception {

		// Based on ideas from: 
		// http://stackoverflow.com/questions/3313020/write-x509-certificate-into-pem-formatted-string-in-java

		KeyStore ks  = KeyStore.getInstance("jks");
		ks.load(new FileInputStream("src/test/certs/client.jks"), "password".toCharArray());
		Certificate cert = ks.getCertificate("consumer");
		X509Certificate x509Cert = (X509Certificate)cert;

//		Enumeration<String> a = ks.aliases();
//		while (a.hasMoreElements()) {
//			String string = (String) a.nextElement();
//		}
//
//		int i = 0;
//		if (i == 0) return;
		
//		InputStream inStream = new FileInputStream("src/test/certs/client.jks");
//		X509Certificate cert = X509Certificate.getInstance(inStream);
//		inStream.close();
		 
//	    try {
	        System.out.println(X509Factory.BEGIN_CERT);
	        String pemCertContent = DatatypeConverter.printBase64Binary(x509Cert.getEncoded()); 
	        System.out.println(pemCertContent);
	        System.out.println(X509Factory.END_CERT);
//	    } catch (CertificateEncodingException e) {
//	        e.printStackTrace();
//	    }		 

	        // TODO: Try to revert it...
	//		final X509Certificate certificate = PemConverter.buildCertificate(pemCertContent);
	
	}
}