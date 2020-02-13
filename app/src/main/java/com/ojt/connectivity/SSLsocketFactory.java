/*@ID: CN20140001
 *@Description: srcSSLsocketFactory 
 * SSL certificate check for secured connection 
 * @Developer: Arunachalam
 * @Version 1.0
 * @Stage: 1
 * @Date: 10/03/2014
 */
package com.ojt.connectivity;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.http.conn.ssl.SSLSocketFactory;
public class SSLsocketFactory extends SSLSocketFactory 
{
	SSLContext sslcontext = SSLContext.getInstance("TLS");
    public SSLsocketFactory(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException 
    {
        super(truststore);
        TrustManager trustmanager = new X509TrustManager() 
        {
            public java.security.cert.X509Certificate[] getAcceptedIssuers()
            {
                return null;
            }
			@Override
			public void checkClientTrusted(
					java.security.cert.X509Certificate[] chain, String authType)
					throws java.security.cert.CertificateException {}
			@Override
			public void checkServerTrusted(
					java.security.cert.X509Certificate[] chain, String authType)
					throws java.security.cert.CertificateException {}
        };
        sslcontext.init(null, new TrustManager[] {trustmanager}, null);
    }
    @Override
    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException 
    {
        return sslcontext.getSocketFactory().createSocket(socket, host, port, autoClose);
    }
    @Override
    public Socket createSocket() throws IOException 
    {
        return sslcontext.getSocketFactory().createSocket();
    }
}