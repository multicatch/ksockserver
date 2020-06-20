package io.github.multicatch.ksock.tcp

import org.apache.logging.log4j.LogManager
import sun.security.tools.keytool.CertAndKeyGen
import sun.security.x509.X500Name
import java.security.PrivateKey
import java.security.cert.Certificate


fun selfSignedCertificate(size: Int = 4096, name: String = "localhost", validity: Long = 365 * 24 * 3600): CertificateWithKey {
    logger.debug("Generating self-signed certificate of length ${size} for name ${name} with validity of ${validity}")
    val keyGen = CertAndKeyGen("RSA", "SHA1WithRSA", null)
    keyGen.generate(size)

    return CertificateWithKey(
            certificate = keyGen.getSelfCertificate(X500Name("""CN=$name"""), validity),
            key = keyGen.privateKey
    )
}

data class CertificateWithKey(
        val certificate: Certificate,
        val key: PrivateKey
)

private val logger = LogManager.getLogger(SecureTcpRequestDispatcher::class.java)