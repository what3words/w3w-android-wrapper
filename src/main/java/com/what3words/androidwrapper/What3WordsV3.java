package com.what3words.androidwrapper;

import java.security.MessageDigest;

import com.google.common.io.BaseEncoding;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;

public class What3WordsV3 extends com.what3words.javawrapper.What3WordsV3 {
    private static String getSignature(Context context) {
        if (context == null) {
            return null;
        }
        
        try {
            PackageManager packageManager = context.getPackageManager();
            String packageName = context.getPackageName();
            
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
            if (packageInfo == null
                    || packageInfo.signatures == null
                    || packageInfo.signatures.length == 0
                    || packageInfo.signatures[0] == null) {
                return null;
            }
            return signatureDigest(packageInfo.signatures[0]);
        } catch (Exception e) {
            return null;
        }
    }
    private static String signatureDigest(Signature sig) {
        if (sig == null) {
            return null;
        }
        
        try {
            byte[] signature = sig.toByteArray();
            
            MessageDigest md = MessageDigest.getInstance("SHA1");
            byte[] digest = md.digest(signature);
            return BaseEncoding.base16().lowerCase().encode(digest);
        } catch (Exception e) {
            return null;
        }
    }
    
    public What3WordsV3(String apiKey, Context context) {
        super(apiKey, context.getPackageName(), getSignature(context));
    }
    
    public What3WordsV3(String apiKey, String endpoint, Context context) {
        super(apiKey, endpoint, context.getPackageName(), getSignature(context));
    }
}
