import com.android.apksig.ApkSigner;
import java.io.File;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class ApkSignerTool {
    public static void main(String[] args) throws Exception {
        if (args.length < 4) {
            System.err.println("Usage: ApkSignerTool <input.apk> <output.apk> <keystore.jks> <ks-pass> [key-alias] [key-pass]");
            System.exit(1);
        }

        File inputApk = new File(args[0]);
        File outputApk = new File(args[1]);
        File keystoreFile = new File(args[2]);
        String ksPass = args[3];
        String keyAlias = args.length > 4 ? args[4] : null;
        String keyPass = args.length > 5 ? args[5] : ksPass;

        // Load keystore
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        try (java.io.FileInputStream fis = new java.io.FileInputStream(keystoreFile)) {
            ks.load(fis, ksPass.toCharArray());
        }

        // Resolve alias
        if (keyAlias == null) {
            Enumeration<String> aliases = ks.aliases();
            if (aliases.hasMoreElements()) {
                keyAlias = aliases.nextElement();
            } else {
                System.err.println("Keystore has no aliases");
                System.exit(1);
            }
        }

        PrivateKey privateKey = (PrivateKey) ks.getKey(keyAlias, keyPass.toCharArray());
        java.security.cert.Certificate[] certChain = ks.getCertificateChain(keyAlias);

        List<X509Certificate> certs = new ArrayList<>();
        for (java.security.cert.Certificate cert : certChain) {
            certs.add((X509Certificate) cert);
        }

        ApkSigner.SignerConfig signerConfig = new ApkSigner.SignerConfig.Builder(
                "signer #1", privateKey, certs).build();

        ApkSigner.Builder builder = new ApkSigner.Builder(Collections.singletonList(signerConfig));
        builder.setInputApk(inputApk);
        builder.setOutputApk(outputApk);
        builder.setV1SigningEnabled(true);
        builder.setV2SigningEnabled(true);
        builder.setV3SigningEnabled(true);

        ApkSigner signer = builder.build();
        signer.sign();

        System.out.println("SUCCESS: Signed " + outputApk.getAbsolutePath() + " with v1+v2+v3 schemes");
    }
}
